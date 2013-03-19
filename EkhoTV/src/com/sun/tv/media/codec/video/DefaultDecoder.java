/*
 * @(#)DefaultDecoder.java	1.6 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.codec.video;

import javax.media.Control;

import com.sun.tv.media.Codec;
import com.sun.tv.media.Data;
import com.sun.tv.media.Format;
import com.sun.tv.media.MediaDecoder;
import com.sun.tv.media.MediaFormat;
import com.sun.tv.media.VidData;
import com.sun.tv.media.codec.DefaultCodecFactory;
import com.sun.tv.media.format.video.VideoFormat;
import com.sun.tv.media.util.DataBufQueue;

/**
 * DefaultDecoder Decodes a frame of compressed video data.
 */
public class DefaultDecoder extends MediaDecoder {

    // //////////////////////////////////////////////////////////////////////
    // CONSTANTS

    final private int INIT_BUFSIZE = 320 * 240;

    // //////////////////////////////////////////////////////////////////////
    // VARIABLES

    private int inWidth = 320; // Assume an initial input size but
    private int inHeight = 240; // can be changed later.

    private VideoFormat inputFormat;
    private String codecID;

    // //////////////////////////////////////////////////////////////////////
    // METHODS

    public DefaultDecoder() {
	super();
	registerInput("VideoIn", new VideoIn(this));
	registerOutput("VideoOut", new VideoOut(this));
    }

    public DefaultDecoder(String codecName) {
	this();
	createCodec(codecName);
    }

    public DefaultDecoder(VideoFormat format) {
	this();
	createCodec(format);
    }

    protected DataBufQueue allocBuffers() {

	// The input format is set to some default which should be the
	// biggest buffer expected.
	inputFormat = new VideoFormat(inWidth, inHeight, INIT_BUFSIZE, 1);

	// Allocate a queue of one buffer for the incoming compressed data.
	DataBufQueue bufQ = new DataBufQueue(1);
	bufQ.addNewBuffer(new VidData(inputFormat));
	return bufQ;
    }

    public boolean supports(MediaFormat format) {
	createCodec(format);
	return getCodec() != null;
    }

    public Data getContainer(Format format) {
	// System.err.println("video.DefaultDecoder.getContainer(): " + format);
	VidData data = (VidData) super.getContainer(format);
	return checkBuffer(data, (VideoFormat) format);
    }

    public Data tryGetContainer(Format format) {
	VidData data;
	if ((data = (VidData) super.tryGetContainer(format)) == null)
	    return null;
	data = checkBuffer(data, (VideoFormat) format);
	return data;
    }

    protected VidData checkBuffer(VidData data, VideoFormat newFmt) {
	if (newFmt == null)
	    return data;
	if (newFmt.getArrayElSize() != inputFormat.getArrayElSize()
		|| newFmt.getBufferSize() > inputFormat.getBufferSize()) {
	    // The required input format is different. We'll reallocate
	    // the buffer. The old data will be garbage-collected.
	    inputFormat = (VideoFormat) newFmt.clone();
	    VidData newData = new VidData(inputFormat);
	    bufQ.replaceOldBuffer(data, newData);
	    data = newData;
	}
	return data;
    }

    /**
     * Accepts a filled container from the upstream node and processes it.
     */
    public void putData(Data inData) {

	VideoFormat format = (VideoFormat) inData.getFormat();

	if (codecID == null || !format.getCodec().equals(codecID))
	    createCodec(format);

	if (getCodec() != null)
	    super.putData(inData);
    }

    public void createCodec(String name) {
	if (name == null)
	    return;
	Codec codec = DefaultCodecFactory.createCodec("video." + name);
	// System.err.println("Video codec = " + name);
	if (codec == null) {
	    // Don't know about this codec.
	    System.err.println("DefaultDecoder: Unknown codec: " + name);
	    setCodec(null);
	} else {
	    codec.initialize(this, null);
	    setCodec(codec);
	}
	codecID = new String(name);
    }

    public void createCodec(MediaFormat format) {
	if (format == null)
	    return;
	Codec codec = DefaultCodecFactory.createCodec(
		"video." + format.getCodec(), format, "codec.video.classes");
	// System.err.println("Video codec = " + format.getCodec());
	if (codec == null) {
	    // Don't know about this codec.
	    System.err.println("DefaultDecoder: Unknown codec: "
		    + format.getCodec());
	    setCodec(null);
	} else {
	    codec.initialize(this, format);
	    setCodec(codec);
	}
	codecID = new String(format.getCodec());
    }

    public Control[] getControls() {
	return new Control[0];
    }

    public void flush() {
	super.flush();
	if (getCodec() != null)
	    getCodec().flush();
    }
}
