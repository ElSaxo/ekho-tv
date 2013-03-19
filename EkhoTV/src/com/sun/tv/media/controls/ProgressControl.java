/*
 * @(#)ProgressControl.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.controls;


public interface ProgressControl extends GroupControl {

    /**
     * A StringControl that displays the instantaneous frame rate, if video is
     * present.
     */
    StringControl getFrameRate();

    /**
     * A StringControl that displays the instantaneous bandwidth of the input
     * stream.
     */
    StringControl getBitRate();

    /**
     * Displays the video properties such as size, compression type, etc. which
     * are specific to the incoming video stream.
     */
    StringControl getVideoProperties();

    StringControl getVideoCodec();

    StringControl getAudioCodec();

    /**
     * Displays the audio properties such as sampling rate, resolution,
     * compression type, etc. specific to the incoming audio stream.
     */
    StringControl getAudioProperties();
}
