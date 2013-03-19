/*
 * @(#)AudData.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media;

import android.util.Log;

import com.sun.tv.media.format.audio.AudioFormat;

/**
 * Base class for audio data.
 */
public class AudData extends Data {

    protected Object buffer;
    protected int bufferSize;
    protected int length;
    protected AudioFormat format;
    protected Object header;

    /**
     * Create the data and allocate a byte buffer of length = len.
     * 
     * @param b
     *            the memory buffer.
     * @param the
     *            size of the buffer.
     */
    public AudData(Object b, int size) {
	setBuffer(b, size);
	header = null;
    }

    public AudData(Format f) {
	if (setFormat(f))
	    allocateBuffer();
    }

    public boolean setFormat(Format f) {
	if (f != null) {
	    if (f instanceof AudioFormat) {
		if (format == null || !format.equals(f))
		    format = (AudioFormat) ((AudioFormat) f).clone();
		return true;
	    } else
		System.err.println("Illegal format");
	} else
	    Log.e("EkhoTV", "AudData: Format is null");
	return false;
    }

    private void allocateBuffer() {
	if (format.getFrameSize() == AudioFormat.UNKNOWN_SIZE)
	    return;
	setBuffer(new byte[format.getFrameSize()], format.getFrameSize());
    }

    /**
     * @return the format.
     */
    public Format getFormat() {
	return format;
    }

    /**
     * @return the internal buffer.
     */
    public Object getBuffer() {
	return buffer;
    }

    /**
     * Set the buffer.
     * 
     * @param b
     *            the memory buffer.
     * @param size
     *            the size of the buffer.
     */
    public void setBuffer(Object b, int size) {
	buffer = b;
	bufferSize = size;
    }

    /**
     * @return the size of the buffer.
     */
    public int getBufferSize() {
	return bufferSize;
    }

    /**
     * @return the length of data stored in the buffer.
     */
    public int getLength() {
	return length;
    }

    /**
     * Mark the length of data stored in the buffer.
     * 
     * @param l
     *            length of the data in the buffer.
     */
    public void setLength(int l) {
	length = l;
    }

    /**
     * Set the header information.
     * 
     * @param header
     *            header object.
     */
    public void setHeader(Object header) {
	this.header = header;
    }

    /**
     * @return the header information as an object. Receiver will need to cast
     *         to the right header type.
     */
    public Object getHeader() {
	return header;
    }
}
