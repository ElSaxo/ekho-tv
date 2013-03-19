/*
 * @(#)MovieSourceNode.java	1.7 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.protocol.reliable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.media.CachingControlEvent;
import javax.media.Control;
import javax.media.Controller;
import javax.media.ControllerErrorEvent;
import javax.media.DurationUpdateEvent;
import javax.media.EndOfMediaEvent;
import javax.media.StopByRequestEvent;
import javax.media.StopEvent;
import javax.media.Time;
import javax.media.TimeBase;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.PushDataSource;

import android.util.Log;

import com.sun.tv.media.AudData;
import com.sun.tv.media.BadHeaderException;
import com.sun.tv.media.Data;
import com.sun.tv.media.Format;
import com.sun.tv.media.InputConnectable;
import com.sun.tv.media.MediaCachingControl;
import com.sun.tv.media.MediaProcessor;
import com.sun.tv.media.MediaPullSourceStream;
import com.sun.tv.media.MediaSourceNode;
import com.sun.tv.media.OutputConnectable;
import com.sun.tv.media.Parser;
import com.sun.tv.media.Track;
import com.sun.tv.media.UnsupportedFormatEvent;
import com.sun.tv.media.VidData;
import com.sun.tv.media.content.DefaultParserFactory;
import com.sun.tv.media.content.InvalidTrackIDException;
import com.sun.tv.media.content.video.TChunkList;
import com.sun.tv.media.controls.ProgressControl;
import com.sun.tv.media.controls.StringControl;
import com.sun.tv.media.format.audio.AudioFormat;
import com.sun.tv.media.format.video.VideoFormat;
import com.sun.tv.media.protocol.MediaPullDataSource;
import com.sun.tv.media.protocol.reliable.caching.CachingInputStream;
import com.sun.tv.media.util.JMFI18N;
import com.sun.tv.media.util.LoopThread;

public class MovieSourceNode extends MediaSourceNode implements
	com.sun.tv.media.util.Update {

    public boolean DEBUG = false;

    // Output connectables.
    protected final static String AudioOutName = /* NOI18N */"AudioOut";
    protected final static String VideoOutName = /* NOI18N */"VideoOut";

    protected int AudioTrack = 0;
    protected int VideoTrack = 0;

    private Control controls[];
    private String cacheMode;
    private boolean flushingAudio = false;
    private boolean flushingVideo = false;
    private boolean sourceIsOn = false;
    private long startvalue = 0L;
    private long endvalue = 0L;

    private MediaCachingControl mcc[];
    protected MediaPullDataSource mediaSource[];
    protected Parser parser;
    private int soundError;

    // Cache of the down stream connectables to which we'll pass
    // along the data.
    protected InputConnectable nextAudioIn;
    protected InputConnectable nextVideoIn;

    // Movie attributes.
    protected long duration; // in secs.
    protected AudioFormat audioFormat;
    protected VideoFormat videoFormat;
    private int iVideoIndex; // Current Frame
    private long frameTime = 0; // in nano secs

    private boolean audioEnable = false;
    private boolean videoEnable = false;

    private int iAudioIndex; // Current Sound Chunk
    protected mAudioTimer audioTimer;
    protected mVideoTimer videoTimer;
    protected TimeBase timeBase;

    private boolean eomForEitherMedia = false;

    private ProgressControl progressControl;
    private long bitsRead; // total bits read so far.
    private long updateInterval = 1000; // in millisecs.
    private int kbps = 0;
    private long bitRateIntervalCounter;
    private StringControl bitRateControl;
    private Track track[];

    private long newVideoIndexOffset = -1;
    private long newAudioIndexOffset = -1;

    public MovieSourceNode() {
	super();
	nextAudioIn = null;
	nextVideoIn = null;
	audioFormat = null;
	videoFormat = null;
	audioTimer = null;
	videoTimer = null;
	timeBase = null;
	iVideoIndex = 0;
	iAudioIndex = 0;
	duration = 0;
	soundError = 0;
    }

    public MovieSourceNode(javax.media.protocol.DataSource s)
	    throws IOException {
	this();
	if (s instanceof PushDataSource) {
	    Log.e("EkhoTV", /* NOI18N */"Is a PushDataSource");
	}
	if (!(s instanceof PullDataSource)) {
	    Log.e("EkhoTV", /* NOI18N */"Only PullDataSources are implemented");
	    return;
	}

	boolean otherDataSource = (!((s instanceof com.sun.tv.media.protocol.file.DataSource) || (s instanceof com.sun.tv.media.protocol.http.DataSource)));

	if (otherDataSource) {
	    MediaPullDataSource mpds;
	    mpds = (MediaPullDataSource) new mPullDataSource((PullDataSource) s);
	    s = mpds;
	}
	cacheMode = getCacheMechanism(s);

	if ((cacheMode == FILE_CACHE_ENABLE)) {
	    int maxTracks = 20;
	    mediaSource = new MediaPullDataSource[1];
	    mediaSource[0] = (MediaPullDataSource) s;
	    String lp = findLocalPath()
		    + getCacheFile((mediaSource[0].getURL()).getFile());
	    PullSourceStream pss = (mediaSource[0].getStreams())[0];
	    mcc = new MediaCachingControl[maxTracks];
	    int length = (int) pss.getContentLength();
	    mcc[0] = new MediaCachingControl(pss, length, lp);
	    mcc[0].setCallback(this); // Added
	    mcc[0].setBufferSize(getCacheSize());
	    mcc[0].startDownload();
	} else {
	    mediaSource = new MediaPullDataSource[2];
	    mediaSource[0] = (MediaPullDataSource) s;
	    mediaSource[0].connect(); //
	    mediaSource[1] = (MediaPullDataSource) ((MediaPullDataSource) s)
		    .clone();
	    mediaSource[1].connect();
	}
	registerOutput(AudioOutName, new mAudioOut(this));
	registerOutput(VideoOutName, new mVideoOut(this));
    }

    //
    // Read Quicktime resource and set the data stream pointer
    // Get and Set audio and video information.
    protected boolean doRealize() {

	// PullSourceStream pss = (mediaSource[0].getStreams())[0];
	PullSourceStream pss;
	String contentType = mediaSource[0].getContentType();
	long tduration = 0;

	startSource(true, true);

	// TODO: Add pointer check
	if (cacheMode == FILE_CACHE_ENABLE) {
	    pss = mcc[0].getStream();
	    mcc[0].startDownload(); // Start or resume download
	} else {
	    pss = (mediaSource[0].getStreams())[0];
	}

	parser = DefaultParserFactory.createParser(contentType);
	if (parser == null)
	    return false;
	parser.setSourceStream(pss);
	try {
	    parser.readHeader();
	} catch (BadHeaderException e) {
	    e.printStackTrace();
	    Log.e("EkhoTV", /* NOI18N */"readHeader failed " + e);
	    return false;
	} catch (IOException e) {
	    e.printStackTrace();
	    Log.e("EkhoTV", /* NOI18N */"readHeader failed " + e);
	    return false;
	}

	track = parser.getTracks();

	if (track == null) {
	    Log.e("EkhoTV", /* NOI18N */"There is no track.");
	    return false;
	}

	if (cacheMode != CACHE_DISABLE)
	    mcc = new MediaCachingControl[track.length];

	for (int i = 0; i < track.length; i++) {
	    // Retrieve the format information from the track
	    // class.
	    Format format = track[i].getFormat();
	    if (format instanceof AudioFormat) {
		// NOTE: (to cania)
		// This is not a good fix. Should find a way to deal with
		// multiple tracks. Bugid 4091534
		if (i < 2) {
		    AudioTrack = i;
		    audioFormat = (AudioFormat) ((AudioFormat) format).clone();
		    audioEnable = audioFormat.getEnableFlag();
		    tduration = audioFormat.getDuration();
		}
	    }
	    if (format instanceof VideoFormat) {
		if (i < 2) {
		    VideoTrack = i;
		    videoFormat = (VideoFormat) ((VideoFormat) format).clone();
		    videoEnable = videoFormat.getEnableFlag();
		    tduration = videoFormat.getDuration();
		}
	    }

	    if (tduration > duration) {
		duration = tduration;
	    }

	    // Note: Do not clone data source if cacheMode is
	    // file.
	    // $$$ NOT CALLING SETPARSER if FILE_CACHE_ENABLE ??
	    // $$$ Check with Cania
	    // ORIG if ((cacheMode != FILE_CACHE_ENABLE) || (i == 0)) {
	    if (cacheMode != FILE_CACHE_ENABLE) { // NEW
		if (i < 2) {
		    mediaSource[i].setParser(parser);
		    mediaSource[i].setPosition(-1); // rewind the file.
		    try {
			mediaSource[i].setTrackID(i);
		    } catch (InvalidTrackIDException e) {
			e.printStackTrace();
			return false;
		    }
		}
	    }

	    if (cacheMode == FILE_CACHE_ENABLE) {
		// if ((i > 0) && (parser.isStreamable()))
		if (i > 0) {
		    mcc[i] = (MediaCachingControl) mcc[0].clone();
		}
	    }

	    // $$TODO We should remove references to RAM CACHE
	    if (cacheMode == RAM_CACHE_ENABLE) {
		pss = (mediaSource[i].getStreams())[0];
		mcc[i] = new MediaCachingControl(pss, duration);
		if (mcc[i].setBufferSize(getCacheSize()) < 0) {
		    Log.e("EkhoTV","RAM cache not available. Disabling caching");
		    cacheMode = CACHE_DISABLE;
		}
	    }
	}
	// Log.e("EkhoTV", "Caching mode: "+cacheMode);

	sendEvent(new DurationUpdateEvent(this, new Time(duration * 1E+9)));

	// Check whether it is supported.

	if ((!isSupported(audioFormat)) && audioFormat != null) {
	    audioEnable = false;
	    sendEvent(new UnsupportedFormatEvent(this, audioFormat));
	}

	if ((!isSupported(videoFormat)) && videoFormat != null) {
	    videoEnable = false;
	    sendEvent(new UnsupportedFormatEvent(this, videoFormat));
	}

	//
	// Set the values of the property sheet
	//
	// StringControl aprop = progressControl.getAudioProperties();
	// StringControl acodec = progressControl.getAudioCodec();
	// String channel = /*NOI18N*/"";
	// String sampSize = /*NOI18N*/"";
	/*
	 * if (audioEnable) { if (audioFormat.getChannels() == 1) channel = new
	 * String(JMFI18N.getResource("moviesourcenode.mono.string")); else if
	 * (audioFormat.getChannels() == 2) channel = new
	 * String(JMFI18N.getResource("moviesourcenode.stereo.string")); if
	 * (audioFormat.getCodec().equals("linear")) sampSize =
	 * audioFormat.getSampleSize() +
	 * JMFI18N.getResource("moviesourcenode.-bit") + " "; if (aprop != null)
	 * { aprop.setValue(audioFormat.getSampleRate()/1000.0 +
	 * JMFI18N.getResource("moviesourcenode.khz") + sampSize + channel); }
	 * 
	 * if (acodec != null) acodec.setValue(JMFI18N.getResource("codec." +
	 * audioFormat.getCodec())); } else { if (audioFormat != null) { if
	 * (acodec != null) acodec.setValue(audioFormat.getCodec() +
	 * JMFI18N.getResource("moviesourcenode.unsupported/disabled")); if
	 * (aprop != null)
	 * aprop.setValue(JMFI18N.getResource("mediaplayer.N/A")); } else { if
	 * (acodec != null)
	 * acodec.setValue(JMFI18N.getResource("moviesourcenode.none")); if
	 * (aprop != null)
	 * aprop.setValue(JMFI18N.getResource("mediaplayer.N/A")); } }
	 */

	/*
	 * StringControl vprop = progressControl.getVideoProperties();
	 * StringControl vcodec = progressControl.getVideoCodec(); if
	 * (videoEnable) { if (vprop != null)
	 * vprop.setValue(videoFormat.getWidth() + " x " +
	 * videoFormat.getHeight()); if (vcodec != null)
	 * vcodec.setValue(JMFI18N.getResource("codec." +
	 * videoFormat.getCodec())); } else { if (videoFormat != null) { if
	 * (vcodec != null) vcodec.setValue(videoFormat.getCodec() +
	 * JMFI18N.getResource("moviesourcenode.unsupported/disabled"));
	 * vprop.setValue(JMFI18N.getResource("mediaplayer.N/A"));
	 * 
	 * } else { if (vcodec != null)
	 * vcodec.setValue(JMFI18N.getResource("moviesourcenode.none"));
	 * vprop.setValue(JMFI18N.getResource("mediaplayer.N/A")); } }
	 */
	// if (cacheMode == CACHE_DISABLE)
	// regionControl.setEnable(false);
	return true;
    }

    public void doDispose() { // 4389657. Invoked when QuickTime's Handler
			      // closes.
	if (audioTimer != null)
	    audioTimer.kill();
	if (videoTimer != null)
	    videoTimer.kill();
    }

    public int getWidth() {
	if (videoFormat == null)
	    return 0;
	return videoFormat.getWidth();
    }

    public int getHeight() {
	if (videoFormat == null)
	    return 0;
	return videoFormat.getHeight();
    }

    public boolean getAudioEnable() {
	return audioEnable;
    }

    public boolean startSource(boolean on, boolean regardless) {
	if (sourceIsOn == on)
	    return true;
	if (regardless || cacheMode == CACHE_DISABLE) {
	    try {
		if (on)
		    mediaSource[0].start();
		else
		    mediaSource[0].stop();
	    } catch (Exception ge) {
		System.err.println("Couldn't stop the data source");
		return false;
	    }
	    sourceIsOn = on;
	}
	return true;
    }

    // $$ Added. Handle audio device is busy case
    public void disableAudio() {
	audioEnable = false;
    }

    public boolean getVideoEnable() {
	return videoEnable;
    }

    public VideoFormat getVideoFormat() {
	return videoFormat;
    }

    public AudioFormat getAudioFormat() {
	return audioFormat;
    }

    public String getVCodec() {
	if (videoFormat == null)
	    return null;
	return videoFormat.getCodec();
    }

    public String getACodec() {
	if (audioFormat == null)
	    return null;
	return audioFormat.getEncoding();
    }

    public int getMaxSoundSize() {
	if (audioFormat == null)
	    return 0;
	return audioFormat.getBytesPerSec();
    }

    public int getMaxVideoSize() {
	if (videoFormat == null)
	    return 0;
	// System.err.println("videoFormat.getMaxVideoSize() = " +
	// videoFormat.getMaxVideoSize());
	return videoFormat.getMaxVideoSize();
    }

    protected void abortRealize() {
	if (mcc != null && mcc[0] != null) {
	    mcc[0].stopDownload(); // Stop Download
	}
	startSource(false, true);
    }

    //
    // Fill audio and video buffer as much data as possible to reduce
    // start latency.
    protected boolean doPrefetch() {
	AudData audBuf;
	VidData vidBuf;

	startSource(true, false);

	if (mcc != null && mcc[0] != null) {
	    mcc[0].startDownload(); // Start or resume download
	    sendCacheControlEvent();
	}

	if (audioEnable) {
	    while ((audBuf = (AudData) nextAudioIn.tryGetContainer(audioFormat)) != null) {
		getAData(audBuf);
		nextAudioIn.putData(audBuf);
	    }
	}
	if (videoEnable) {
	    while ((vidBuf = (VidData) nextVideoIn.tryGetContainer(videoFormat)) != null) {
		getVData(vidBuf);
		vidBuf.setDiscard(false);
		nextVideoIn.putData(vidBuf);
	    }
	}

	if (audioEnable && videoEnable)
	    eomForEitherMedia = false;
	else
	    eomForEitherMedia = true;

	return true;
    }

    protected void abortPrefetch() {
	/*
	 * -- ivg should need to kill the threads. if (audioTimer != null) {
	 * audioTimer.stop(); audioTimer = null; } if (videoTimer != null) {
	 * videoTimer.stop(); videoTimer = null; } flush();
	 */
	if (mcc != null && mcc[0] != null) {
	    mcc[0].stopDownload(); // Stop Download
	}

	startSource(false, true);
    }

    //
    // Methods to implement the MediaProcessor interface.
    //
    public void doStart() {
	eomForEitherMedia = false;
	// try {
	// JMFSecurity.enablePrivilege.invoke(JMFSecurity.privilegeManager,
	// JMFSecurity.threadArgs);
	// JMFSecurity.enablePrivilege.invoke(JMFSecurity.privilegeManager,
	// JMFSecurity.threadGroupArgs);
	// } catch (Exception e) {}

	startSource(true, false);

	// start the read thread if it's not already started. If
	// it's already started, we'll just resume it.
	if (audioEnable) {
	    if (audioTimer == null) {
		audioTimer = new mAudioTimer(this);
		audioTimer.setName(/* NOI18N */"Audio timer thread: "
			+ getClass().getName());
	    }
	    audioTimer.restart();
	}

	if (videoEnable) {
	    if (videoTimer == null) {
		videoTimer = new mVideoTimer(this);
		videoTimer.setName(/* NOI18N */"Video timer thread: "
			+ getClass().getName());
	    }
	    videoTimer.restart();
	}
    }

    // To be used by the audio thread.
    public void processAudio() {
	AudData audBuf;

	flushingAudio = false;

	audBuf = (AudData) nextAudioIn.getContainer(audioFormat);

	if (flushingAudio) {
	    nextAudioIn.putContainer(audBuf);
	    flushingAudio = false;
	    return;
	}

	getAData(audBuf);
	updateSliderRegion(AudioTrack);

	// Check for end of media.
	if (audBuf.getLength() == Data.EOM) {
	    nextAudioIn.putData(audBuf);
	    audioTimer.pause();

	    if (!videoEnable || eomForEitherMedia) {
		pause();
		startSource(false, false);
		setTargetState(Prefetched);
		if (videoEnable)
		    eomForEitherMedia = false;
		sendEvent(new EndOfMediaEvent(this, Started, Prefetched,
			getTargetState(), getMediaTime()));
	    } else
		eomForEitherMedia = true;

	    return;
	}

	bitsRead += audBuf.getLength() * 8;
	updateBitRate();
	nextAudioIn.putData(audBuf);
    }

    // To be used by the read thread.
    public void processVideo() {
	long now;
	VidData vidBuf;

	flushingVideo = false;

	vidBuf = (VidData) nextVideoIn.getContainer(videoFormat);

	if (flushingVideo) {
	    nextVideoIn.putContainer(vidBuf);
	    flushingVideo = false;
	    return;
	}

	getVData(vidBuf);
	updateSliderRegion(VideoTrack);

	// Check for end of media.
	if (vidBuf.getLength() == Data.EOM) {

	    nextVideoIn.putData(vidBuf);
	    videoTimer.pause();

	    // if (eomForEitherMedia) {
	    if (!audioEnable || eomForEitherMedia) {
		pause();
		startSource(false, false);
		setTargetState(Prefetched);
		if (audioEnable)
		    eomForEitherMedia = false;
		sendEvent(new EndOfMediaEvent(this, Started, Prefetched,
			getTargetState(), getMediaTime()));
	    } else
		eomForEitherMedia = true;

	    return;
	}

	// Log.e("EkhoTV", "MSN: MT = " + getMediaNanoseconds() +
	// " PT = " + vidBuf.getPresentationTime());

	// Check to see if we are on schedule or not. If not, we'll
	// set the discard hint for the decoder to deal with it.
	// It might choose to drop it or decode it.
	if (getMediaNanoseconds() - vidBuf.getPresentationTime()
		- vidBuf.getDuration() / 4 > 0) {
	    Log.e("EkhoTV", "Running behind: MT = " + getMediaNanoseconds()
		    + " PT = " + vidBuf.getPresentationTime());
	    vidBuf.setDiscard(true);
	} else
	    vidBuf.setDiscard(false);

	// Update the bitrate control
	bitsRead += vidBuf.getLength() * 8;
	if (!audioEnable)
	    updateBitRate();

	nextVideoIn.putData(vidBuf);
    }

    // $$ TODO: cleanup 'if' statements.
    private PullSourceStream useCacheStream(int index, int trackid, int error) {
	CachingInputStream cis;
	PullSourceStream pss = null;
	long where, offset;
	Track thisTrack = (parser.getTracks())[trackid];

	offset = thisTrack.index2Offset(index);
	cis = (CachingInputStream) mcc[trackid].getStream();
	where = trySeek(cis, offset + error);

	if (where == (offset + error)) {
	    pss = cis;
	} else {
	    // Log.e("EkhoTV", "$$$: useCacheStream: returned from trySeek: "
	    // +
	    // where + ": " + (offset + error));
	    pss = cis;
	}
	return pss;
    }

    //
    // Fill up audio data
    //
    private synchronized void getAData(Data audBuf) {
	int totalRead = 0;
	int totalByte = 0;
	int errorByte = 0;
	PullSourceStream pss = null;
	Track audioTrack = (parser.getTracks())[AudioTrack];

	// No more audio data
	if (iAudioIndex >= audioTrack.getNumIndexes()) {
	    audBuf.setLength(Data.EOM);
	    return;
	}

	totalByte = audioTrack.getChunkSize(iAudioIndex);

	if (mcc != null && mcc[AudioTrack] != null) {
	    pss = useCacheStream(iAudioIndex, AudioTrack, soundError);
	} else {
	    long pos = mediaSource[AudioTrack].setPosition(iAudioIndex);
	    if (pos < 0)
		pss = null;
	    else
		pss = (mediaSource[AudioTrack].getStreams())[0];
	}
	// Log.e("EkhoTV", "getAData: tell " + ((Seekable) pss).tell());
	if (soundError != 0) {
	    totalByte -= soundError;
	    soundError = 0;
	}
	if (pss == null) {
	    Log.e("EkhoTV", /* NOI18N */
		    "PullSourceStream error in getAData, ControllerEventEvent is sent.");
	    sendEvent(new ControllerErrorEvent(this, /* NOI18N */
	    "PullSourceStream is NULL."));
	    this.pause();
	    return;
	}

	try {
	    totalRead = readFrame(pss, audBuf, totalByte);
	    audBuf.setLength(totalRead);
	} catch (IOException e) {
	    Log.e("EkhoTV", /* NOI18N */"getAData: IOException caught: " + e);
	    audBuf.setLength(0);
	}
	audBuf.setFormat(audioFormat);
	iAudioIndex++;
    }

    //
    // Fill up video data.
    //
    private synchronized void getVData(Data vidBuf) {
	int totalByte = 0, totalRead = 0;
	PullSourceStream pss = null;
	long offset, where;
	Track videoTrack = (parser.getTracks())[VideoTrack];

	// No more video data
	if (iVideoIndex >= videoTrack.getNumIndexes()) {
	    vidBuf.setPresentationTime(frameTime);
	    vidBuf.setLength(Data.EOM);
	    return;
	}

	// Sanity check to see if the buffer is big enough to hold the data.
	if (((byte[]) vidBuf.getBuffer()).length < totalByte) {
	    System.out
		    .println(getClass().getName()
			    +
			    /* NOI18N */": container not big enough to hold the compressed data: "
			    + ((byte[]) vidBuf.getBuffer()).length
			    + /* NOI18N */" < " + totalByte);
	    vidBuf.setLength(0);
	    iVideoIndex++;
	    return;
	}

	if (mcc != null && mcc[VideoTrack] != null) {
	    pss = useCacheStream(iVideoIndex, VideoTrack, 0);
	} else {
	    long pos = mediaSource[VideoTrack].setPosition(iVideoIndex);
	    if (pos < 0)
		pss = null;
	    else
		pss = (mediaSource[VideoTrack].getStreams())[0];
	}

	if (pss == null) {
	    // cania should post error.
	    System.out
		    .println(/* NOI18N */"PullSourceStream error in getVData, ControllerErrorEvent is sent.");
	    sendEvent(new ControllerErrorEvent(this, /* NOI18N */
	    "PullSourceStream is NULL."));
	    this.pause();
	    return;
	}

	totalByte = videoTrack.getChunkSize(iVideoIndex);
	try {
	    totalRead = readFrame(pss, vidBuf, totalByte);
	    vidBuf.setLength(totalRead);
	} catch (IOException e) {
	    Log.e("EkhoTV", /* NOI18N */"getVData: IOException caught: " + e);
	    vidBuf.setLength(0);
	}

	TChunkList cl = videoTrack.getList();
	boolean kf = cl.GetKFEntry(iVideoIndex);
	long duration = (cl.GetFrameTimeEntry(iVideoIndex) * 1000L);
	vidBuf.setDuration(duration);
	vidBuf.setPresentationTime(frameTime);
	frameTime += duration;
	videoFormat.setKeyFrame(kf);
	vidBuf.setFormat(videoFormat);
	iVideoIndex++;
    }

    public void update() {
	sendCacheControlEvent();
	if (getState() >= Controller.Realized) {
	    updateSliderRegion(AudioTrack);
	    updateSliderRegion(VideoTrack);
	}
    }

    private void updateSliderRegion(int currentTrack) {

	if (cacheMode == CACHE_DISABLE)
	    return;

	if ((mcc == null) || (mcc[0] == null))
	    return;

	// if (currentTrack != 0) { /// WHY $$
	// Log.e("EkhoTV", "updateSliderRegion: currentTrack != 0, return");
	// return;
	// }

	long svalue, evalue;
	Time smtime, emtime;
	int ref = iAudioIndex;

	if (currentTrack == VideoTrack)
	    ref = iVideoIndex;

	svalue = mcc[0].getStart();
	if (svalue != startvalue) {
	    startvalue = svalue;
	    smtime = track[0].offset2Time(svalue, ref);
	    // regionControl.setMinValue(smtime.getNanoseconds());
	}

	evalue = mcc[0].getEnd();
	if (evalue != endvalue) {
	    endvalue = evalue;
	    emtime = track[0].offset2Time(evalue, ref);
	    // regionControl.setMaxValue(emtime.getNanoseconds());
	}
    }

    // Cache the input connectable of the downstream node.
    public void connectAudio(InputConnectable ic) {
	nextAudioIn = ic;
    }

    // Cache the input connectable of the downstream node.
    public void connectVideo(InputConnectable ic) {
	nextVideoIn = ic;
    }

    public void setMediaTime(Time t) {
	super.setMediaTime(t);
	Time vt = null;

	if (videoEnable) {
	    iVideoIndex = track[VideoTrack].time2Index(t);
	    newVideoIndexOffset = track[VideoTrack].index2Offset(iVideoIndex); // ADDED
	    if (cacheMode != FILE_CACHE_ENABLE) {
		Time setTime = mediaSource[VideoTrack].setPosition(t, 1);
		if (setTime.getSeconds() < 0) {
		    // cania should post error.
		    sendEvent(new ControllerErrorEvent(this, /* NOI18N */
		    "setPosition() returns negative time."));
		    this.pause();
		    return;
		}
	    } else {
		Track thisTrack = (parser.getTracks())[VideoTrack];

		CachingInputStream cis = (CachingInputStream) mcc[VideoTrack]
			.getStream();
		long offset = thisTrack.time2Offset(t);
		// cis.seek(offset);
		cis.newSeekLocation(offset);
	    }
	    vt = track[VideoTrack].index2Time(iVideoIndex);
	    frameTime = vt.getNanoseconds();

	    if (DEBUG) {
		long offset = track[VideoTrack].index2Offset(iVideoIndex);
		vt = track[VideoTrack].offset2Time(offset);
		Log.e("EkhoTV", /* NOI18N */
			"Video: media time t = " + t.getSeconds());
		System.out.println(/* NOI18N */"     : index = " + iVideoIndex);
		Log.e("EkhoTV", /* NOI18N */"     : offset = " + offset);
		Log.e("EkhoTV", /* NOI18N */"     : time =  " + vt.getSeconds());
	    }
	}

	if (audioEnable) {
	    iAudioIndex = track[AudioTrack].time2Index(t);
	    newAudioIndexOffset = track[AudioTrack].index2Offset(iAudioIndex); // ADDED

	    long timeoffset, indexoffset;
	    timeoffset = track[AudioTrack].time2Offset(t);
	    indexoffset = track[AudioTrack].index2Offset(iAudioIndex);

	    soundError = (int) (timeoffset - indexoffset);
	    if (cacheMode != FILE_CACHE_ENABLE) {
		Time setTime = mediaSource[AudioTrack].setPosition(t, 1);
		if (setTime.getSeconds() < 0) {
		    // cania should post error.
		    System.out
			    .println(/* NOI18N */"PullSourceStream error in setMediaTime(audio), ControllerErrorEvent is sent.");
		    sendEvent(new ControllerErrorEvent(this, /* NOI18N */
		    "setPosition() returns negative time."));
		    this.pause();
		    return;
		}
	    } else {
		Track thisTrack = (parser.getTracks())[AudioTrack];

		CachingInputStream cis = (CachingInputStream) mcc[AudioTrack]
			.getStream();
		long offset = thisTrack.time2Offset(t);
		// cis.seek(offset);
		cis.newSeekLocation(offset);
	    }

	    if (DEBUG) {
		Log.e("EkhoTV", /* NOI18N */
			"Audio: media time t = " + t.getSeconds());
		System.out.println(/* NOI18N */"     : index = " + iAudioIndex);
		Log.e("EkhoTV", /* NOI18N */"     : offset = " + indexoffset);
		Log.e("EkhoTV", /* NOI18N */"     : time =  " + timeoffset);
		Log.e("EkhoTV", /* NOI18N */"     : soundError = " + soundError);
		Log.e("EkhoTV", /* NOI18N */"SMT:timeoffset = " + timeoffset);
		Log.e("EkhoTV", /* NOI18N */"SMT:indexoffset = " + indexoffset);
	    }
	}
    }

    private void pause() {
	// Pause the timer thread.
	if (audioEnable && (audioTimer != null))
	    audioTimer.pause();
	if (videoEnable && (videoTimer != null))
	    videoTimer.pause();
	super.stop();
    }

    public void stop() {
	this.pause();
	setTargetState(Prefetched);
	sendEvent((StopEvent) new StopByRequestEvent(this, Started, Prefetched,
		getTargetState(), getMediaTime()));
	startSource(false, false);
	bitsRead = 0;
	bitRateIntervalCounter = 0;
	updateBitRate();
    }

    public void flush() {
	// Put it self in a flushing state.
	// Then propagate the flush downstream.
	flushingAudio = true;
	flushingVideo = true;
	super.flush();
    }

    public void setStopTime(Time stopTime) {
	super.setStopTime(stopTime);
    }

    // public float setRate(float factor) {
    // return 1;
    // }

    // public float getRate() {
    // return 1;
    // }

    // Get the current time either base of the given timeBase, if
    // there's one, or use the system's time. The unit is in millisec.
    public long getTime() {
	if (timeBase != null)
	    return (timeBase.getNanoseconds() / 1000000L);
	else
	    return System.currentTimeMillis();
    }

    //
    // Methods to implement the Duration interface.
    //
    public Time getDuration() {
	return new Time(duration * 1000000000L); // from sec to nanos.
    }

    public Control[] getControls() {
	// Create the controls if they are not already created.
	return new Control[0];
    }

    public final void doClose() {
	// Free up TChunkList.

	if (audioTimer != null)
	    audioTimer.stop();
	if (videoTimer != null)
	    videoTimer.stop();

	if (mcc != null) {
	    for (int i = 0; i < mcc.length; i++) {
		if (mcc[i] != null) {
		    mcc[i].dispose();
		}
	    }
	}

	if (mediaSource != null) {
	    for (int i = 0; i < mediaSource.length; i++) {
		if (mediaSource[i] != null) {
		    mediaSource[i].disconnect();
		}
	    }
	}

	if (parser != null)
	    parser.dispose();

    }

    public void sendCacheControlEvent() {
	long progress = 0L;
	progress = mcc[0].getContentProgress();
	sendEvent(new CachingControlEvent(this, mcc[0], progress));

    }

    public void setProgressControl(ProgressControl pc) {
	progressControl = pc;
	if (pc != null)
	    bitRateControl = pc.getBitRate();
    }

    private synchronized void updateBitRate() {
	if (bitRateControl != null) {
	    if (bitRateIntervalCounter == 0) {
		bitRateIntervalCounter = System.currentTimeMillis();
		bitRateControl.setValue(JMFI18N
			.getResource("moviesourcenode.0_kbps"));
		return;
	    }
	    long currentTime = System.currentTimeMillis();
	    long diff = currentTime - bitRateIntervalCounter;

	    if (diff >= updateInterval) {
		kbps = (int) ((float) bitsRead / diff * (1000f / 1024f));
		bitRateControl.setValue(kbps
			+ JMFI18N.getResource("moviesourcenode.kbps"));
		bitRateIntervalCounter = currentTime;
		bitsRead = 0;
	    }
	}
    }

    public void PRINT_DEBUG_MSG(String str) {
	if (DEBUG)
	    Log.e("EkhoTV", str);
    }

    // ////////////////////////////////////////////////////////////////////////
    // INNER CLASSES

    //
    // A thread to read from the MovieSourceNode.
    //
    class mAudioTimer extends LoopThread {

	MovieSourceNode movieSourceNode;

	mAudioTimer(MovieSourceNode node) {
	    super();
	    this.pause();
	    movieSourceNode = node;
	    useAudioPriority();
	    start();
	}

	public boolean process() {
	    movieSourceNode.processAudio();
	    return true;
	}
    }

    //
    // A thread to read from the MovieSourceNode.
    //
    class mVideoTimer extends LoopThread {

	MovieSourceNode movieSourceNode;

	mVideoTimer(MovieSourceNode node) {
	    super();
	    this.pause();
	    movieSourceNode = node;
	    useVideoPriority();
	    start();
	}

	public boolean process() {
	    movieSourceNode.processVideo();
	    return true;
	}
    }

    class mAudioOut implements OutputConnectable {

	protected MovieSourceNode node;
	protected Format formats[];
	protected Format format;
	protected InputConnectable nextIn;

	public mAudioOut(MovieSourceNode n) {
	    node = n;
	}

	public Format[] listFormats() {
	    return formats;
	}

	public void setFormat(Format f) {
	}

	public Format getFormat() {
	    return format;
	}

	public InputConnectable connectedTo() {
	    return nextIn;
	}

	public MediaProcessor getMediaProcessor() {
	    return node;
	}

	public void connectTo(InputConnectable port) {
	    node.connectAudio(port);
	    port.connectTo(this);
	    nextIn = port;
	}
    }

    class mVideoOut implements OutputConnectable {

	protected MovieSourceNode node;
	protected Format formats[];
	protected Format format;
	protected InputConnectable nextIn;

	public mVideoOut(MovieSourceNode n) {
	    node = n;
	}

	public Format[] listFormats() {
	    return formats;
	}

	public void setFormat(Format f) {
	}

	public Format getFormat() {
	    return format;
	}

	public InputConnectable connectedTo() {
	    return nextIn;
	}

	public MediaProcessor getMediaProcessor() {
	    return node;
	}

	public void connectTo(InputConnectable port) {
	    node.connectVideo(port);
	    nextIn = port;
	}
    }

    class mPullDataSource extends com.sun.tv.media.protocol.MediaPullDataSource {
	javax.media.protocol.PullDataSource s;

	public mPullDataSource(javax.media.protocol.PullDataSource s) {
	    this.s = s;
	}

	public Object clone() {
	    DataSource dupe = new mPullDataSource(s);
	    dupe.setLocator(getLocator());
	    return dupe;
	}

	public void connect() throws IOException {
	    // Log.e("EkhoTV", "In mPullDataSource: connect: " + s);
	    s.connect();

	    srcStreams = s.getStreams();
	    // Log.e("EkhoTV", "srcStreams is " + srcStreams);
	    // Log.e("EkhoTV", "mPullDataSource: connect: this is " + this);
	    // Log.e("EkhoTV", "srcStreams[0] is " + srcStreams[0]);
	    connected = true;
	}

	public void disconnect() {
	    s.disconnect();
	    connected = false;
	}

	public void start() throws IOException {
	    s.start();
	}

	public void stop() {
	    try {
		s.stop();
	    } catch (IOException e) {
	    }
	}

	public String getContentType() {
	    // Log.e("EkhoTV", "In mPullDataSource: getContentType " +
	    // getCorrectedContentType(s.getContentType()));
	    return getCorrectedContentType(s.getContentType());
	}

	public PullSourceStream[] getStreams() {
	    return s.getStreams();
	}

	public MediaPullSourceStream createPullSourceStream(InputStream is,
		ContentDescriptor type, URL url, long length) {
	    return null;
	}
    }
}
