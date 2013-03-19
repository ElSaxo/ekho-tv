/*
 * @(#)PseudoToRGB.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.renderer.video;

import com.sun.tv.media.VidData;
import com.sun.tv.media.format.video.IndexColorFormat;
import com.sun.tv.media.format.video.RGBFormat;
import com.sun.tv.media.format.video.VidFormat;

/**
 * Use this class to convert an IndexColor frame to RGB.
 */

public class PseudoToRGB implements ColorConverter {

    private int[] cmap;
    private int depth;

    private native boolean initConverter(byte[] colors, int rMask, int gMask,
	    int bMask, int depth);

    private native boolean convertPseudoToRGB(VidData in, VidData out);

    // Load the jmutil library that contains the conversion code.
    static {
	// try {
	// JMFSecurity.loadLibrary("jmutil");
	// } catch (UnsatisfiedLinkError e) {
	// System.err.println("Error loading native library jmutil:" + e);
	// }
    }

    public PseudoToRGB(IndexColorFormat srcFmt, RGBFormat destFmt) {
	cmap = new int[srcFmt.getColormapSize()];
	this.depth = destFmt.getDepth();
	// System.err.println(this.depth+" "+cmap.length);
	initConverter(srcFmt.getColormap(), destFmt.getRedMask(),
		destFmt.getGreenMask(), destFmt.getBlueMask(),
		destFmt.getDepth());
    }

    public int[] getCmap() {
	return cmap;
    }

    public boolean canConvert(VidFormat in, VidFormat out) {
	return ((in instanceof IndexColorFormat) && (out instanceof RGBFormat));
    }

    public boolean convert(VidData in, VidData out) {
	return convertPseudoToRGB(in, out);
    }
}
