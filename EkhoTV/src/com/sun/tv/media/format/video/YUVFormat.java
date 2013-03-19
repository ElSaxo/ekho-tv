/*
 * @(#)YUVFormat.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media.format.video;

import com.sun.tv.media.Format;

/**
 * YUVFormat
 */
public class YUVFormat extends VideoFormat {

    public static final int YUV411 = 1;
    public static final int YUV422 = 2;
    public static final int YUV111 = 4;
    public static final int YVU9 = 3;

    /**
     * Remember if you add a new attribute, please also update copyAttr.
     * Otherwise, clone() won't work.
     */
    protected int decimation;

    public YUVFormat(int width, int height, int bs, int aes, int decimation) {
	super(width, height, bs, aes);
	this.decimation = decimation;
    }

    public YUVFormat() {
    }

    public void setDecimation(int dec) {
	decimation = dec;
    }

    public int getDecimation() {
	return decimation;
    }

    public boolean equals(Format other) {
	try {
	    YUVFormat yuv = (YUVFormat) other;
	    if (yuv.getDecimation() == decimation && super.equals(other))
		return true;
	} catch (ClassCastException e) {
	}
	return false;
    }

    public Format match(Format other) {
	if (other instanceof YUVFormat)
	    return this;
	else
	    return null;
    }

    public Object clone() {
	YUVFormat dupe = new YUVFormat();
	dupe.copyAttr(this);
	return dupe;
    }

    public void copyAttr(YUVFormat other) {
	super.copyAttr(other);
	decimation = other.decimation;
    }

}
