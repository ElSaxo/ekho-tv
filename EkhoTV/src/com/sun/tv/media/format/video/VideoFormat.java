/*
 * @(#)VideoFormat.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media.format.video;

import com.sun.tv.media.Format;
import com.sun.tv.media.MediaFormat;

public class VideoFormat extends VidFormat {

    public boolean DEBUG = false;
    static final String SEP = ", "; // separator

    /**
     * Remember if you add a new attribute, please also update copyAttr.
     * Otherwise, clone() won't work.
     */
    private String sIDCODEC = "unknown"; // CODEC type
    // any codec specific string that will be interpreted by the codec
    private byte[] codecData;
    private int iFrameTime; // Frame time (usec/Frame)
    private int iFrameNumber; // Number of frames
    private int iPixelDepth; // Image pixel depth
    private int iVideoMaxSize;
    private long iDuration;
    private boolean bEnable;
    private boolean bKeyFrame;
    private int iColorMapID;

    /**
     * private int iFrameSize; // Aggregate size of frames private boolean
     * bUsingFrameTimes; // Unevenly-spaced movie?
     **/

    /**
     * VideoFormat Constructor
     */
    public VideoFormat(int w, int h, int bs, int aes) {
	super(w, h, bs, aes);
	bEnable = false;
	bKeyFrame = true;
    }

    public VideoFormat(String codec, int pixel, /**
     * boolean flag, int fsize,
     **/
    int ftime, int fnum, int vsize, int w, int h) {

	/**
	 * Note: Quicktime only iFrameSize = fsize; bUsingFrameTimes = flag;
	 **/

	iFrameTime = ftime;
	iFrameNumber = fnum;
	iPixelDepth = pixel;
	iVideoMaxSize = vsize;
	width = w;
	height = h;
	bEnable = true;
	bKeyFrame = true;
	iColorMapID = -1;

	sIDCODEC = MediaFormat.normalizeCodecName(codec);
	setBufferSize(iVideoMaxSize);
	setArrayElSize(1);
    }

    // Note: AVI/Parser use this VideoFormat.
    // setFrameSize will set the maximum chunk size.
    public VideoFormat(String codec, int pixel, int ftime, int fnum, int w,
	    int h) {

	iFrameTime = ftime;
	iFrameNumber = fnum;
	iPixelDepth = pixel;
	width = w;
	height = h;
	bEnable = true;
	bKeyFrame = true;
	iColorMapID = -1;
	sIDCODEC = MediaFormat.normalizeCodecName(codec);

    }

    public VideoFormat() {
	bEnable = false;
	iColorMapID = -1;
    }

    public void setColorTableID(int colorID) {
	iColorMapID = colorID;
    }

    public void setKeyFrame(boolean f) {
	bKeyFrame = f;
    }

    public int getColorTableID() {
	return iColorMapID;
    }

    /**
     * getEnableFlag
     * 
     * @return audio stream enable flag.
     * 
     */
    public boolean getEnableFlag() {
	return bEnable;
    }

    public boolean isKeyFrame() {
	return bKeyFrame;
    }

    public String getCodec() {
	return sIDCODEC;
    }

    public void setCodec(String codec) {
	sIDCODEC = new String(codec);
    }

    public byte[] getCodecData() {
	return codecData;
    }

    public void setCodecData(byte[] codecs) {
	if (codecs == null)
	    return;
	codecData = new byte[codecs.length];
	for (int i = 0; i < codecs.length; i++)
	    codecData[i] = codecs[i];
    }

    public int getFrameTime() {
	return iFrameTime;
    }

    public int getNumberOfFrames() {
	return iFrameNumber;
    }

    public int getPixelDepth() {
	return iPixelDepth;
    }

    public long getDuration() {
	return iDuration;
    }

    public void setDuration(long dur) {
	iDuration = dur;
    }

    public int getMaxVideoSize() {
	return iVideoMaxSize;
    }

    public void setFrameSize(int vsize) {
	iVideoMaxSize = vsize;
	setBufferSize(iVideoMaxSize);
	setArrayElSize(1);
    }

    public boolean equals(Format other) {
	if (!sIDCODEC.equals(((VideoFormat) other).getCodec()))
	    return false;
	return super.equals(other);
    }

    /**
     * match
     * 
     * @return a format compatible with the argument
     */
    public Format match(Format other) {
	// Note: It is incomplete. Will revisit when work on
	// audio information. [cania 1/9/97]
	if (!(other instanceof VideoFormat))
	    return null;

	VideoFormat vf = (VideoFormat) other;

	if (vf.width != width)
	    return null;

	if (vf.height != height)
	    return null;

	if (!vf.sIDCODEC.equals(sIDCODEC))
	    return null;

	if (vf.iPixelDepth != iPixelDepth)
	    return null;

	if (vf.iFrameNumber != iFrameNumber)
	    return null;

	if (vf.iDuration != iDuration)
	    return null;

	return vf;

    }

    /*
     * public String toString() { String w = String.valueOf(width); String h =
     * String.valueOf(height); String framenum = String.valueOf(iFrameNumber);
     * String depth = String.valueOf(iPixelDepth); String dur =
     * String.valueOf(iDuration);
     * 
     * return ("Video: "+ framenum + " frames" + SEP + w + "x" + h + SEP +
     * sIDCODEC + SEP + depth + " bits" + SEP + dur + " seconds" ); }
     */

    public Object clone() {
	VideoFormat dupe = new VideoFormat();
	dupe.copyAttr(this);
	return dupe;
    }

    public void copyAttr(VideoFormat other) {
	super.copyAttr(other);
	iFrameTime = other.iFrameTime;
	iFrameNumber = other.iFrameNumber;
	iPixelDepth = other.iPixelDepth;
	iDuration = other.iDuration;
	iVideoMaxSize = other.iVideoMaxSize;
	bEnable = other.bEnable;
	bKeyFrame = other.bKeyFrame;
	sIDCODEC = new String(other.sIDCODEC);
	codecData = other.codecData;
	// iFrameSize = other.iFrameSize;
	// bUsingFrameTimes = other.bUsingFrameTimes;
    }
}
