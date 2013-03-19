/*
 * @(#)VidFormat.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media.format.video;

import com.sun.tv.media.Format;
import com.sun.tv.media.MediaFormat;

/**
 * VidFormat
 */
public class VidFormat extends MediaFormat {

    /**
     * Remember if you add a new attribute, please also update copyAttr.
     * Otherwise, clone() won't work.
     */
    protected int width;
    protected int height;
    protected int strideX;
    protected int strideY;
    protected int bufferSize;
    protected int arrayElSize;

    public VidFormat(int w, int h, int bs, int aes) {
	width = w;
	height = h;
	strideX = w;
	strideY = h;
	bufferSize = bs;
	arrayElSize = aes;
    }

    public VidFormat() {
    }

    public void setWidth(int width) {
	this.width = width;
    }

    public void setHeight(int height) {
	this.height = height;
    }

    public void setBufferSize(int bufferSize) {
	this.bufferSize = bufferSize;
    }

    public void setArrayElSize(int arrayElSize) {
	this.arrayElSize = arrayElSize;
    }

    public void setStrideX(int x) {
	strideX = x;
    }

    public void setStrideY(int y) {
	strideY = y;
    }

    public int getWidth() {
	return width;
    }

    public int getHeight() {
	return height;
    }

    public int getBufferSize() {
	return bufferSize;
    }

    public int getArrayElSize() {
	return arrayElSize;
    }

    public int getStrideX() {
	return strideX;
    }

    public int getStrideY() {
	return strideY;
    }

    public boolean equals(Format other) {
	VidFormat fmt = (VidFormat) other;
	if (width == fmt.getWidth() && height == fmt.getHeight()
		&& strideX == fmt.getStrideX() && strideY == fmt.getStrideY()
		&& bufferSize == fmt.getBufferSize()
		&& arrayElSize == fmt.getArrayElSize())
	    return true;
	else
	    return false;
    }

    public Format match(Format other) {
	if (other instanceof VidFormat)
	    return this;
	else
	    return null;
    }

    public Object clone() {
	VidFormat dupe = new VidFormat();
	dupe.copyAttr(this);
	return dupe;
    }

    public void copyAttr(VidFormat other) {
	width = other.width;
	height = other.height;
	strideX = other.strideX;
	strideY = other.strideY;
	bufferSize = other.bufferSize;
	arrayElSize = other.arrayElSize;
    }

    public String toString() {
	String ret = new String(this.getClass().getName() + ": width=" + width
		+ ", height=" + height + ", strideX=" + strideX + ", strideY="
		+ strideY + ", bufferSize=" + bufferSize + ", arrayElSize="
		+ arrayElSize);
	return ret;
    }

    public long getDuration() {
	return 0;
    }

    public String getCodec() {
	return null;
    }
}
