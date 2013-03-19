/*
 * @(#)VidData.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import android.util.Log;

import com.sun.tv.media.format.video.VidFormat;

/**
 * Base class for video data.
 */
public class VidData extends Data {

    protected Object buffer; // Video buf can of byte[] or int[] depending
    // on what's more efficient. Data conversion
    // is too expensive to perform in Java.
    /**
     * Number of valid elements in the buffer. Less than of equal to bufSize.
     */
    protected int length;

    /**
     * Format of the video data.
     */
    protected VidFormat format;

    /**
     * Header can be of any type. Just keeping it generic.
     */
    protected Object header;

    /**
     * Length of the header, if it is an array.
     */
    protected int headerLength;

    public VidData() {
    }

    /**
     * Store the format object
     */
    public VidData(Format f) {
	if (setFormat(f))
	    allocateBuffer();
    }

    /**
     * Allocate a buffer from the format information
     */
    public void allocateBuffer() {
	int aes = format.getArrayElSize();
	if (aes == 4) {
	    buffer = new int[format.getBufferSize()];
	} else if (aes == 1) {
	    buffer = new byte[format.getBufferSize()];
	} else if (aes == 2) {
	    buffer = new short[format.getBufferSize()];
	} else if (aes == 3) {
	    buffer = new byte[format.getBufferSize() * 3];
	} else
	    buffer = null;
    }

    /**
     * @return the format.
     */
    public Format getFormat() {
	return format;
    }

    public boolean setFormat(Format f) {
	if (f != null) {
	    if (f instanceof VidFormat) {
		if (format == null || !format.equals(f))
		    format = (VidFormat) ((VidFormat) f).clone();
		return true;
	    } else
		System.err.println("Illegal format");
	} else
	    Log.e("EkhoTV", "VidData: Format is null");
	return false;
    }

    /**
     * @return the internal buffer.
     */
    public Object getBuffer() {
	return buffer;
    }

    /**
     * Set the buffer object and the size of the buffer.
     * 
     * @param b
     *            the memory buffer.
     */
    public void setBuffer(Object b) {
	buffer = b;
    }

    /**
     * @return the size of the buffer.
     */
    public int getBufferSize() {
	if (format != null)
	    return format.getBufferSize();
	// otherwise
	return 0;
    }

    /**
     * @return the width of the image.
     */
    public int getWidth() {
	if (format != null)
	    return format.getWidth();
	return 0;
    }

    /**
     * @return the height of the image.
     */
    public int getHeight() {
	if (format != null)
	    return format.getHeight();
	return 0;
    }

    /**
     * @return the length of data stored in the buffer.
     */
    public int getLength() {
	return length;
    }

    /**
     * Mark the length of data stored in the int array.
     * 
     * @param l
     *            the length of the data.
     */
    public void setLength(int l) {
	length = l;
    }

    /**
     * Set the header for the video frame. The format of the header depends on
     * the format of the video.
     */
    public void setHeader(Object header, int len) {
	this.header = header;
	headerLength = len;
    }

    /**
     * Get the header. The format of the video decides the type of header. So
     * the node requesting the header should know the format of the header.
     * 
     * @see #setHeader
     */
    public Object getHeader() {
	return header;
    }

    /**
     * Returns the length of the header as specified durint setHeader.
     * 
     * @see #setHeader
     */
    public int getHeaderLength() {
	return headerLength;
    }
}
