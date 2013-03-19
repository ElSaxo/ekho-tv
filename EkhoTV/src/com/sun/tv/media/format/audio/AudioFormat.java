/*
 * @(#)AudioFormat.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.format.audio;

import com.sun.tv.media.Format;
import com.sun.tv.media.MediaFormat;

/**
 * implements general audio format. It contains sample rate, encoding, sample
 * size, channels and flags.
 **/
public class AudioFormat extends MediaFormat {

    public static final String JAUDIO_UNKNOWN = "JAUDIO_UNKNOWN";
    public static final String JAUDIO_LINEAR = "JAUDIO_LINEAR";
    public static final String JAUDIO_G711_ULAW = "JAUDIO_G711_ULAW";
    public static final String JAUDIO_G711_ALAW = "JAUDIO_G711_ALAW";
    public static final String JAUDIO_NONE = "none";
    public static final String JAUDIO_FLOAT = "float";
    public static final String JAUDIO_DOUBLE = "double";
    public static final String JAUDIO_G722 = "g722";
    public static final String JAUDIO_G723_3 = "g723";
    public static final String JAUDIO_G723_5 = "g723";
    public static final String JAUDIO_G721_ADPCM = "g721_adpcm";
    public static final String JAUDIO_DBI_ADPCM = "dbi_adpcm";
    public static final String JAUDIO_OKI_ADPCM = "oki_adpcm";
    public static final String JAUDIO_MS_ADPCM = "msadpcm";
    public static final String JAUDIO_DIGISTD = "digistd";
    public static final String JAUDIO_DIGIFIX = "digifix";
    public static final String JAUDIO_DVI = "dvi";
    public static final String JAUDIO_IMA4 = "ima4";
    public static final String JAUDIO_LINEAR8 = "linear8";
    public static final String JAUDIO_MAC6 = "mac6";
    public static final String JAUDIO_MAC3 = "mac3";
    public static final String JAUDIO_GSM = "gsm";
    public static final String JAUDIO_SX7383 = "sx7383";

    // stream type
    public static final int JM_UNKNOWN = 0;
    public static final int JM_AIFF = 1;
    public static final int JM_WAVE = 2;
    public static final int JM_AU = 3;
    public static final int JM_EMBEDDED_QT = 4;
    public static final int JM_EMBEDDED_AVI = 5;
    public static final int JM_MIDI = 6;
    public static final int JM_RMF = 7;
    public static final int JM_VIVO = 8;
    public static final int JM_RTP = 9;

    public static final int FLAG_BIGENDIAN = 1 << 0;
    public static final int FLAG_SIGNED = 1 << 1;
    public static final int UNKNOWN_SIZE = -1;

    /**
     * Remember if you add a new attribute, please also update copyAttr.
     * Otherwise, clone() won't work.
     */
    protected String sAudioEncoding; // Audio Encoding Type
    protected String sIDCODEC = "unknown";
    protected byte[] codecData;
    protected int iSampleRate; // 16.16 Hz
    protected int iSampleSize; // 8-bit or 16-bit ( in bit )
    protected int iChannels; // 1==mono 2==stereo
    protected int iFlags;
    protected int frameSize; // An audio frame size in bytes.
    protected long lDuration; // in nanoseconds
    private boolean bEnable;
    private int bytesPerSec;
    private int totalBytes;
    private int header;
    private int nBlockAlign = UNKNOWN_SIZE;
    private int streamType = JM_UNKNOWN;

    /**
     * AudioFormat Constructor
     * 
     * @param sr
     *            sample rate
     * @param en
     *            encoding
     * @param ss
     *            sample size
     * @param ch
     *            # of channels
     * @param f
     *            flags: FLAG_BIGENDIAN | FLAG_SIGNED
     */

    public AudioFormat() {
	header = 0;
	bEnable = false;
	setBytesPerSec(UNKNOWN_SIZE);
	setnBlockAlign(1);
	setFrameSize(UNKNOWN_SIZE);
	setDuration(UNKNOWN_SIZE);
	sAudioEncoding = AudioFormat.JAUDIO_NONE;
	sIDCODEC = new String("none");
    }

    // Note: quicktime/Parser and x_msvideo/Parser will used the following
    // AudioFormat (default one) to construct the audio format.
    public AudioFormat(int sRate, String en, int sSize, int ch, int flag) {
	iSampleRate = sRate;
	sAudioEncoding = en;
	iSampleSize = sSize;
	iChannels = ch;
	iFlags = flag;
	bEnable = true;
	header = 0;

	// NOTE: JavaSound only supports ulaw and linear.
	// JavaSound reads this attribute.
	// Should eliminate it later... cania
	if (en.equals("ulaw"))
	    sAudioEncoding = AudioFormat.JAUDIO_G711_ULAW;
	else if (en.equals("raw ") || en.equals("twos") || en.equals(""))
	    sAudioEncoding = AudioFormat.JAUDIO_LINEAR;

	// By default
	// Calculate number of bytes per sound unit
	// bytesPerSec = (iChannels * iSampleSize / 8);
	//
	// Calculate number of bytes of 1 second of second
	// cbSoundWorth = iSampleRate * cbSoundSize;
	bytesPerSec = (int) (iChannels * iSampleSize * iSampleRate) / 8;
	setnBlockAlign(1);
	setFrameSize(bytesPerSec);
	setDuration(UNKNOWN_SIZE);

	if (en.equals("raw ") || en.equals("twos") || en.equals(""))
	    en = AudioFormat.JAUDIO_LINEAR;

	sIDCODEC = MediaFormat.normalizeCodecName(en);
    }

    // Note: x_wav/Parser, basic/Parser, and x_aiff/Parser will
    // used the following AudioFormat constructor to construct
    // the audio Format. (They have totalBytes and head offset)
    public AudioFormat(int sr, String en, int ss, int ch, int f, int tb,
	    int head) {
	this(sr, en, ss, ch, f);
	totalBytes = tb;
	header = head;

	if ((bytesPerSec > 0) && (totalBytes > 0))
	    setDuration(totalBytes * 1000000000L / bytesPerSec);
    }

    public void setnBlockAlign(int ba) {
	nBlockAlign = ba;

	if (frameSize != UNKNOWN_SIZE && nBlockAlign != 0)
	    frameSize = frameSize - (frameSize % nBlockAlign);
    }

    public int getnBlockAlign() {
	return nBlockAlign;
    }

    public int getType() {
	return streamType;
    }

    public void setType(int ftype) {
	streamType = ftype;
    }

    // Note: This is useful for audio file only such as au, wav, aiff.
    public void setHeaderSize(int h) {
	header = h;
    }

    public int getHeaderSize() {
	return header;
    }

    public void setCodecData(byte[] s) {
	if (s == null)
	    return;
	codecData = new byte[s.length];
	for (int i = 0; i < s.length; i++)
	    codecData[i] = s[i];

    }

    public byte[] getCodecData() {
	return codecData;
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

	if (af.iChannels != iChannels)
	    return null;

	if (af.iFlags != iFlags)
	    return null;

	if (sAudioEncoding == null)
	    return null;

	if (!(af.sAudioEncoding.equals(sAudioEncoding)))
	    return null;

	return af;

    }

    /**
     * getEnableFlag
     * 
     * @return audio stream enable flag.
     * 
     */
    public boolean getEnableFlag() {
	return bEnable;
    }

    /**
     * getEncoding
     * 
     * @return audio encoding type
     */
    public String getEncoding() {
	return sAudioEncoding;
    }

    /**
     * @return the name of codec used.
     */
    public String getCodec() {
	return sIDCODEC;
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
     * getSampleSize (in # of bits)
     * 
     * @return sample size
     */
    public int getSampleSize() {
	return iSampleSize;
    }

    /**
     * getFlags
     * 
     * @return Endianness
     */
    public int getFlags() {
	return iFlags;
    }

    public void setCodec(String codec) {
	sIDCODEC = new String(codec);
    }

    /**
     * setFlags
     * 
     * @param f
     *            flags
     */
    public void setFlags(int f) {
	iFlags = f;
    }

    /**
     * Get the number of bytes per sound unit
     * 
     * @return number of bytes per sound unit.
     */
    public int getBytesPerSec() {
	return bytesPerSec;
    }

    /**
     * For compressed audio, bytes per second cannot be rely on sample rate,
     * sample size and channels to calculate.
     */
    public void setBytesPerSec(int bps) {
	bytesPerSec = bps;
    }

    /**
     * @return the size of an audio frame.
     */
    public int getFrameSize() {
	return frameSize;
    }

    /**
     * Set the frame size.
     * 
     * @param size
     *            the frame size.
     */
    public void setFrameSize(int size) {
	frameSize = size;
	if (frameSize < UNKNOWN_SIZE)
	    frameSize = UNKNOWN_SIZE;

	// Frame Size should be aligned by nBlockAlign
	if (nBlockAlign != UNKNOWN_SIZE && nBlockAlign != 0) {
	    frameSize = frameSize - (frameSize % nBlockAlign);
	}
    }

    /**
     * Clone the format.
     * 
     * @return the clone of the format.
     */
    public Object clone() {
	AudioFormat dupe = new AudioFormat();
	dupe.copyAttr(this);
	return dupe;
    }

    /*
     * Set the format encoding.
     * 
     * @param encoding audio encoding
     */
    public void setEncoding(String encoding) {
	sAudioEncoding = new String(encoding);
    }

    /**
     * Set the sample size.
     * 
     * @param size
     *            the sample size.
     */
    public void setSampleSize(int size) {
	iSampleSize = size;
    }

    protected void copyAttr(AudioFormat from) {
	sAudioEncoding = from.sAudioEncoding;
	sIDCODEC = from.sIDCODEC;
	iSampleSize = from.iSampleSize;
	iSampleRate = from.iSampleRate;
	iChannels = from.iChannels;
	iFlags = from.iFlags;
	frameSize = from.frameSize;
	lDuration = from.lDuration;
	bEnable = from.bEnable;
	bytesPerSec = from.bytesPerSec;
	codecData = from.codecData;
	nBlockAlign = from.nBlockAlign;
	streamType = from.streamType;
    }

    public long getDuration() {
	return lDuration;
    }

    public void setDuration(long d) {
	lDuration = d;
    }

    public String toString() {
	String s = this.getClass().getName();
	s = s + " encoding " + sAudioEncoding;
	s = s + " sIDCODEC " + sIDCODEC;
	s = s + " iSampleSize " + iSampleSize;
	s = s + " iSampleRate " + iSampleRate;
	s = s + " iChannels " + iChannels;
	s = s + " iFlags " + iFlags;
	s = s + " frameSize " + frameSize;
	s = s + " lDuration " + lDuration;
	s = s + " bEnable " + bEnable;
	s = s + " bytesPerSec " + bytesPerSec;
	s = s + " codecData " + codecData;
	s = s + " nBlockAlign " + nBlockAlign;
	return s;
    }

}
