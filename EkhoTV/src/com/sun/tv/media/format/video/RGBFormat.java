/*
 * @(#)RGBFormat.java	1.3 08/09/15
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
public class RGBFormat extends VideoFormat {

    /**
     * Remember if you add a new attribute, please also update copyAttr.
     * Otherwise, clone() won't work.
     */
    protected int redMask, greenMask, blueMask;
    protected int depth;

    public RGBFormat(int width, int height, int bs, int aes, int rmask,
	    int gmask, int bmask, int d) {
	super(width, height, bs, aes);
	redMask = rmask;
	greenMask = gmask;
	blueMask = bmask;
	depth = d;
    }

    public RGBFormat(int width, int height, int bs, int aes, int rmask,
	    int gmask, int bmask) {
	this(width, height, bs, aes, rmask, gmask, bmask, aes);
    }

    public RGBFormat() {
    }

    public boolean equals(Format other) {
	try {
	    RGBFormat rgb = (RGBFormat) other;
	    if (rgb.redMask == redMask && rgb.greenMask == greenMask
		    && rgb.blueMask == blueMask && rgb.depth == depth
		    && super.equals(other))
		return true;
	} catch (ClassCastException e) {
	}
	return false;
    }

    public Format match(Format other) {
	if (other instanceof RGBFormat)
	    return this;
	else
	    return null;
    }

    public Object clone() {
	RGBFormat dupe = new RGBFormat();
	dupe.copyAttr(this);
	return dupe;
    }

    public void copyAttr(RGBFormat other) {
	super.copyAttr(other);
	redMask = other.redMask;
	greenMask = other.greenMask;
	blueMask = other.blueMask;
	depth = other.depth;
    }

    public int getDepth() {
	return depth;
    }

    public int getRedMask() {
	return redMask;
    }

    public int getGreenMask() {
	return greenMask;
    }

    public int getBlueMask() {
	return blueMask;
    }

    public String toString() {
	String ret = super.toString();
	return ret.concat(", redMask=" + redMask + ", greenMask=" + greenMask
		+ ", blueMask=" + blueMask + ", depth=" + depth);
    }
}
