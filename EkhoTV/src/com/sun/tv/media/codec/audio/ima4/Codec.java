/*
 * @(#)Codec.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.codec.audio.ima4;

import com.sun.tv.media.AudData;
import com.sun.tv.media.Data;
import com.sun.tv.media.Format;
import com.sun.tv.media.MediaFormat;
import com.sun.tv.media.MediaFrameCodec;
import com.sun.tv.media.format.audio.AudioFormat;

public class Codec extends MediaFrameCodec {

    // //////////////////////////////////////////////////////////////////////////
    // Variables
    // //////////////////////////////////////////////////////////////////////////

    // variable used by native code to store a pointer to the C++ class
    int codec;

    private static boolean fUseJava;

    // Format of the input compressed audio
    private AudioFormat inputFormat;
    // Format of the output DVI
    private AudioFormat outputFormat;
    // debug flag
    private boolean DEBUG = false;

    // state of the ima4 decoder
    private IMA4State ima4state;
    private boolean flushFlag = false;

    // //////////////////////////////////////////////////////////////////////////
    // Native methods

    // initialize the native codec
    private native boolean initIMA4Codec();

    // free any buffers allocated by the native codec
    private native boolean freeIMA4Codec();

    // Decode a compressed 4 bit DVI audio frame and output a
    // 16 bits PCM samples
    private native int decodeIMA4(Object inData, Object outData, int lenIn,
	    int lenOut, int nChannels);

    private native int getEndianess(int f);

    static {

	fUseJava = true;

    }

    // //////////////////////////////////////////////////////////////////////////
    // Methods

    public Codec() {
    }

    /** Initializes the codec. **/
    protected boolean initialize(MediaFormat format) {
	inputFormat = new AudioFormat();
	outputFormat = new AudioFormat();

	if (fUseJava) {
	    ima4state = new IMA4State();
	} else {
	    initIMA4Codec();
	}

	return true;
    }

    public boolean supports(MediaFormat fmt) {
	return fmt.getCodec().equals("ima4");
    }

    /** Clean up **/
    public void finalize() {

	if (fUseJava) {
	    ima4state = null;
	} else {
	    freeIMA4Codec();
	}

    }

    /**
     * Checks the header of the compressed audio packet and detects any format
     * changes. Does not modify the data in any way.
     */
    public boolean checkFormat(Data data) {

	if (data.getFormat() == null
		|| !(data.getFormat() instanceof AudioFormat))
	    return false;

	AudioFormat fmt = (AudioFormat) data.getFormat();
	if (inputFormat.match(fmt) == null) {
	    inputFormat = (AudioFormat) fmt.clone();
	    outputFormat = (AudioFormat) fmt.clone();
	    outputFormat.setFrameSize(inputFormat.getFrameSize() * 4);
	    outputFormat.setEncoding(AudioFormat.JAUDIO_LINEAR);
	    outputFormat.setSampleSize(16);
	    outputFormat.setFlags(AudioFormat.FLAG_SIGNED
		    | AudioFormat.FLAG_BIGENDIAN);
	}
	return true;
    }

    /** decode the data **/
    public boolean decode(Data inData, Data outData) {
	int flags = 0;
	int decoded;

	flushFlag = false;
	AudData iaData = (AudData) inData;
	outData.setFormat(outputFormat);

	int channels = outputFormat.getChannels();
	byte[] inBuffer = (byte[]) inData.getBuffer();
	byte[] outBuffer = (byte[]) outData.getBuffer();

	if (fUseJava) {
	    decoded = decodeJavaIMA4(inBuffer, outBuffer, inData.getLength(),
		    outBuffer.length, channels);
	} else {
	    decoded = decodeIMA4(inBuffer, outBuffer, inData.getLength(),
		    outBuffer.length, channels);
	}

	outData.setLength(decoded);

	if (flushFlag) {
	    return false;
	} else {
	    return true;
	}
    }

    /**
     * Returns a Format object describing the type of input data expected.
     * 
     * @see #decode
     */
    public Format getInputFormat() {
	return inputFormat;
    }

    /**
     * Returns a Format object describing the type of output produced by the
     * decode method.
     * 
     * @see #decode
     */
    public Format getOutputFormat() {
	return outputFormat;
    }

    /**
     * IMA4 decoding: sends the data to either decodeIMA4mono or
     * decodeIMA4stereo
     **/
    private int decodeJavaIMA4(byte[] inBuffer, byte[] outBuffer, int lenIn,
	    int lenOut, int nChannels) {
	switch (nChannels) {
	case 1: // mono
	    return decodeIMA4mono(inBuffer, outBuffer, lenIn, lenOut, 0x20); // 0x20
									     // is
									     // IMA4
									     // chunk
									     // size
	case 2: // stereo
	    return decodeIMA4stereo(inBuffer, outBuffer, lenIn, lenOut, 0x20);
	default:
	    throw new RuntimeException(
		    "IMA4: Can only handle 1 or 2 channels\n");

	}
    }

    private int decodeIMA4mono(byte[] inBuffer, byte[] outBuffer, int lenIn,
	    int lenOut, int blockSize) {
	int inCount = 0;
	int outCount = 0;

	// IMA4 mono chunk format is 2 bytes header followed by 32 bytes encoded
	// data

	lenIn = (lenIn / (blockSize + 2)) * (blockSize + 2);

	while (inCount < lenIn) {
	    int state = (inBuffer[inCount++] << 8);
	    state |= (inBuffer[inCount++] & 0xff);

	    // state is now prevVal(9 most significant bits- signed )::index (7
	    // least significant bits- unsigned)

	    int index = state & 0x7F;

	    if (index > 88)
		index = 88;

	    ima4state.valprev = state & 0xFFFFFF80;
	    ima4state.index = index;

	    IMA4.decode(inBuffer, inCount, outBuffer, outCount, blockSize << 1,
		    ima4state, 0);

	    inCount += blockSize;
	    outCount += blockSize << 2;
	}

	return outCount;
    }

    private int decodeIMA4stereo(byte[] inBuffer, byte[] outBuffer, int lenIn,
	    int lenOut, int blockSize) {
	int inCount = 0;
	int outCount = 0;

	// Log.e("EkhoTV", lenIn);
	lenIn = (lenIn / 2 / (blockSize + 2)) * (blockSize + 2) * 2;

	// IMA4 stereo chunk format is left IMA4 mono chunk followed by right
	// IMA4 mono chunk
	while (inCount < lenIn) {
	    // LEFT
	    int stateL = (inBuffer[inCount++] << 8);
	    stateL |= (inBuffer[inCount++] & 0xff);

	    int indexL = stateL & 0x7F;

	    if (indexL > 88)
		indexL = 88;

	    ima4state.valprev = stateL & 0xFFFFFF80;
	    ima4state.index = indexL;

	    IMA4.decode(inBuffer, inCount, outBuffer, outCount, blockSize << 1,
		    ima4state, 2);

	    inCount += blockSize;

	    // RIGHT
	    int stateR = (inBuffer[inCount++] << 8);
	    stateR |= (inBuffer[inCount++] & 0xff);

	    int indexR = stateR & 0x7F;

	    if (indexR > 88)
		indexR = 88;

	    ima4state.valprev = stateR & 0xFFFFFF80;
	    ima4state.index = indexR;

	    IMA4.decode(inBuffer, inCount, outBuffer, outCount + 2,
		    blockSize << 1, ima4state, 2);

	    // loop counters
	    inCount += blockSize;
	    outCount += blockSize << 3;
	}

	return outCount;
    }

    public void flush() {
	if (fUseJava) { // reset
	    ima4state.valprev = 0;
	    ima4state.index = 0;
	}
	flushFlag = true;
	super.flush();
    }

}
