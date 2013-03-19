/*
 * @(#)RGBToRGB.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media.renderer.video;

import com.sun.tv.media.VidData;
import com.sun.tv.media.format.video.RGBFormat;
import com.sun.tv.media.format.video.VidFormat;

/**
 * Use this class for any RGB to RGB conversions. The bit depths of input/output
 * formats are 16, 24, 32.
 */

public class RGBToRGB implements ColorConverter {

    // The RGB masks for the conversion
    private int redMask;
    private int greenMask;
    private int blueMask;
    // The required shift for the conversion
    private int redShift;
    private int greenShift;
    private int blueShift;
    // The native conversion function
    private int conversionFun;

    // performs the actual initialization procedure
    private native boolean initConverter(int rm1, int gm1, int bm1, int aes1,
	    int rm2, int gm2, int bm2, int aes2);

    // performs the actual conversion from one RGB to another
    private native boolean convertRGBToRGB(VidData in, VidData out);

    /**
     * Creates an object for RGB to RGB conversions and sets up conversion masks
     * depending on input and output RGB masks.
     */
    public RGBToRGB(int rm1, int gm1, int bm1, int aes1, int rm2, int gm2,
	    int bm2, int aes2) {
	initConverter(rm1, gm1, bm1, aes1, rm2, gm2, bm2, aes2);
    }

    public boolean convert(VidData in, VidData out) {
	if (canConvert((VidFormat) in.getFormat(), (VidFormat) out.getFormat()))
	    return convertRGBToRGB(in, out);
	else
	    return false;
    }

    public boolean canConvert(VidFormat in, VidFormat out) {
	return ((in instanceof RGBFormat) && (out instanceof RGBFormat));
    }

}
