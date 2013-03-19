/*
 * @(#)IndexColorFormat.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media.format.video;

import com.sun.tv.media.Format;

public class IndexColorFormat extends VideoFormat {

    /**
     * Remember if you add a new attribute, please also update copyAttr.
     * Otherwise, clone() won't work.
     */
    int colormapSize;
    byte[] colormap = null;

    public IndexColorFormat(int width, int height, int bs, int aes, int cms,
	    byte[] colormap) {
	super(width, height, bs, aes);
	colormapSize = cms;
	this.colormap = colormap;
    }

    public IndexColorFormat(String codec, int pixel, int ftime, int fnum,
	    int vsize, int w, int h, int cms, byte[] colormap) {
	super(codec, pixel, ftime, fnum, vsize, w, h);
	colormapSize = cms;
	this.colormap = colormap;
    }

    public IndexColorFormat() {
    }

    public boolean equals(Format other) {
	try {
	    IndexColorFormat icf = (IndexColorFormat) other;
	    if (sameColormap(icf) && super.equals(icf))
		return true;
	    else
		return false;
	} catch (Exception e) {
	    return false;
	}
    }

    private boolean sameColormap(IndexColorFormat other) {
	if (colormapSize != other.getColormapSize())
	    return false;

	byte[] otherColormap = other.getColormap();

	// check for eq?, if that fails, check for equal?
	if (otherColormap == colormap)
	    return true;

	if (otherColormap.length < colormapSize * 3)
	    return false;
	for (int i = 0; i < colormapSize * 3; i++)
	    if (colormap[i] != otherColormap[i])
		return false;
	return true;
    }

    public byte[] getColormap() {
	return colormap;
    }

    public int getColormapSize() {
	return colormapSize;
    }

    public void setColormap(byte[] colormap) {
	this.colormap = colormap;
    }

    public void setColormapSize(int cms) {
	colormapSize = cms;
    }

    public Object clone() {
	IndexColorFormat dupe = new IndexColorFormat();
	dupe.copyAttr(this);
	return dupe;
    }

    public String toString() {
	String ret = super.toString();
	return ret.concat(", no. colors=" + colormapSize);
    }

    public void copyAttr(IndexColorFormat other) {
	super.copyAttr(other);
	colormapSize = other.colormapSize;
	colormap = other.colormap;
    }
}
