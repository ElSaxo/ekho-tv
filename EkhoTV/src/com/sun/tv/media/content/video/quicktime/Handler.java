/*
 * @(#)Handler.java	1.7 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.content.video.quicktime;

import java.io.IOException;

import javax.media.ControllerEvent;
import javax.media.GainChangeEvent;
import javax.media.IncompatibleSourceException;
import javax.media.IncompatibleTimeBaseException;
import javax.media.Time;
import javax.media.TimeBase;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.Seekable;

import nl.ekholabs.ekhotv.awt.Dimension;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import com.sun.tv.media.InputConnectable;
import com.sun.tv.media.MediaController;
import com.sun.tv.media.MediaDecoder;
import com.sun.tv.media.MediaFilter;
import com.sun.tv.media.MediaPlayer;
import com.sun.tv.media.OutputConnectable;
import com.sun.tv.media.SizeChangeEvent;
import com.sun.tv.media.format.video.YUVFormat;
import com.sun.tv.media.protocol.reliable.MovieSourceNode;
import com.sun.tv.media.renderer.audio.AudioRenderer;
import com.sun.tv.media.renderer.video.VideoRenderer;

/**
 * A player to playback .mov files. It consists of a MovieSourceNode, codec
 * node, audio and video renderers.
 */
public class Handler extends MediaPlayer {

    private View playbackPanel = null;
    private MovieSourceNode movieSourceNode = null;
    private MediaFilter aCodecNode;
    private MediaFilter vCodecNode;
    private VideoRenderer videoRenderer = null;
    private AudioRenderer audioRenderer = null;
    private boolean nodesHaveBeenCreated = false;
    private TimeBase masterTimeBase = null;
    private float scale;
    private int width, height;
    private View userRequestedComponent;
    private Rect userRequestedBounds;
    private final static int AUDIO_TRACK = 1;
    private final static int VIDEO_TRACK = 2;

    public Handler() {
	super();
	scale = (float) 1.0;
    }

    protected void doClose() {
	if (playbackPanel != null) {
	    // ((DefaultControlPanel)playbackPanel).dispose();
	    playbackPanel = null;
	}

	// 4389657. Stop the video/audio timer threads
	if (movieSourceNode != null) {
	    movieSourceNode.doClose();
	    movieSourceNode.doDispose();
	}

	// 6355228. Close renders also.
	if (videoRenderer != null)
	    videoRenderer.close();
	// videoRenderer.doClose();
	if (audioRenderer != null)
	    audioRenderer.close();
	// audioRenderer.doClose();

	aCodecNode = null;
	vCodecNode = null;
    }

    protected TimeBase getMasterTimeBase() {
	return masterTimeBase;
    }

    protected void allocBuffers() {
	audioRenderer.allocBuffers(movieSourceNode.getMaxSoundSize(), 2);
	// Allocate Video Buffer.
	videoRenderer.allocBuffers(movieSourceNode.getMaxVideoSize(), 1);
	videoRenderer.setSize(movieSourceNode.getWidth(),
		movieSourceNode.getHeight());
    }

    /**
     * Set the DataSource that provides the media for this player. Handler only
     * supports PullDataSource by default.
     * 
     * @param source
     *            of media for this player.
     * @exception IOException
     *                thrown when an i/o error occurs in reading information
     *                from the data source.
     * @exception IncompatibleSourceException
     *                thrown if the Player can't use this source.
     */
    public void setSource(javax.media.protocol.DataSource source)
	    throws IOException, IncompatibleSourceException {

	super.setSource(source); // throws exception if not PullDataSource
	PullDataSource pds = (PullDataSource) source;
	PullSourceStream[] pss = pds.getStreams();

	// QuickTime format requires that streams be seekable.
	// If they are not seekable, IncompatibleSourceException will be thrown
	for (int i = 0; i < pss.length; i++) {
	    if (!((pss[i] instanceof Seekable) && (((Seekable) pss[i])
		    .isRandomAccess()))) {
		Log.e("EkhoTV", this + ": Stream not seekable");
		throw new IncompatibleSourceException(this
			+ ": No Seekable stream from  " + source);

	    }
	}
    }

    protected boolean createNodes() {
	javax.media.protocol.DataSource source;

	if (nodesHaveBeenCreated)
	    return true;

	if ((source = getSource()) == null) {
	    return false;
	}

	// String protocol = (source.getLocator()).getProtocol();

	// Create all the nodes
	// Create MovieSourceNode (1 of 3)
	try {
	    movieSourceNode = new MovieSourceNode(getSource());
	} catch (IOException e) {
	    System.err.println("QTPlayer: " + e.getMessage()
		    + ": Unable to create MovieSourceNode");
	    movieSourceNode = null;
	    return false;
	}
	// movieSourceNode.setProgressControl(progressControl);
	// movieSourceNode.setRegionControl(regionControl);

	// Create AudioRenderer Node (2 of 3)
	audioRenderer = new AudioRenderer();

	// Create the video renderer. (3 of 3)
	videoRenderer = new VideoRenderer();
	if (userRequestedComponent != null) {
	    videoRenderer.setVisualComponent(userRequestedComponent,
		    userRequestedBounds);
	}

	// Add all the nodes in this player
	addNode(videoRenderer);
	addNode(audioRenderer, true); // Audio optional
	addNode(movieSourceNode);

	nodesHaveBeenCreated = true;
	addControls(audioRenderer.getControls());
	addControls(videoRenderer.getControls());
	// videoRenderer.setFrameRateControl(frameRate);
	return true;
    }

    protected boolean connectNodes() {

	OutputConnectable oc;
	InputConnectable ic;

	if (movieSourceNode.getAudioEnable()) {
	    if ((aCodecNode = getCodecNode(AUDIO_TRACK)) != null) {
		oc = movieSourceNode.getOutputPort("AudioOut");
		ic = aCodecNode.getInputPort("AudioIn");

		if (oc == null || ic == null) {
		    System.err
			    .println("QTPlayer:: Failed to connect: no ic or oc");
		    return false;
		}
		oc.connectTo(ic);

		oc = aCodecNode.getOutputPort("AudioOut");
		ic = audioRenderer.getInputPort("AudioIn");

		if (oc == null || ic == null) {
		    System.err
			    .println("QTPlayer:: Failed to connect: no ic or oc");
		    return false;
		}
		oc.connectTo(ic);
	    } else {
		oc = movieSourceNode.getOutputPort("AudioOut");
		ic = audioRenderer.getInputPort("AudioIn");

		if (oc == null || ic == null) {
		    System.err
			    .println("QTPlayer:: Failed to connect: no ic or oc");
		    return false;
		}
		oc.connectTo(ic);
	    }

	    // Allocate Audio Buffer
	    audioRenderer.allocBuffers(movieSourceNode.getMaxSoundSize(), 2);
	} else {
	    //
	    // We know there is no audio track after realize the source node
	    // remove the audioRenderer node.
	    removeNode(audioRenderer);
	    audioRenderer.close();
	}

	if (movieSourceNode.getVideoEnable()) {
	    // Create the codec node.
	    if ((vCodecNode = getCodecNode(VIDEO_TRACK)) == null) {
		System.err.println("Unable to create codec for "
			+ movieSourceNode.getVCodec());
		return false;
	    }

	    oc = movieSourceNode.getOutputPort("VideoOut");
	    ic = vCodecNode.getInputPort("VideoIn");

	    if (oc == null || ic == null) {
		// This would be really weird!
		System.err.println("QTPlayer:: Failed to connect: no ic or oc");
		return false;
	    }
	    oc.connectTo(ic);
	    ic.connectTo(oc);
	    oc = vCodecNode.getOutputPort("VideoOut");
	    ic = videoRenderer.getInputPort("VideoIn");

	    if (oc == null || ic == null) {
		System.err.println("QTPlayer:: Failed to connect: no ic or oc");
		return false;
	    }
	    oc.connectTo(ic);
	    // Allocate Video Buffer.
	    // videoRenderer.allocBuffers(movieSourceNode.getMaxVideoSize(), 1);
	    int w = movieSourceNode.getWidth();
	    int h = movieSourceNode.getHeight();
	    // if ((((MediaDecoder)vCodecNode).getCodec() instanceof
	    // com.sun.tv.media.codec.video.raw.Codec)||
	    // (((MediaDecoder)vCodecNode).getCodec() instanceof
	    // com.sun.tv.media.codec.video.rle.Codec)){
	    // videoRenderer.setInputFormat(((MediaDecoder)vCodecNode).getCodec().getOutputFormat());
	    // /* new RGBFormat(w, h, w*h*4,4,
	    // 0x000000FF,
	    // 0x0000FF00,
	    // 0x00FF0000));*/
	    // }
	    // else {
	    videoRenderer.setInputFormat(new YUVFormat(w, h, w * h * 3, 4,
		    YUVFormat.YUV411));
	    // }
	    width = w;
	    height = h;
	    scale = (float) 1.0;
	    videoRenderer.setSize(w, h);

	} else {
	    //
	    // We know there is no video track after realize the source node
	    // remove the videoRenderer node.
	    removeNode(videoRenderer);
	    videoRenderer.close();
	}

	// Specify the Master TimeBase
	if (movieSourceNode.getAudioEnable())
	    masterTimeBase = audioRenderer.getAudioTimeBase();
	else
	    masterTimeBase = systemTimeBase;

	setMediaLength(movieSourceNode.getDuration().getNanoseconds());

	return true;
    }

    protected boolean audioEnabled() {
	return (movieSourceNode != null ? movieSourceNode.getAudioEnable()
		: false);
    }

    protected boolean videoEnabled() {
	return (movieSourceNode != null ? movieSourceNode.getVideoEnable()
		: false);
    }

    // Handle audio device busy case
    // If this method returns false, then prefetch will fail
    // TODO: choose a more appropriate name for this method
    protected boolean deviceBusy(MediaController mc) {
	// Log.e("EkhoTV", "In audioDeviceBusy: " + this + ": " + mc);

	if (mc instanceof AudioRenderer) {
	    // Audio device busy
	    // See if video is enabled
	    if (!movieSourceNode.getVideoEnable()) {
		// audio device is busy and no video track
		// Nothing can be done. Return false
		System.out
			.println("Handler: deviceBusy: audio device busy and no video track. preftech fails");
		return false;
	    }
	    movieSourceNode.disableAudio();

	    // Audio device is busy. Setting timebase to systemTimeBase.
	    masterTimeBase = systemTimeBase;
	    try {
		slaveToMasterTimeBase(systemTimeBase);
	    } catch (IncompatibleTimeBaseException e) {
		Log.e("EkhoTV", e.getMessage());
		return false; // Correct?
	    }
	    return true;
	} else {
	    return true;
	}
    }

    public void flush() {
	// Initiate the flush from the source node.
	movieSourceNode.flush();
    }

    /**
     * Get From the MovieSourceNode the codec type and instantiate the correct
     * codec to decode the stream. Return null if no matching codec is found.
     */
    private MediaFilter getCodecNode(int type) {
	MediaDecoder decoder;

	if (type == VIDEO_TRACK) {
	    decoder = new com.sun.tv.media.codec.video.DefaultDecoder();
	    if (decoder.supports(movieSourceNode.getVideoFormat()))
		return decoder;
	    else {
		System.err.println("Unsupported codec: "
			+ movieSourceNode.getVideoFormat().getCodec());
		return null;
	    }
	} else if (type == AUDIO_TRACK) {
	    // System.err.println("codec = " +
	    // movieSourceNode.getAudioFormat().getCodec());
	    decoder = new com.sun.tv.media.codec.audio.DefaultDecoder();
	    if (decoder.supports(movieSourceNode.getAudioFormat()))
		return decoder;
	    else
		return null;
	}

	return null;
    }

    public void setVisualComponent(View c, Rect bounds) {
	super.setVisualComponent(c, bounds);
	userRequestedComponent = c;
	userRequestedBounds = bounds;
    }

    public View getVisualComponent() {
	super.getVisualComponent();
	if (videoEnabled())
	    return videoRenderer.getVisualComponent();
	else
	    return null;
    }

    public View getControlPanelComponent() {
	return super.getControlPanelComponent();
	// if (playbackPanel == null)
	// playbackPanel = new DefaultControlPanel( this );
	// return playbackPanel;
    }

    public void seek(float loc) {
	setMediaTime(new Time((long) (loc * getDuration().getNanoseconds())));
    }

    /***
     * Note: no longer use updateProgress to update the playbackPanel. public
     * void updateProgress() { if (movieSourceNode != null && playbackPanel !=
     * null) { long t1 = movieSourceNode.getMediaNanoseconds() / 1000000; long
     * t2 = movieSourceNode.getDuration().getNanoseconds() / 1000000; double p =
     * (double)t1/(double)t2; // playbackPanel.setProgress((float)p);
     * //playbackPanel.setStatus(videoRenderer.getFrameRate() + " fps"); } }
     ****/

    public void doZoom() {
	int newWidth = (int) (width * scale);
	int newHeight = (int) (height * scale);
	SizeChangeEvent e = new SizeChangeEvent(this, newWidth, newHeight,
		scale);
	sendEvent(e);
	videoRenderer.setSize(newWidth, newHeight);
	if (playbackPanel != null) {
	    playbackPanel.setMinimumWidth(newWidth);
	    playbackPanel.setMinimumHeight(0);
	}
    }

    public void zoomIn() {
	if (scale < (float) 2.0) {
	    scale *= (float) 2.0;
	    doZoom();
	}
    }

    public void zoomOut() {
	if (scale > (float) 0.25) {
	    scale /= (float) 2.0;
	    doZoom();
	}
    }

    public void doProcessEvent(ControllerEvent evt) {
	if (evt instanceof SizeChangeEvent) {
	    scale = ((SizeChangeEvent) evt).getScale();
	    sendEvent(evt);
	    SizeChangeEvent e = (SizeChangeEvent) evt;
	    width = e.getWidth();
	    height = e.getHeight();
	    videoRenderer.setSize(width, height);
	    if (playbackPanel != null) {
		playbackPanel.setMinimumWidth(width);
		playbackPanel.setMinimumHeight(0);
	    }
	}
    }

    public void gainChange(GainChangeEvent gce) {
	if (audioRenderer != null)
	    audioRenderer.setGain(gce.getLevel());
    }

    public void muteChange(boolean m) {
	audioRenderer.setMute(m);
    }

    public Dimension getPreferredSize() {
	if (playbackPanel != null) {
	    Dimension d = new Dimension(playbackPanel.getWidth(),
		    playbackPanel.getHeight());
	    return d;
	} else {
	    return new Dimension(320, 240);
	}
    }
}
