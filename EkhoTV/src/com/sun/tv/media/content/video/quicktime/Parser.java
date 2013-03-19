/*
 * @(#)Parser.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.content.video.quicktime;

import java.io.IOException;

import android.util.Log;

import com.sun.tv.media.BadHeaderException;
import com.sun.tv.media.MediaFormat;
import com.sun.tv.media.Track;
import com.sun.tv.media.content.video.TChunkList;
import com.sun.tv.media.format.audio.AudioFormat;
import com.sun.tv.media.format.video.IndexColorFormat;
import com.sun.tv.media.format.video.VideoFormat;

public class Parser extends com.sun.tv.media.content.video.ContainerParser {

    public boolean DEBUG = false;

    static final int TIME_OFFSET = 8; // Creation + Modification Time
    static final int VERSION_PLUS_FLAG = 4; // Version + flags
    static final int COMP_TYPE = 4; // Component type
    static final int ATOM_PLUS_SIZE = 8;
    static final int SAMPLE_SIZE = 4;

    static final int RESERVE2_OFFSET = 2; // 2bytes
    static final int RESERVE4_OFFSET = 4; // 4 bytes
    static final int RESERVE8_OFFSET = 8; // 8 bytes
    static final int RESERVE10_OFFSET = 10; // 10 bytes

    static final int DESC4 = 4;
    static final int DESC46 = 46;

    static final int minAtomSize = 8; // Size + Type = 8 bytes
    static final String MDAT = "mdat"; // Movie data atom
    static final String MovieAID = "moov"; // Movie atom
    static final String MovieHeaderAID = "mvhd"; // Movie header atom
    static final String TrackAID = "trak"; // Track atom
    static final String TrackHeaderAID = "tkhd"; // Track header atom
    static final String MediaHeaderAID = "mdhd"; // Media header atom
    static final String HandlerAID = "hdlr"; // Handler reference atom
    static final String MediaInfoAID = "minf"; // Media information atom
    static final String MediaAID = "mdia"; // Media atom
    static final String SampleTableAID = "stbl"; // Sample table atom
    static final String STSampleDescAID = "stsd"; // Sample description atom
    static final String STTimeToSampAID = "stts"; // Time-to-sample atom
    static final String STSyncSampleAID = "stss"; // Sync sample atom
    static final String STSampleToChunkAID = "stsc"; // Sample-to-chunk atom
    static final String STSampleSizeAID = "stsz"; // Sample size atom
    static final String STChunkOffsetAID = "stco"; // Chunk offset atom
    static final String STShadowSyncAID = "stsh"; // Shadow sync atom
    static final String DataInfoAID = "dinf"; // Data information atom
    static final String MHD = "mhd"; // vmhd/smhd atom
    static final String ClipAID = "clip"; // Clipping atom
    static final String MatteAID = "matt"; // Track matte atom
    static final String TrackRefAID = "tref"; // QT2.5
    static final String TrackLoadAID = "load"; // QT2.5
    static final String TrackMapAID = "imap"; // QT2.5

    static final String EditsAID = "edts"; // Edit atom
    static final String EditsListAID = "elst"; // Edit list atom
    static final String CtabAID = "ctab"; // Color map atom
    static final String UDTA = "udta"; // User-defined atom
    static final String SOUND = "soun"; // sound component
    static final String VIDEO = "vide"; // video component

    // VideoFormat Variables (Frame info)
    private MediaFormat format;
    private TChunkList plst;
    private int iFrameTime; // Time for 1 frame in usec
    private int cbSoundWorth; // number of bytes of 1 second of second
    private int iVideoTimeScale; // Video track time scale
    private int iEnable; // Track Header Flag
    private String iTrackCODEC; // Track compressor type
    private int iTrackPixelDepth; // Track pixel depth
    private int iTrackColorTableID; // Track color table id
    private String iTrackType; // Track type
    private int iTrackTimeScale; // Track time scale
    private int iTrackDuration; // Track duration
    private int iTrackStart; // Track start
    private int iTrackChannels; // Track Channels
    private int iTrackSampleSize; // Possible sound characteristics
    private int iTrackSampleRate; // Possible sound sample rate
    private int iTrackTimeCount; // Track time to sample count
    private int piTrackTimeStart[]; // Track time to sample start
    private int iTrackSyncCount; // Track sync sample count
    private int piTrackSyncStart[]; // Track sync sample start
    private int iTrackChunkCount; // Track chunk count
    private int piTrackChunkStart[]; // Track chunk start
    private int iTrackSizeCount; // Track sample size count
    private int piTrackSizeStart[]; // Track sample size start
    private int iTrackOffsetCount; // Track chunk offset count
    private int piTrackOffsetStart[]; // Track chunk offset start

    private Runtime rt;
    private boolean visitedVideoTrack = false;
    private boolean bEditListOn = false; // Don't deal with Edit list.

    private MVHD mvhdInfo;
    private TKHD tkhdInfo;
    private CTAB ctabInfo = null;

    /**
     * Parser
     */
    public Parser() {
	super();
	format = null;
    }

    /*
     * Note: read header only in doRealize. If these codes is placed in
     * constructor, everytime MediaSource.setPosition() seeks back by reopen the
     * url will re-read the header information. To avoid redundancy work, call
     * readAndSetHeader in doRealize() only.
     */
    public void readHeader() throws BadHeaderException, IOException {

	int size = 0, size1 = 0, size2 = 0;
	String atom = null, atom1 = null, atom2 = null;
	int minfSize, availResSize, trackSize;
	int remainSize;
	if (DEBUG)
	    rt = Runtime.getRuntime();

	reReadStream();

	size = readInt();
	atom = readString();
	PRINT_DEBUG_MSG("size = " + size);
	PRINT_DEBUG_MSG("atom = " + atom);

	if (!(atom.equals(MDAT) || atom.equals(MovieAID))) {

	    /*
	     * Search for QT moov chunk in case "mdat" atom is missing.
	     */
	    if (size < 4)
		throw new BadHeaderException("Invalid header");

	    skipBytes(size - 8);
	    size = readInt();
	    atom = readString();
	    PRINT_DEBUG_MSG("search atom = " + atom);
	    if (!atom.equals(MovieAID))
		throw new BadHeaderException("moov atom is not found.");
	}
	PRINT_DEBUG_MSG("size = " + size);
	PRINT_DEBUG_MSG("atom = " + atom);

	/*
	 * When the mdat atom is exist, the resource is located at the end of
	 * the media file.
	 */
	streamable = true;
	while (atom.equals(MDAT)) {
	    // streamable = false;
	    skipWholeAtom(size);
	    size = readInt();
	    atom = readString();
	    PRINT_DEBUG_MSG("size = " + size);
	    PRINT_DEBUG_MSG("atom = " + atom);
	}

	// movie 'moov' atom
	// moov atom contains the movie header atom which defines the
	// time scale and duration information for the entire movie.
	if (atom.equals(MovieAID)) {
	    // Initialize the availResSize to the size of the
	    // whole resource atom.
	    availResSize = size;
	    // Movie header atom (mvhd)
	    size = readInt();
	    atom = readString();
	    if (atom.equals(MovieHeaderAID)) {
		availResSize -= size; // Read the mvhd atom
		parseMvhd(size);
	    }
	} else
	    throw new BadHeaderException("non-flatten movie");

	//
	// 'trak' - Track Atom
	// There are one or more track atoms; therefore, use a for loop
	// to get all information.
	while (availResSize > minAtomSize) {
	    PRINT_DEBUG_MSG("<==== Track Begin ");
	    //
	    // Ignore clipping atom and user defined atom
	    size = readInt();
	    atom = readString();
	    while (atom.equals(UDTA) || atom.equals(ClipAID)
		    || atom.equals(CtabAID) || atom.equals(TrackLoadAID)
		    || atom.equals(MatteAID) || atom.equals(EditsAID)
		    || atom.equals(TrackRefAID) || atom.equals(TrackMapAID)) {

		if (atom.equals(CtabAID))
		    parseCtab(size);
		else
		    skipBytes(size - 8);

		availResSize -= size; // Read udta/clip atoms
		PRINT_DEBUG_MSG("Available resource size = " + availResSize);
		PRINT_DEBUG_MSG(atom + " atom; " + size + " bytes.");
		size = readInt();
		atom = readString();
	    }

	    PRINT_DEBUG_MSG(atom + " atom; " + size + " bytes");

	    // All atoms have been read if AvailResSize <= minimum atom size.
	    if (availResSize <= minAtomSize)
		break;
	    //
	    // Read Track Atom (trak)
	    if (!atom.equals(TrackAID)) {
		throw new BadHeaderException("Unexpected atom: " + atom);
	    }

	    /*
	     * Keep track the size of the track atom. Track atom contains Track
	     * Header atom, and media atom which are required atoms. Track
	     * clipping atom, track matte atom and edit atom are optional atom.
	     */
	    trackSize = size;
	    availResSize -= trackSize; // Read track atom

	    PRINT_DEBUG_MSG("Available resource size = " + availResSize);
	    PRINT_DEBUG_MSG(atom + " atom " + size + " bytes");

	    //
	    // Read Track Header Atom (tkhd)
	    size = readInt();
	    atom = readString();
	    PRINT_DEBUG_MSG(atom + " atom " + size + " bytes");
	    if (atom.equals(TrackHeaderAID)) {
		parseTkhd(size);
		trackSize -= size;
	    } else {
		throw new BadHeaderException("Unexpected atom: " + atom);
	    }

	    //
	    // Read Track fields.
	    // iTrackEditCount = 0;
	    iTrackTimeCount = 0;
	    iTrackSyncCount = 0;
	    iTrackChunkCount = 0;
	    iTrackSizeCount = 0;
	    iTrackOffsetCount = 0;

	    //
	    // Search the mdia atom ( Media Atom )
	    // Note: Ignore clip/matt/edts atom if they exist.
	    while (true) {
		size = readInt();
		atom = readString();
		PRINT_DEBUG_MSG(atom + " atom; " + size + " bytes.");
		trackSize -= size;
		// MediaAID, mdia, is a required atom.
		if (atom.equals(MediaAID)) {
		    break;
		} else if (atom.equals(ClipAID) || atom.equals(MatteAID)
			|| atom.equals(EditsAID) || atom.equals(TrackRefAID)
			|| atom.equals(TrackLoadAID) || atom.equals(CtabAID)
			|| atom.equals(TrackMapAID) || atom.equals(UDTA)) {
		    // Ignore clip and matt atoms.
		    // They are optional atoms.
		    skipWholeAtom(size);
		} else if (atom.equals(CtabAID)) {
		    parseCtab(size);
		} else {
		    throw new BadHeaderException("Unexcepted atom: " + atom);
		}
		// If trackSize is 0 and mdia is not found, this media file
		// is not supported.
		if (trackSize <= 0)
		    throw new BadHeaderException("Media atom is not found.");
	    }
	    //
	    // Read Media Header Atom (mdhd)
	    size = readInt();
	    atom = readString();
	    PRINT_DEBUG_MSG(atom + " atom; " + size + " bytes.");
	    if (atom.equals(MediaHeaderAID))
		parseMdhd(size);
	    else
		throw new BadHeaderException("Unexpected atom: " + atom);
	    //
	    // Read Media Handler reference Atom (hdlr)
	    size = readInt();
	    atom = readString();
	    PRINT_DEBUG_MSG(atom + " atom; " + size + " bytes.");
	    if (atom.equals(HandlerAID))
		parseMdiaHdlr(size);
	    else
		throw new BadHeaderException("Unexpected atom: " + atom);

	    //
	    // Video Media Information Atom (container)
	    minfSize = readInt();
	    atom = readString();
	    PRINT_DEBUG_MSG(atom + " atom; " + minfSize + " bytes");
	    if (!atom.equals(MediaInfoAID))
		throw new BadHeaderException("Unexpected atom: " + atom);
	    //
	    // Video Media Information Header Atom
	    // Note: Skip the details fields in vmhd/smhd for now.
	    size1 = readInt();
	    atom1 = readString();
	    PRINT_DEBUG_MSG("Media Information header : " + atom1 + " atom; "
		    + size1 + " bytes");
	    if (atom1.endsWith(MHD))
		skipWholeAtom(size1);
	    else
		throw new BadHeaderException("Unexpected atom: " + atom1);

	    // remainSize is equal to the total size of the
	    // remaining atom, video media information atom called minf.
	    // Data Handler Reference Atom
	    // Note: Skip the data handler reference atom.
	    remainSize = minfSize - ATOM_PLUS_SIZE - size1;
	    size1 = readInt();
	    atom1 = readString();
	    PRINT_DEBUG_MSG(atom1 + " atom; " + size1 + " bytes.");
	    PRINT_DEBUG_MSG("RemainSize = " + remainSize + " bytes");

	    if (atom1.equals(HandlerAID)) {
		skipWholeAtom(size1);
		remainSize -= size1;
	    } else
		throw new BadHeaderException("Unexcepted atom: " + atom1);

	    // Data information atom/sample table atom is not a
	    // required atom. If remainSize is 0, it implies
	    // there is no optional atom exist.
	    while (remainSize != 0) {
		PRINT_DEBUG_MSG("remainSize = " + remainSize);
		size1 = readInt();
		atom1 = readString();
		PRINT_DEBUG_MSG(atom1 + " atom; " + size1 + " bytes.");
		// Data Information Atom
		// Note: Skip the details in dinf for now.
		if (atom1.equals(DataInfoAID)) {
		    skipWholeAtom(size1);
		} else if (atom1.equals(SampleTableAID)) {
		    parseSampleTable(size1, iTrackType);
		} else {
		    skipWholeAtom(size1);
		}

		remainSize -= size1;
	    }
	    PRINT_DEBUG_MSG("====> End of Track");
	    BuildPlayListFromTrack();
	    reInitialize();
	}

	// * Determine whether it is streamable

	interleaved = true;
	if ((trackList != null) && (trackList.length == 2)) {
	    TChunkList t0, t1;
	    long min0, min1, max0 = 0, max1 = 0;

	    t0 = trackList[0].getList();
	    t1 = trackList[1].getList();

	    if (t0 != null && t1 != null) {
		min0 = t0.GetStartEntry(0);
		min1 = t1.GetStartEntry(0);
		if (t0.GetNumEntries() > 0)
		    max0 = t0.GetStartEntry(t0.GetNumEntries() - 1);
		if (t1.GetNumEntries() > 0)
		    max1 = t1.GetStartEntry(t1.GetNumEntries() - 1);

		if (min0 < min1) {
		    if (max0 < min1)
			interleaved = false;
		} else {
		    if (max1 < min0)
			interleaved = false;
		}
	    }
	}
	streamable = streamable && interleaved;
    }

    public void PRINT_DEBUG_MSG(String str) {
	if (DEBUG)
	    System.err.println(str);
    }

    private void reInitialize() {
	iTrackCODEC = "unknown"; // Track compressor type
	iTrackPixelDepth = -1; // Track pixel depth
	iTrackColorTableID = -1; // Track color table id
	iTrackType = ""; // Track type
	iTrackTimeScale = 0; // Track time scale
	iTrackDuration = 0; // Track duration
	iTrackStart = 0; // Track start
	iTrackChannels = 0; // Track Channels
	iTrackSampleSize = 0; // Possible sound characteristics
	iTrackSampleRate = 0; // Possible sound sample rate
	iTrackTimeCount = 0; // Track time to sample count
	piTrackTimeStart = null; // Track time to sample start
	iTrackSyncCount = 0; // Track sync sample count
	piTrackSyncStart = null; // Track sync sample start
	iTrackChunkCount = 0; // Track chunk count
	piTrackChunkStart = null; // Track chunk start
	iTrackSizeCount = 0; // Track sample size count
	piTrackSizeStart = null; // Track sample size start
	iTrackOffsetCount = 0; // Track chunk offset count
	piTrackOffsetStart = null; // Track chunk offset start
	format = null;
    }

    // Private helper function
    private void BuildPlayListFromTrack() throws BadHeaderException {

	PRINT_DEBUG_MSG("$$$$$ iTrackType = " + iTrackType);

	if (iTrackType.equals(VIDEO)) {
	    if (visitedVideoTrack)
		return;
	    BuildVideoPlayList();
	    if (format instanceof VideoFormat) {
		((VideoFormat) format).setColorTableID(iTrackColorTableID);
		((VideoFormat) format).setDuration(getDuration());
	    }
	    visitedVideoTrack = true;
	    Track t = new Track(plst);
	    addTrack(t);
	    t.setFormat(format);
	} else if (iTrackType.equals(SOUND)) {
	    BuildSoundPlayList();
	    if (format != null) {
		((AudioFormat) format).setDuration(getDuration());
		Track t = new Track(plst);
		addTrack(t);

		// find the max sound size
		int max = 0;
		for (int i = 0; i < plst.GetNumEntries(); i++) {
		    int cksize = plst.GetEndEntry(i) - plst.GetStartEntry(i);
		    if (max < cksize)
			max = cksize;
		}
		((AudioFormat) format).setFrameSize(max);
		t.setFormat(format);
	    }
	} else {
	    System.err.println(iTrackType + " media track is not supported.");
	}
    }

    private void BuildVideoPlayList() throws BadHeaderException {

	int width, height;
	int iFrameNumber;
	int iPixelDepth;
	int iVideoMaxSize; // Max video chunk size

	int lApplies[], numChunk;
	int lSamples, lOffset = 0, lSize, lKF, lTime;
	int iDuration, lFrameTime;
	int lEntries = 0;

	String type = TChunkList.VIDEO_TYPE;

	/***
	 * NOTE: Don't deal with edit list for fast performance. // Note: These
	 * are for Edit list. // int lEditDuration = 0; // int lEditStart = 0;
	 * // int lEditEnd = 0; // int lEditRate, lEditTime = 0; // int
	 * iNumEdits = iTrackEditCount;
	 ***/

	int iNumOffsets = iTrackOffsetCount;
	int iNumSizes = iTrackSizeCount;
	int iNumSyncs = iTrackSyncCount;
	int iNumTimes = iTrackTimeCount;

	int numEntries = 0;
	int samplePerChunk[];

	width = tkhdInfo.iTrackWidth;
	height = tkhdInfo.iTrackHeight;
	iPixelDepth = iTrackPixelDepth;
	iVideoTimeScale = iTrackTimeScale;

	PRINT_DEBUG_MSG("width = " + width);
	PRINT_DEBUG_MSG("height = " + height);
	PRINT_DEBUG_MSG("iPixelDepth = " + iPixelDepth);
	PRINT_DEBUG_MSG("iVideoTimeScale = " + iVideoTimeScale);

	if ((iTrackTimeCount == 0) || (iTrackSizeCount == 0)
		|| (iTrackOffsetCount == 0)) {
	    PRINT_DEBUG_MSG("iTrackTimeCount = " + iTrackTimeCount);
	    PRINT_DEBUG_MSG("iTrackSizeCount = " + iTrackSizeCount);
	    PRINT_DEBUG_MSG("iTrackOffsetCount = " + iTrackOffsetCount);
	    lApplies = new int[1];
	    samplePerChunk = new int[1];
	} else {
	    samplePerChunk = new int[iTrackChunkCount];
	    lApplies = new int[iTrackChunkCount];
	    for (int i = 0; i < iTrackChunkCount; i++) {
		int firstChunk = piTrackChunkStart[i * 3];
		samplePerChunk[i] = piTrackChunkStart[i * 3 + 1];
		if (i == (iTrackChunkCount - 1)) {
		    lApplies[i] = iTrackOffsetCount - firstChunk + 1;
		} else {
		    lApplies[i] = piTrackChunkStart[i * 3 + 3] - firstChunk;
		}
		numEntries += (lApplies[i] * samplePerChunk[i]);
		PRINT_DEBUG_MSG("lApplies[" + i + "] = " + lApplies[i]);
		PRINT_DEBUG_MSG("samplePerChunk[" + i + "]= "
			+ samplePerChunk[i]);
	    }
	}

	if (iTrackChunkCount == 0) {
	    numEntries = 1;
	    samplePerChunk[0] = 0;
	    lApplies[0] = 1;
	}

	PRINT_DEBUG_MSG("numEntries = " + numEntries);
	plst = new TChunkList(numEntries, type);

	int l = 0, m = 0;
	for (int i = 0; i < iTrackChunkCount; i++) {
	    for (int j = 0; j < lApplies[i]; j++) {
		lOffset = piTrackOffsetStart[m++];
		for (int k = 0; k < samplePerChunk[i]; k++) {

		    /*
		     * Note: If all the samples have the same size, the sample
		     * field of this atom indicates the size of all the samples.
		     * If this field is set to 0, then the samples have
		     * different sizes, and those sizes are stored in the sample
		     * size table. When the sample size table is not exist, the
		     * length of piTrackSizeStart array will be different from
		     * number of entries, iTrackSizeCount. It indicates all
		     * samples have same sample size.
		     */
		    if (piTrackSizeStart.length != iTrackSizeCount)
			lSize = piTrackSizeStart[0];
		    else
			lSize = piTrackSizeStart[l++];
		    plst.AppendEntry(lOffset, lOffset + lSize, 0, false);
		    lOffset += lSize;
		}
	    }
	}

	// If no sync sample atom exists, then all the samples are key frames.
	if (iNumSyncs != 0) {
	    // Do a pass over the play list to identify key frames
	    for (int i = 0; i < iNumSyncs; i++) {
		lKF = piTrackSyncStart[i];
		plst.SetKFEntry(lKF - 1, true);
	    }
	} else {
	    for (int i = 0; i < numEntries; i++)
		plst.SetKFEntry(i, true);
	}

	// Do a pass over the play list to set the track time
	lTime = iTrackStart;

	// Run over the time-to-sample table and set the track time.
	int k = 0;
	for (int j = 0; j < iNumTimes; j++) {
	    lSamples = piTrackTimeStart[2 * j];
	    iDuration = piTrackTimeStart[(2 * j) + 1];
	    for (int i = 0; i < lSamples; i++) {
		plst.SetIDEntry(k, lTime);
		if (iNumTimes > 1) {
		    plst.SetFrameTimeEntry(k,
			    MulDiv32(iDuration, 1000000, iVideoTimeScale));
		} else {
		    plst.SetFrameTimeEntry(
			    k,
			    MulDiv32(
				    mvhdInfo.iDuration,
				    1000000,
				    (mvhdInfo.iTimeScale * plst.GetNumEntries())));
		}
		k++;
		lTime += iDuration;
	    }
	}
	// Prepare to process the edit list
	lEntries = plst.GetNumEntries();

	/**
	 * NOTE: Don't parse the Editing list for faster performance.
	 ** 
	 * if (iTrackEditCount > 0) { int lChunk = 0; lEditDuration =
	 * piTrackEditStart[ lChunk * 3 ]; lEditStart = piTrackEditStart[ 3 *
	 * lChunk + 1 ]; lEditRate = piTrackEditStart[ ( 3 * lChunk + 2 ) ]; }
	 **/

	/*
	 * Loop through the chunk list to compute the iVideoMaxSize. This code
	 * was taken from -r1.16. Somehow, the latest version of the Parser has
	 * the following piece of code deleted. Cania, will you please the logic
	 * here. -ivg
	 */
	iVideoMaxSize = 0;
	int start, end, size, idx;
	if (/**
	 * NOTE: Don't deal with Edit List (iTrackEditCount == 0) ||
	 * ((iTrackEditCount == 1) && (lEditDuration == mvhdInfo.iDuration) &&
	 * (lEditStart == 0) )
	 **/
	!bEditListOn) {

	    for (idx = 0; idx < lEntries; idx++) {
		start = plst.GetStartEntry(idx);
		end = plst.GetEndEntry(idx);

		size = end - start + 10;
		// NOT NEEDED: iFrameSize += size;

		if (size >= iVideoMaxSize)
		    iVideoMaxSize = size;
	    }
	}

	// We now know even more about this movie
	iFrameNumber = plst.GetNumEntries();
	iFrameTime = MulDiv32(1000000, mvhdInfo.iDuration,
		(mvhdInfo.iTimeScale * iFrameNumber));

	PRINT_DEBUG_MSG("mvhdInfo.iDuration = " + mvhdInfo.iDuration);
	PRINT_DEBUG_MSG("iFrameNumber = " + iFrameNumber);
	PRINT_DEBUG_MSG("mvhdInfo.iTimeScale = " + mvhdInfo.iTimeScale);
	PRINT_DEBUG_MSG("1000000 * mvhdInfo.iDuration /(mvhdInfo.iTimeScale * iFrameNumber) = (iFrameTime ) "
		+ iFrameTime);

	if (format instanceof VideoFormat) {
	    ((VideoFormat) format).setFrameSize(iVideoMaxSize);
	}
    }

    private int imaSamplesToSize(int samples, int channels) {
	if (channels == 1)
	    return samples / 64 * 34;
	else if (channels == 2)
	    return samples / 64 * 68;
	else
	    return samples;
    }

    private void BuildSoundPlayList() throws BadHeaderException {

	int iFlags = AudioFormat.FLAG_SIGNED | AudioFormat.FLAG_BIGENDIAN;
	int lTime = iTrackStart;
	int numEntries = 0;
	int lOffset, lSize = 0;
	int iNumOffsets = iTrackOffsetCount;
	// We now know a lot about this movie
	int iChannels = iTrackChannels;
	int iSampleSize = iTrackSampleSize;
	String type = TChunkList.AUDIO_TYPE;

	// Calculate how many total number of entries
	// Sum of number of chunks in each entries plus sample per chunk
	// in sample-to-time table
	int iSamplesPerChunk[] = new int[iTrackChunkCount];
	int lApplies[];
	if (iTrackChunkCount > 0)
	    lApplies = new int[iTrackChunkCount];
	else
	    lApplies = new int[1];

	PRINT_DEBUG_MSG("iTrackChunkCount = " + iTrackChunkCount);

	for (int i = 0; i < iTrackChunkCount; i++) {
	    int firstChunk = piTrackChunkStart[i * 3];
	    iSamplesPerChunk[i] = piTrackChunkStart[i * 3 + 1];
	    if (i == (iTrackChunkCount - 1)) {
		lApplies[i] = iTrackOffsetCount - firstChunk + 1;
	    } else {
		lApplies[i] = piTrackChunkStart[i * 3 + 3] - firstChunk;
	    }
	    if (DEBUG) {
		System.err.println("lApplies[" + i + "] = " + lApplies[i]);
		System.err.println("iSamplesPerChunk[" + i + "]= "
			+ iSamplesPerChunk[i]);
	    }
	}
	numEntries = iTrackOffsetCount;

	if (iTrackChunkCount == 0) {
	    numEntries = 1;
	    lApplies[0] = 1;
	}

	plst = new TChunkList(iTrackOffsetCount, type);
	PRINT_DEBUG_MSG("iTrackOffsetCount = " + iTrackOffsetCount);

	int k = 0, totalApplies = lApplies[0];
	for (int i = 0; i < iTrackOffsetCount; i++) {
	    lOffset = piTrackOffsetStart[i];
	    if (iTrackCODEC.equals(AudioFormat.JAUDIO_IMA4))
		lSize = imaSamplesToSize(iSamplesPerChunk[k], iTrackChannels);
	    else if (iTrackCODEC.equals(AudioFormat.JAUDIO_GSM))
		lSize = iSamplesPerChunk[k] / 160 * 33;
	    else
		lSize = iSamplesPerChunk[k] * iChannels * iSampleSize / 8;
	    lTime += iSamplesPerChunk[k];
	    if (totalApplies == (i + 1) && (k < (iTrackChunkCount - 1))) {
		k++;
		totalApplies += lApplies[k];
	    }

	    if (DEBUG) { // DEBUG
		System.err.println("lOffset = " + lOffset + "; lSize = "
			+ lSize + "; lTime = " + lTime);
	    }
	    plst.AppendEntry(lOffset, lOffset + lSize, lTime, false);
	}
    }

    // Read Sample table elements
    // atom2 may be 'stsd', 'stts', 'stss', 'stsc',
    // 'stsz', 'stco', 'stsh'
    private void parseSampleTable(int stblSize, String mtype)
	    throws IOException {
	int rs = stblSize - ATOM_PLUS_SIZE;
	int size;
	String atom;

	while (rs > 0) {
	    size = readInt();
	    atom = readString();
	    PRINT_DEBUG_MSG("remain size, rs = " + rs);
	    PRINT_DEBUG_MSG(atom + " atom; " + size + " bytes.");

	    if (atom.equals(STSampleDescAID))
		parseStsd(size, mtype);

	    else if (atom.equals(STTimeToSampAID))
		parseStts(size);

	    else if (atom.equals(STSyncSampleAID))
		parseStss(size);

	    else if (atom.equals(STSampleToChunkAID))
		parseStsc(size);

	    else if (atom.equals(STSampleSizeAID))
		parseStsz(size);

	    else if (atom.equals(STChunkOffsetAID))
		parseStco(size);

	    else if (atom.equals(STShadowSyncAID))
		parseStsh(size);

	    else if (size > 8)
		skipBytes(size - 8);

	    rs -= size;
	}
    }

    /*
     * The movie header atom is a leaf atom, which contains time information for
     * the entire atom, such as time scale, and duration.
     */
    private void parseMvhd(int size) throws IOException {

	mvhdInfo = new MVHD();
	mvhdInfo.iVersionPlusFlag = readInt();
	mvhdInfo.iCreateTime = readInt();
	mvhdInfo.iModtime = readInt();
	mvhdInfo.iTimeScale = readInt();
	mvhdInfo.iDuration = readInt();
	mvhdInfo.iPerfRate = readInt();
	mvhdInfo.sPerfVol = readShort();
	skipBytes(RESERVE10_OFFSET);

	mvhdInfo.aMatrix = new int[9];
	for (int i = 0; i < 9; i++) {
	    mvhdInfo.aMatrix[i] = readInt();
	}
	mvhdInfo.iPrevTime = readInt();
	mvhdInfo.iPrevDuration = readInt();
	mvhdInfo.iPosterTime = readInt();
	mvhdInfo.iSelectTime = readInt();
	mvhdInfo.iSelectDuration = readInt();
	mvhdInfo.iCurrentTime = readInt();
	mvhdInfo.iNextTrackID = readInt();

	PRINT_DEBUG_MSG("mvhdInfo.iVersionPlusFlag = "
		+ mvhdInfo.iVersionPlusFlag);
	PRINT_DEBUG_MSG("mvhdInfo.iCreateTime = " + mvhdInfo.iCreateTime);
	PRINT_DEBUG_MSG("mvhdInfo.iModtime = " + mvhdInfo.iModtime);
	PRINT_DEBUG_MSG("mvhdInfo.iTimeScale = " + mvhdInfo.iTimeScale);
	PRINT_DEBUG_MSG("mvhdInfo.iDuration = " + mvhdInfo.iDuration);
	PRINT_DEBUG_MSG("mvhdInfo.iPerfRate = " + mvhdInfo.iPerfRate);
	PRINT_DEBUG_MSG("mvhdInfo.sPerfVol = " + mvhdInfo.sPerfVol);

	for (int i = 0; i < 9; i++) {
	    PRINT_DEBUG_MSG("mvhdInfo.aMatrix[" + i + "] = "
		    + mvhdInfo.aMatrix[i]);
	}
	PRINT_DEBUG_MSG("mvhdInfo.iPrevTime = " + mvhdInfo.iPrevTime);
	PRINT_DEBUG_MSG("mvhdInfo.iPrevDuration = " + mvhdInfo.iPrevDuration);
	PRINT_DEBUG_MSG("mvhdInfo.iPosterTime = " + mvhdInfo.iPosterTime);
	PRINT_DEBUG_MSG("mvhdInfo.iSelectTime = " + mvhdInfo.iSelectTime);
	PRINT_DEBUG_MSG("mvhdInfo.iSelectDuration = "
		+ mvhdInfo.iSelectDuration);
	PRINT_DEBUG_MSG("mvhdInfo.iCurrentTime = " + mvhdInfo.iCurrentTime);
	PRINT_DEBUG_MSG("mvhdInfo.iNextTrackID = " + mvhdInfo.iNextTrackID);
    }

    /*
     * The track header specifies the characteristic of a single track within a
     * movie. It includes temporal, spatial, and volume information.
     */
    private void parseTkhd(int size) throws IOException {

	tkhdInfo = new TKHD();
	tkhdInfo.iVersionPlusFlag = readInt();
	tkhdInfo.iCreateTime = readInt();
	tkhdInfo.iModtime = readInt();
	tkhdInfo.iTrackID = readInt();
	skipBytes(RESERVE4_OFFSET);
	tkhdInfo.iDuration = readInt();
	skipBytes(RESERVE8_OFFSET);
	tkhdInfo.sLayer = readShort();
	tkhdInfo.sAlternateGroup = readShort();
	tkhdInfo.sVolume = readShort();
	skipBytes(RESERVE2_OFFSET);
	tkhdInfo.aMatrix = new int[9];
	for (int i = 0; i < 9; i++)
	    tkhdInfo.aMatrix[i] = readInt();
	tkhdInfo.iTrackWidth = readInt() >> 16;
	tkhdInfo.iTrackHeight = readInt() >> 16;

	iEnable = tkhdInfo.iVersionPlusFlag & 0x0FFFFFF;

	if (false/* DEBUG */) {
	    System.err.println("mvhd atom, its size is " + size);
	    System.err.println("tkhdInfo.iVersionPlusFlag = "
		    + tkhdInfo.iVersionPlusFlag);
	    System.err
		    .println("tkhdInfo.iCreateTime = " + tkhdInfo.iCreateTime);
	    System.err.println("tkhdInfo.iModtime = " + tkhdInfo.iModtime);
	    System.err.println("tkhdInfo.iTrackID = " + tkhdInfo.iTrackID);
	    System.err.println("tkhdInfo.iDuration = " + tkhdInfo.iDuration);
	    System.err.println("tkhdInfo.sLayer = " + tkhdInfo.sLayer);
	    System.err.println("tkhdInfo.sAlternateGroup = "
		    + tkhdInfo.sAlternateGroup);
	    System.err.println("tkhdInfo.sVolume = " + tkhdInfo.sVolume);
	    for (int i = 0; i < 9; i++) {
		System.err.println("tkhdInfo.aMatrix[" + i + "] "
			+ tkhdInfo.aMatrix[i]);
	    }
	    System.err
		    .println("tkhdInfo.iTrackWidth = " + tkhdInfo.iTrackWidth);
	    System.err.println("tkhdInfo.iTrackHeight = "
		    + tkhdInfo.iTrackHeight);
	    System.err.println("iEnable = " + iEnable);
	}
    }

    /*
     * The handler reference atom specifies the component that is to interpret a
     * media's data. This component is called media handler.
     */
    private void parseMdiaHdlr(int size) throws IOException {

	// get component subtype.
	// i.e. 'vide' for video data; 'soun' for sound data.
	int skipped;

	PRINT_DEBUG_MSG("mdia hdlr atom, its size is " + size);

	skipBytes(VERSION_PLUS_FLAG + COMP_TYPE);
	iTrackType = readString();
	skipped = size - (VERSION_PLUS_FLAG + COMP_TYPE + 4 + ATOM_PLUS_SIZE);
	skipBytes(skipped);
    }

    /*
     * The media header atom specifies the characteristics of the media that is
     * used to store data for the movie track defined in its associated track
     * atom. The media header atom contains the number of bytes in the media
     * header atom, the format of the data in the media header atom, and the
     * media header. TIME_OFFSET includes creation time and modification time.
     */
    private void parseMdhd(int size) throws IOException {

	skipBytes(VERSION_PLUS_FLAG + TIME_OFFSET);
	iTrackTimeScale = readInt();
	iTrackDuration = readInt();
	iTrackStart = readInt();
    }

    // Color map atom
    // For details about color map atom, see class CTAB.
    private void parseCtab(int size) throws IOException {

	int arraySize = 0;

	if (ctabInfo == null)
	    ctabInfo = new CTAB();
	ctabInfo.iSeed = readInt();
	ctabInfo.sFlags = readShort();
	ctabInfo.sSize = (short) (readShort() + 1);
	arraySize = ctabInfo.sSize * 3;

	if (ctabInfo.aColorArray != null) {
	    if (ctabInfo.aColorArray.length < arraySize)
		ctabInfo.aColorArray = new byte[arraySize];
	} else
	    ctabInfo.aColorArray = new byte[arraySize];

	for (int i = 0, j = 0; i < ctabInfo.sSize; i++) {
	    readShort();
	    readByte();
	    ctabInfo.aColorArray[j++] = readByte();
	    readByte();
	    ctabInfo.aColorArray[j++] = readByte();
	    readByte();
	    ctabInfo.aColorArray[j++] = readByte();
	}

	PRINT_DEBUG_MSG("ColorTableSeed = " + ctabInfo.iSeed);
	PRINT_DEBUG_MSG("ColorTableFlags = " + ctabInfo.sFlags);
	PRINT_DEBUG_MSG("ColorTableSize = " + ctabInfo.sSize);
	for (int i = 0; i < arraySize; i++)
	    PRINT_DEBUG_MSG("ColorArray[" + i + "] = "
		    + ctabInfo.aColorArray[i]);
	skipBytes(size - 16 - (8 * ctabInfo.sSize));
    }

    // parseElst
    // An edit atom contains several number of entries.
    // Each entries has three fields: Track duration,
    // media time, and media rate respectively.
    private void parseElst(int size) throws IOException {
	int totalEntries;

	skipBytes(size - 8);
	/**
	 * // NOTE: Don't parse Edit list for faster performance. //
	 * iTrackEditCount = readInt(); // totalEntries = iTrackEditCount *3; //
	 * piTrackEditStart = new int[totalEntries]; // for (int j = 0; j <
	 * totalEntries; j++){ // piTrackEditStart[j] = readInt(); // }
	 **/
    }

    // The sample description informat for each media type is
    // different.
    // Note: we only support video and sound now.
    private void parseStsd(int atomsize, String mtype) throws IOException {
	int skipped;
	int remainsize = 0;

	skipBytes(VERSION_PLUS_FLAG);
	int numEntries = readInt(); // Number of Entries

	if (!(mtype.equals(VIDEO) || mtype.equals(SOUND))) {
	    skipBytes(atomsize - VERSION_PLUS_FLAG - ATOM_PLUS_SIZE - 4);
	    return;
	}

	if (DEBUG) {
	    Log.e("EkhoTV", "numEntries = " + numEntries);
	    Log.e("EkhoTV", "atomsize = " + atomsize);
	}

	for (int i = 0; i < numEntries; i++) {
	    int descSize = readInt(); // Sample Description Size
	    String dataFormat = readString(); // compression format or
					      // media Type;
	    if (DEBUG) {
		Log.e("EkhoTV", "descSize = " + descSize);
		Log.e("EkhoTV", "dataFormat = " + dataFormat);
	    }
	    skipBytes(8);

	    if (mtype.equals(SOUND)) {
		iTrackCODEC = dataFormat;
		skipBytes(8); // Version + Revision Level + Vendor
		iTrackChannels = readShort();
		iTrackSampleSize = readShort();
		skipBytes(4);
		iTrackSampleRate = readInt();

		int iFlags = AudioFormat.FLAG_SIGNED
			| AudioFormat.FLAG_BIGENDIAN;

		if ((iTrackSampleSize == 8) && iTrackCODEC.equals("twos"))
		    iFlags = 0;

		if (iTrackCODEC.equals("agsm"))
		    iTrackCODEC = AudioFormat.JAUDIO_GSM;

		iTrackSampleRate = (iTrackSampleRate >> 16) & 0x0000FFFF;

		// Note: This is a special case that ignores the sample
		// size field.
		if (iTrackCODEC.equals("ulaw"))
		    iTrackSampleSize = 8;

		AudioFormat af = new AudioFormat(iTrackSampleRate, iTrackCODEC,
			iTrackSampleSize, iTrackChannels, iFlags);

		af.setType(AudioFormat.JM_EMBEDDED_QT);
		if (iTrackCODEC.equals(AudioFormat.JAUDIO_IMA4)) {
		    int bytesPerSec;
		    af.setnBlockAlign(34 * iTrackChannels);
		    af.setBytesPerSec(imaSamplesToSize(iTrackSampleRate,
			    iTrackChannels));
		} else if (iTrackCODEC.equals(AudioFormat.JAUDIO_GSM)) {
		    af.setBytesPerSec(iTrackSampleRate * 33 / 160);
		}

		format = af;

		if (DEBUG) {
		    Log.e("EkhoTV", "iTrackCODEC = " + iTrackCODEC);
		    Log.e("EkhoTV", "iTrackChannels = " + iTrackChannels);
		    Log.e("EkhoTV", "iTrackSampleRate = " + iTrackSampleRate);
		    Log.e("EkhoTV", "iTrackSampleSize = " + iTrackSampleSize);
		}
	    }

	    if (mtype.equals(VIDEO) && (i < 1)) {
		iTrackCODEC = dataFormat;
		skipBytes(4 * 4);
		/************************************************
		 * // skipBytes(4); // Version + Revision Level // String vendor
		 * = readString(); // int temporal = readInt(); // int spatial =
		 * readInt();
		 ************************************************/
		int width = readShort();
		int height = readShort();
		skipBytes(4 * 3 + 2 + 32);
		/***********************************************
		 * // int horRes = readInt(); // int verRes = readInt(); // int
		 * datasize = readInt(); // int frameCount = readShort(); //
		 * skipBytes(32); // 32 byte Pascal String;
		 ************************************************/
		iTrackPixelDepth = readShort();
		iTrackColorTableID = readShort();
		int size = 0;

		if (DEBUG) {
		    Log.e("EkhoTV", "iTrackCODEC = " + iTrackCODEC);
		    Log.e("EkhoTV", "iTrackPixelDepth = " + iTrackPixelDepth);
		    Log.e("EkhoTV", "iTrackColorTableID = "
			    + iTrackColorTableID);
		}

		if ((iTrackPixelDepth != 16) && (iTrackPixelDepth != 24)
			&& (iTrackPixelDepth != 32)) {
		    int ncolors;
		    if ((iTrackPixelDepth & 0x4) != 0)
			ncolors = 16;
		    else
			ncolors = 256;
		    if (ctabInfo == null)
			ctabInfo = new CTAB(iTrackPixelDepth);

		    if (iTrackColorTableID == 0) {
			size = readInt();
			String atom = readString();
			if (size > 8)
			    parseCtab(size);
			else
			    size = 8;
			if (DEBUG) {
			    Log.e("EkhoTV", "ctab size = " + size);
			    Log.e("EkhoTV", "ctab atom = " + atom);
			}
		    }
		}
		remainsize = descSize - 16 - 70 - size;

		if (remainsize != 0) {
		    // Video sample description extension.
		    skipBytes(remainsize);
		}

		VideoFormat vf;

		if ((iTrackPixelDepth == 16) || (iTrackPixelDepth == 24)
			|| (iTrackPixelDepth == 32)) {
		    vf = new VideoFormat(iTrackCODEC, iTrackPixelDepth,
		    /**
		     * iFrameTime, iFrameNumber, iVideoMaxSize,
		     **/
		    0, 0, 0, width, height);
		} else {

		    vf = new IndexColorFormat(iTrackCODEC, iTrackPixelDepth,
		    /**
		     * iFrameTime, iFrameNumber, iVideoMaxSize,
		     **/
		    0, 0, 0, width, height, ctabInfo.sSize,
			    ctabInfo.aColorArray);
		}
		format = vf;
	    } else if (mtype.equals(VIDEO)) {
		// Note: The reason skip the second format as follows:
		// Currently, we don't deal with multi-format.
		// chasse.mov contains two video format - cinepak and smc.
		skipBytes(descSize - 16);
	    }
	}
    }

    private void parseStts(int size) throws IOException {
	int totalEntries;

	skipBytes(VERSION_PLUS_FLAG);
	iTrackTimeCount = readInt();
	totalEntries = 2 * iTrackTimeCount;

	piTrackTimeStart = new int[totalEntries];
	for (int j = 0; j < totalEntries; j++) {
	    piTrackTimeStart[j] = readInt();
	}
    }

    private void parseStss(int size) throws IOException {

	skipBytes(VERSION_PLUS_FLAG);
	iTrackSyncCount = readInt();

	piTrackSyncStart = new int[iTrackSyncCount];
	for (int j = 0; j < iTrackSyncCount; j++) {
	    piTrackSyncStart[j] = readInt();
	}
    }

    private void parseStsc(int size) throws IOException {
	int totalEntries;

	skipBytes(VERSION_PLUS_FLAG);
	iTrackChunkCount = readInt();

	totalEntries = 3 * iTrackChunkCount;
	piTrackChunkStart = new int[totalEntries];
	for (int j = 0; j < totalEntries; j++) {
	    piTrackChunkStart[j] = readInt();
	}
    }

    private void parseStsz(int size) throws IOException {
	int totalEntries;
	int sampleSize = 0;

	skipBytes(VERSION_PLUS_FLAG);
	sampleSize = readInt();
	iTrackSizeCount = readInt();

	if (sampleSize != 0) {
	    piTrackSizeStart = new int[1];
	    piTrackSizeStart[0] = sampleSize;
	} else {
	    piTrackSizeStart = new int[iTrackSizeCount];
	    for (int j = 0; j < iTrackSizeCount; j++) {
		piTrackSizeStart[j] = readInt();
	    }
	}
    }

    private void parseStco(int size) throws IOException {

	skipBytes(VERSION_PLUS_FLAG);
	iTrackOffsetCount = readInt();

	// System.err.println("iTrackOffsetCount = " + iTrackOffsetCount);

	piTrackOffsetStart = new int[iTrackOffsetCount];
	for (int j = 0; j < iTrackOffsetCount; j++) {
	    piTrackOffsetStart[j] = readInt();
	    // System.err.println("piTrackOffsetStart[" + j + "] = " +
	    // piTrackOffsetStart[j]);
	}
    }

    private void parseStsh(int size) throws IOException {
	int skipped;

	skipped = size - ATOM_PLUS_SIZE;
	skipBytes(skipped);
    }

    private void skipWholeAtom(int size) throws IOException {

	int skipped;

	skipped = size - ATOM_PLUS_SIZE;
	skipBytes(skipped);
    }

    private long getDuration() {
	long iDuration = -1L;
	iDuration = mvhdInfo.iDuration / mvhdInfo.iTimeScale;
	return iDuration;
    }

    //
    // INNER CLASSES
    //

    // The layout of a movie header atom
    //
    // Field Descriptions
    // Version: A 1 byte specification of the version of this
    // movie header atom
    // Flags: Three bytes of space for future movie header flags
    // Creation time: A 32-bit integer that specifies (in seconds)
    // when the movie atom was created.
    // Modification time: A 32-bit integer that specifies (in seconds)
    // when the movie atom was changed.
    // Time scale: A time value that indicates the time scale for this
    // movie. This is the number of time units that pass per
    // second in its time coordinate system. A time coordinates
    // system that measures time in sixtieths of a second.
    // Duration: A time value that indicates the duration of the movie
    // in time scale units. NOte that this property is derived
    // from the movie's tracks. The value of this field
    // corresponds to the duration of the longest track in the
    // movies.
    // Preferred rate: A 32-bit fixed point number that specifies the
    // rate at which to play this movie. A value of 1.0 indicates
    // normal rate.
    // Preferred volume: A 16 bit fixed point number that specifies how loud
    // to play this movie's sound. A value of 1.0 indicates full
    // volume.
    // Reserved: RESERVE10_OFFSET. Ten bytes reserve for use by Apple.
    // Set to 0.
    // Matrix: The matrix structure associated with this movie. A matrix
    // shows how to map points from one coordinate space into another.
    // Preview time: The time value in the movie at which the preview begins.
    // Preview duration: The duration of the movie preview in movie time scale
    // units.
    // Poster time: The time value of the time of the movie poster.
    // Selection time: The time value of the start time of the current
    // selection.
    // Current time: The time value for current time position within the
    // movie.
    // Next track ID: A 32-bit integer that indicates a value to use for the
    // trackID number of the next track added to this movie. Note:
    // that 0 is not a valid track ID value.
    private class MVHD {
	public int iVersionPlusFlag;
	public int iCreateTime;
	public int iModtime;
	public int iTimeScale;
	public int iDuration;
	public int iPerfRate;
	public short sPerfVol;
	// RESERVE10_OFFSET
	public int aMatrix[]; // 3 x 3 integer
	public int iPrevTime;
	public int iPrevDuration;
	public int iPosterTime;
	public int iSelectTime;
	public int iSelectDuration;
	public int iCurrentTime;
	public int iNextTrackID;
    }

    // The layout of a track header atom.
    //
    // Field Descriptions
    // Version: A 1-byte specification of the version this track header.
    // Track header flags: Three bytes that are reserved for the track
    // header flags indicate how the track is used in the movie.
    // The following flags are valid(all flags are enable when
    // set to 1).
    // Track enabled: indicates that the track is enabled. Flag
    // value is 0x0001.
    // Track in movie: indicates that the track is used in the
    // movie. Flag value is 0x0002.
    // Track in Preview: indicates that the track is used in the
    // movie's preview. Flag value is 0x0004.
    // Track in poster: indicates that the track is used in the
    // movie's poster. Flag value is 0x0008.
    // Creation time: A 32-bit integer that indicates (in seconds) when
    // the track header was created.
    // Modification time: A 32-bit integer that indicates (in seconds) when
    // the track header was changed.
    // TrackID: A 32-bit integer that uniquely identifies the track. A value
    // of 0 must never be used for a trackID.
    // Duration: A time value that indicates the duration of this track.
    // Note: this property is derived form the durations of all the
    // track's edits.
    // Layer: A 16 bits integer that indicates this track's spatial priority
    // in its movie. The QuickTime Movie Toolbox uses this value to
    // to determine how tracks overlay one another. Tracks with lower
    // layer values are displayed in front of the tracks with higher
    // layer values.
    // Alternative group: A 16 bit integer that specifies a collection of movie
    // data for one another. QuickTime chooses one track from the group // to be
    // used when the movie is played. The choice may be based.
    // on such considerations as playback quality or language and the
    // capabilities of the computer.
    // Volume: A 16 bit fixed point value that indicates how loudly this track
    // sound is to be played. A value of 1.0 indicates normal volume.
    // Matrix: The matrix structure associated with this track.
    // Track Width: A 32-bit-fixed point number that specifies the width of
    // this track in pixels.
    // Track height: A 32-bit-fixed point number that specifies the height of
    // this track in pixels.

    private class TKHD {
	public int iVersionPlusFlag;
	public int iCreateTime;
	public int iModtime;
	public int iTrackID;
	// RESERVE4_OFFSET;
	public int iDuration;
	// RESERVE8_OFFSET
	public short sLayer;
	public short sAlternateGroup;
	public short sVolume;
	// RESERVE2_OFFSET;
	public int aMatrix[]; // 3 x 3 integer
	public int iTrackWidth;
	public int iTrackHeight;
    }

    // Field Description:
    // Color table seed: A 32 bit integer that must be set to 0.
    // Color table flags: A 16-bit integer that must be set to 0x8000
    // Color table size: A 16-bit integer that indicates the number of
    // colors in the following color array. This is
    // a zero-relative value; setting field to 0 means
    // there is one color in the array.
    // Color array: An array of colors. Each color is made up of
    // four unsigned 16 bit integers. The first integer
    // must be set to 0, the second is the red value, the
    // the third is the green value, and the fourth is
    // blue value.

    private class CTAB {
	public int iSeed;
	public short sFlags;
	public short sSize;
	public byte aColorArray[];

	byte[] qt_4map = { (byte) 0xff, (byte) 0xfb, (byte) 0xff, (byte) 0xef,
		(byte) 0xd9, (byte) 0xbb, (byte) 0xe8, (byte) 0xc9,
		(byte) 0xb1, (byte) 0x93, (byte) 0x65, (byte) 0x5e,
		(byte) 0xfc, (byte) 0xde, (byte) 0xe8, (byte) 0x9d,
		(byte) 0x88, (byte) 0x91, (byte) 0xff, (byte) 0xff,
		(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
		(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x47,
		(byte) 0x48, (byte) 0x37, (byte) 0x7a, (byte) 0x5e,
		(byte) 0x55, (byte) 0xdf, (byte) 0xd0, (byte) 0xab,
		(byte) 0xff, (byte) 0xfb, (byte) 0xf9, (byte) 0xe8,
		(byte) 0xca, (byte) 0xc5, (byte) 0x8a, (byte) 0x7c, (byte) 0x77 };

	byte[] pat = { (byte) 0xee, (byte) 0xdd, (byte) 0xbb, (byte) 0xaa,
		(byte) 0x88, (byte) 0x77, (byte) 0x55, (byte) 0x44,
		(byte) 0x22, (byte) 0x11 };

	public CTAB() {
	}

	public CTAB(int depth) {
	    sSize = 256;
	    if ((depth & 0x4) != 0)
		sSize = 16;
	    if (aColorArray == null)
		aColorArray = new byte[sSize * 3];
	    if (aColorArray.length < (sSize * 3))
		aColorArray = new byte[sSize * 3];
	    // Log.e("EkhoTV",
	    // "creating the default colormap for depth "+depth);

	    switch (depth) {
	    case 4:
		createDefaultCmap(16);
		break;
	    case 8:
		createDefaultCmap(256);
		break;
	    case 36:
		createGrayCmap(16);
		break;
	    case 40:
		createGrayCmap(256);
		break;
	    default:
		createDefaultCmap(256);
	    }
	}

	public void createDefaultCmap(int ncolors) {
	    int k = 0, j = 0;
	    if (ncolors == 16) {
		for (int i = 0; i < 15; i++) {
		    aColorArray[j++] = qt_4map[k++];
		    aColorArray[j++] = qt_4map[k++];
		    aColorArray[j++] = qt_4map[k++];
		}
	    } else {
		int r = 0xff;
		int g = 0xff;
		int b = 0xff;
		for (int i = 0; i < 215; i++) {
		    aColorArray[j++] = (byte) r;
		    aColorArray[j++] = (byte) g;
		    aColorArray[j++] = (byte) b;
		    b -= 0x33;
		    if (b < 0) {
			b = 0xff;
			g -= 0x33;
			if (g < 0) {
			    g = 0xff;
			    r -= 0x33;
			}
		    }
		}
		for (int i = 0; i < 10; i++) {
		    byte d = pat[i];
		    int ip = 3 * (215 + i);
		    aColorArray[ip++] = d;
		    aColorArray[ip++] = 0;
		    aColorArray[ip++] = 0;
		    ip = 3 * (225 + i);
		    aColorArray[ip++] = 0;
		    aColorArray[ip++] = d;
		    aColorArray[ip++] = 0;
		    ip = 3 * (235 + i);
		    aColorArray[ip++] = 0;
		    aColorArray[ip++] = 0;
		    aColorArray[ip++] = d;
		    ip = 3 * (245 + i);
		    aColorArray[ip++] = d;
		    aColorArray[ip++] = d;
		    aColorArray[ip++] = d;
		}
		j = 3 * 255;
		aColorArray[j++] = 0;
		aColorArray[j++] = 0;
		aColorArray[j] = 0;
	    }
	}

	public void createGrayCmap(int numcolors) {
	    byte g = (byte) 0xff;
	    int i, j = 0;
	    if (numcolors == 256) {
		for (i = 0; i < 256; i++) {
		    aColorArray[j++] = g;
		    aColorArray[j++] = g;
		    aColorArray[j++] = g;
		    g--;
		}
	    } else {
		for (i = 0; i < 16; i++) {
		    aColorArray[j++] = g;
		    aColorArray[j++] = g;
		    aColorArray[j++] = g;
		    g -= 0x11;
		}
	    }
	}

	public byte[] getByteTab() {
	    return aColorArray;
	}
    }
}
