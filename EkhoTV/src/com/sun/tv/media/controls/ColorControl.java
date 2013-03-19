/*
 * @(#)ColorControl.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.controls;


/**
 * A group of controls that modify the color of the video output from this
 * player.
 */
public interface ColorControl extends GroupControl {

    /**
     * Returns a brightness control object for the video output.
     */
    NumericControl getBrightness();

    /**
     * Returns a contrast control object for the video output.
     */
    NumericControl getContrast();

    /**
     * Returns a color saturation control object for the video output.
     */
    NumericControl getSaturation();

    /**
     * Returns a hue control object for the video output.
     */
    NumericControl getHue();

    /**
     * Returns a grayscale control object for the video output. Grayscale output
     * can be turned on or off.
     */
    BooleanControl getGrayscale();
}
