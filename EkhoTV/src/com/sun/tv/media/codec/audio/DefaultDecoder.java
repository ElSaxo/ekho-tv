/*
 * @(#)DefaultDecoder.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.codec.audio;

import javax.media.Control;

import com.sun.tv.media.AudData;
import com.sun.tv.media.Codec;
import com.sun.tv.media.Data;
import com.sun.tv.media.Format;
import com.sun.tv.media.MediaDecoder;
import com.sun.tv.media.MediaFormat;
import com.sun.tv.media.codec.DefaultCodecFactory;
import com.sun.tv.media.format.audio.AudioFormat;
import com.sun.tv.media.util.DataBufQueue;

/**
 * DefaultDecoder Decodes a frame of compressed audio data.
 */
public class DefaultDecoder extends MediaDecoder {

    // //////////////////////////////////////////////////////////////////////
    // CONSTANTS

    // //////////////////////////////////////////////////////////////////////
    // VARIABLES

    private AudioFormat inFormat = null;
    private String codecID;

    // //////////////////////////////////////////////////////////////////////
    // METHODS

    public DefaultDecoder() {
	super();
	registerInput("AudioIn", new AudioIn(this));
	registerOutput("AudioOut", new AudioOut(this));
    }

    public DefaultDecoder(String codecName) {
	this();
	createCodec(codecName);
    }

    public DefaultDecoder(AudioFormat fmt) {
	this();
	createCodec(fmt);
    }

    protected DataBufQueue allocBuffers() {

	// The input format is set to some default which should be the
	// biggest buffer expected.
	AudioFormat fmt = new AudioFormat();

	// Allocate a queue of one buffer for the incoming compressed data.
	DataBufQueue bufQ = new DataBufQueue(1);
	bufQ.addNewBuffer(new AudData(fmt));
	return bufQ;
    }

    public boolean supports(MediaFormat fmt) {
	createCodec(fmt);
	return getCodec() != null;
    }

    public Data getContainer(Format format) {
	// System.err.println("DefaultDecoder.getContainer(): " + format);
	AudData data = (AudData) super.getContainer(format);
	return checkBuffer(data, (AudioFormat) format);
    }

    public Data tryGetContainer(Format format) {
	// System.err.println("DefaultDecoder.tryGetContainer(): " + format);
	AudData data;
	if ((data = (AudData) super.tryGetContainer(format)) == null)
	    return null;
	return checkBuffer(data, (AudioFormat) format);
    }

    protected AudData checkBuffer(AudData data, AudioFormat newFmt) {
	if (newFmt != null && newFmt.getFrameSize() > data.getBufferSize()) {
	    // The required input format is different. We'll reallocate
	    // the buffer. The old data will be garbage-collected.
	    AudData newData = new AudData(newFmt);
	    bufQ.replaceOldBuffer(data, newData);
	    data = newData;
	}
	return data;
    }

    /**
     * Accepts a filled container from the upstream node and processes it.
     */
    public void putData(Data inData) {
	// System.err.println("DefaultDecoder.putData(): " +
	// inData.getFormat());

	AudioFormat fmt = (AudioFormat) ((AudData) inData).getFormat();

	// Check the header to see if we need to switch codecs.
	String name = fmt.getCodec();
	if (codecID == null || !name.equals(codecID))
	    createCodec(fmt);

	if (getCodec() == null)
	    putThrough(inData);
	else
	    super.putData(inData);
    }

    public void createCodec(String name) {
	if (name == null)
	    return;
	Codec codec = DefaultCodecFactory.createCodec("audio." + name);
	if (codec == null) {
	    // Don't know about this codec.
	    setCodec(null);
	} else {
	    codec.initialize(this, null);
	    setCodec(codec);
	}
	codecID = new String(name);
    }

    public void createCodec(MediaFormat fmt) {
	if (fmt == null)
	    return;
	Codec codec = DefaultCodecFactory.createCodec(
		"audio." + fmt.getCodec(), fmt, "codec.audio.classes");
	if (codec == null) {
	    // Don't know about this codec.
	    setCodec(null);
	} else {
	    codec.initialize(this, fmt);
	    setCodec(codec);
	}
	codecID = new String(fmt.getCodec());
    }

    public Control[] getControls() {
	return null;
    }

}
