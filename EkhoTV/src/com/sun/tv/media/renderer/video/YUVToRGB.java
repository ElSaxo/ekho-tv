/*
 * @(#)YUVToRGB.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.renderer.video;

import com.sun.tv.media.VidData;
import com.sun.tv.media.format.video.RGBFormat;
import com.sun.tv.media.format.video.VidFormat;
import com.sun.tv.media.format.video.YUVFormat;

/**
 * Use this class to convert a YUV frame to RGB. Conversion handles YUV data of
 * type 411, 422 and 111. The class can also scale to the following sizes:
 * 0.25x, 0.5x, 1.0x, 2.0x.
 */
public class YUVToRGB implements ColorConverter {

    // The RGB masks for a 32bit RGB output frame.
    private int redMask;
    private int greenMask;
    private int blueMask;
    private int screenDepth;

    // Conversion scaling factor in float
    private float fScale;
    // Scale value as required by the native code:
    // 0.25x = 3, 0.5x = 2, 1.0x = 1, 2.0x = 0
    private int iScale;

    // native C++ object (of type YuvToRgb)
    private int converter;

    private float brightness = 0.5f;
    private float chroma = 0.5f;
    private float contrast = 0.5f;
    private float hue = 0.5f;
    private int grayscale = 0;
    private int tintRed = 0;
    private int tintGreen = 0;
    private int tintBlue = 0;

    // Native functions
    // initConverter expects the values for redMask, greenMask and blueMask
    // to be set before its called. The method creates the uv table for use
    // in conversions.
    private native boolean initConverter();

    // Performs the actual conversion between yuv and rgb at the specified scale
    private native boolean convertYUVToRGB(YUVData yuv, RGBData rgb, int scale,
	    int elSize);

    private native boolean freeConverter();

    // Load the jmutil library that contains the conversion code.
    static {
	// try {
	// JMFSecurity.loadLibrary("jmutil");
	// } catch (UnsatisfiedLinkError e) {
	// System.err.println("Error loading native library jmutil:" + e);
	// }
    }

    /**
     * Creates an object for YUV to RGB conversion and sets up conversion tables
     * depending on the RGB masks specified.
     */
    public YUVToRGB(int rm, int gm, int bm, int sd) {
	redMask = rm;
	greenMask = gm;
	blueMask = bm;
	screenDepth = sd;
	// call the native method to create a C++ class and setup the uvtable
	initConverter();
    }

    public void finalize() {
	freeConverter();
    }

    /**
     * Convert the in frame to out frame, assuming the in frame is YUV and out
     * frame is RGB. <B>scale</B> specifies the scale factor for the conversion.
     * Can be 0.25, 0.5, 1.0 or 2.0. Any other value will default to 1.0
     */
    public boolean convert(VidData in, VidData out) {
	int elSize;
	boolean returnVal;
	float scale = 1.0f;
	int oldGray = grayscale;
	if (in instanceof YUVData && out instanceof RGBData) {
	    YUVData yuv = (YUVData) in;
	    RGBData rgb = (RGBData) out;
	    fScale = scale;
	    iScale = fScaleToiScale(scale);
	    if (rgb.getBuffer() instanceof int[])
		elSize = 4;
	    else
		elSize = 1;
	    // Switch to grayscale output if saturation (chroma) is very low.
	    if (chroma <= 0.02f)
		grayscale = 1;
	    returnVal = convertYUVToRGB(yuv, rgb, iScale, elSize);
	    grayscale = oldGray;
	    return returnVal;
	}
	System.err.println("Unsupported conversion");
	return false;
    }

    /**
     * Converts a float scale value to an integer value required by the native
     * code.
     */
    private int fScaleToiScale(float fscale) {
	if (fscale == 1.00f)
	    return 1;
	if (fscale == 2.00f)
	    return 0;
	if (fscale == 0.50f)
	    return 2;
	if (fscale == 0.25f)
	    return 3;
	return 1;
    }

    /**
     * Sets the brightness of the video output. Should be a value between 0.0
     * and 1.0. Returns the value that was actually set.
     */
    public float setBrightness(float b) {
	if (b < 0)
	    b = 0;
	if (b > 1)
	    b = 1;
	brightness = b;
	return b;
    }

    /**
     * Sets the chroma of the video output. Should be a value between 0.0 and
     * 1.0. Returns the value that was actually set.
     */
    public float setSaturation(float c) {
	if (c < 0)
	    c = 0;
	else if (c > 1)
	    c = 1;
	chroma = c;
	return c;
    }

    /**
     * Sets the contrast of the video output. Should be a value between 0.0 and
     * 1.0. Returns the value that was actually set.
     */
    public float setContrast(float c) {
	if (c < 0)
	    c = 0;
	else if (c > 1)
	    c = 1;
	contrast = c;
	return c;
    }

    /**
     * Sets the hue of the video output. Should be a value between 0.0 and 1.0.
     * Returns the value that was actually set.
     */
    public float setHue(float h) {
	if (h < 0)
	    h = 0;
	else if (h > 1)
	    h = 1;
	hue = h;
	return h;
    }

    /**
     * Sets the video output to grayscale mode. 0 turns it off and 1 turns it
     * on. Returns the value that was actually set.
     */
    public boolean setGrayscale(boolean g) {
	if (g)
	    grayscale = 1;
	else
	    grayscale = 0;
	return g;
    }

    /**
     * Returns the current brightness setting for the video output.
     */
    public float getBrightness() {
	return brightness;
    }

    /**
     * Returns the current contrast setting for the video output.
     */
    public float getContrast() {
	return contrast;
    }

    /**
     * Returns the current hue setting for the video output.
     */
    public float getHue() {
	return hue;
    }

    /**
     * Returns the current chroma setting for the video output.
     */
    public float getSaturation() {
	return chroma;
    }

    /**
     * Returns the current grayscale setting for the video output.
     */
    public boolean getGrayscale() {
	return (grayscale == 1);
    }

    public boolean canConvert(VidFormat in, VidFormat out) {
	if (in instanceof YUVFormat && out instanceof RGBFormat)
	    return true;
	else
	    return false;
    }
}
