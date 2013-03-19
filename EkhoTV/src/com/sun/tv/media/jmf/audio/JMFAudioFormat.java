/*
 * @(#)JMFAudioFormat.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.jmf.audio;

public class JMFAudioFormat extends AudioFormat {

    /**
     * JMFAudioFormat Constructor
     * 
     * @param containerType
     *            where this data came from.
     * @param encoding
     *            data encoding
     * @param sampleSize
     *            of audio data in bytes.
     * @param sampelsPerUnit
     *            of encoded data
     * @param noOfChannels
     *            in encoded data.
     * @param bigEndian
     *            true if big endian data, false if little endian.
     * @param signed
     *            true if data is signed false if unsigned.
     */
    public JMFAudioFormat(int sampleRate, String encoding, int sampleSize,
	    int samplesPerUnit, int noOfChannels, boolean bigEndian,
	    boolean signed) {
	super(sampleRate, encoding, sampleSize, samplesPerUnit, noOfChannels,
		bigEndian, signed);
    }
}
