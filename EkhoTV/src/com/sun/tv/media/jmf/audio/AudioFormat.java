/*
 * @(#)AudioFormat.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.jmf.audio;

/**
 * Implements general audio format.
 * 
 * @version 1.13 98/03/18
 * @author Cania Lee
 * @author Kara Kytlle
 * @author David Rivas
 */

class AudioFormat implements Format {

    // Encoding Types

    // public static final String JAUDIO_NONE = "JAUDIO_NONE";
    public static final String JAUDIO_G711_ULAW = "JAUDIO_G711_ULAW";
    public static final String JAUDIO_G711_ALAW = "JAUDIO_G711_ALAW";
    public static final String JAUDIO_LINEAR = "JAUDIO_LINEAR";
    public static final String JAUDIO_FLOAT = "JAUDIO_FLOAT";
    public static final String JAUDIO_DOUBLE = "JAUDIO_DOUBLE";
    public static final String JAUDIO_G722 = "JAUDIO_G722";
    public static final String JAUDIO_G723_3 = "JAUDIO_G723_3";
    public static final String JAUDIO_G723_5 = "JAUDIO_G723_5";
    public static final String JAUDIO_G721_ADPCM = "JAUDIO_G721_ADPCM";
    public static final String JAUDIO_DBI_ADPCM = "JAUDIO_DBI_ADPCM";
    public static final String JAUDIO_OKI_ADPCM = "JAUDIO_OKI_ADPCM";
    public static final String JAUDIO_DIGISTD = "JAUDIO_DIGISTD";
    public static final String JAUDIO_DIGIFIX = "JAUDIO_DIGIFIX";
    public static final String JAUDIO_DVI = "JAUDIO_DVI";
    public static final String JAUDIO_LINEAR8 = "JAUDIO_LINEAR8";
    public static final String JAUDIO_MAC6 = "JAUDIO_MAC6";
    public static final String JAUDIO_MAC3 = "JAUDIO_MAC3";
    public static final String JAUDIO_GSM = "JAUDIO_GSM";

    // Format particulars.
    private int iSampleRate;
    private String sEncode;
    private int iSampleSize; // in bytes.
    private int iSamplePerUnit;
    private int iChannels; // 1==mono 2==stereo
    private int cbSoundSize; // Bytes per sound unit
    private int cbSoundWorth; // Bytes of sound in 1 second
    private boolean bigEndian; // is the data big or little endian.
    private boolean signed; // is data signed or unsigned.

    /**
     * AudioFormat Constructor
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
    public AudioFormat(int sampleRate, String encoding, int sampleSize,
	    int samplesPerUnit, int noOfChannels, boolean bigEndian,
	    boolean signed) {

	// Set it all up.
	iSampleRate = sampleRate;
	iSamplePerUnit = samplesPerUnit;
	sEncode = encoding;
	iSampleSize = sampleSize;
	iChannels = noOfChannels;
	this.bigEndian = bigEndian;
	this.signed = signed;

	// Calculate number of bytes per sound unit
	cbSoundSize = (iChannels * iSampleSize) / 8;

	// Calculate number of bytes of 1 second of second
	cbSoundWorth = iSampleRate * cbSoundSize;

    }

    /**
     * match
     * 
     * @return a format compatible with the argument
     */
    public Format match(Format other) {

	if (!(other instanceof AudioFormat))
	    return null;

	AudioFormat af = (AudioFormat) other;

	if (af.iSampleRate != iSampleRate)
	    return null;

	if (af.iSampleSize != iSampleSize)
	    return null;

	if (af.iSamplePerUnit != iSamplePerUnit)
	    return null;

	if (af.iChannels != iChannels)
	    return null;

	if (af.bigEndian != bigEndian)
	    return null;

	if (af.signed != signed)
	    return null;

	if (!(af.sEncode.equals(sEncode)))
	    return null;

	return af;
    }

    /**
     * getEncoding
     * 
     * @return audio encoding type
     */
    public String getEncoding() {

	return sEncode;
    }

    /**
     * getSampleRate
     * 
     * @return audio sample rate
     */
    public int getSampleRate() {

	return iSampleRate;
    }

    /**
     * getChannels
     * 
     * @return audio channel ( mono == 1, stereo == 2, none == 0 )
     */
    public int getChannels() {

	return iChannels;
    }

    /**
     * getSampleSize
     * 
     * @return sample size
     */
    public int getSampleSize() {

	return iSampleSize;
    }

    /**
     * getSamplePerUnit
     * 
     * @return sample per unit
     */
    public int getSamplePerUnit() {

	return iSamplePerUnit;
    }

    /**
     * getMaxSoundSize
     * 
     * @return sound size.
     */
    public int getMaxSoundSize() {

	return cbSoundWorth;
    }

    /**
     * @return endianness
     */
    public boolean isBigEndian() {
	return bigEndian;
    }

    /**
     * @return signed or unsigned.
     */
    public boolean isSigned() {
	return signed;
    }

    /**
     * toString print out all fields in AudioFormat
     * 
     * @return string for format.
     */
    public String toString() {

	String channels;
	switch (iChannels) {

	case 1:
	    channels = "mono, ";
	    break;

	case 2:
	    channels = "stereo, ";
	    break;

	default:
	    channels = String.valueOf(iChannels) + " channel, ";
	    break;

	}
	int bit = (8 / iSamplePerUnit) * iSampleSize;

	String formatString = channels + String.valueOf(bit) + " bit "
		+ String.valueOf(iSampleRate) + "Hz, " + getEncoding() + ", "
		+ ((signed) ? "signed " : "unsigned ")
		+ ((bigEndian) ? "big-endian " : "little-endian ")
		+ "audio data";

	return formatString;
    }
}
