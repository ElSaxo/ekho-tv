/*
 * @(#)SunAudioPlay.java	1.5 08/11/27
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.jmf.audio;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Method;

import android.util.Log;

public class SunAudioPlay extends AudioPlay {
    protected sun.audio.AudioStream audioStream;

    private int SUN_MAGIC = 0x2e736e64; // au file magic number
    private int HDR_SIZE = 24; // minimum au header file size
    private int FILE_LENGTH = 0; // file length (optional)
    private int SAMPLE_RATE = 8000;
    private int ENCODING = 1; // ULAW
    private int CHANNELS = 1;

    private int samplesPlayed = 0;
    private boolean isMuted = false;
    private float gain = 0;
    private byte silence[];
    private boolean fConvertToULAW = false;
    /** padding length of silence at the end of the media (default constant) **/
    private static final int END_OF_MEDIA_PADDING_LENGTH = 800;
    /** padding length of silence at the end of the media **/
    private int endOfMediaPaddingLength;
    private RateConversion rateConversion = null;
    private byte[] conversionBuffer;
    private int numberOfInputChannels;
    private int inputSampleSize;
    private int lsbOffset;
    private int msbOffset;
    private int inputBias;
    private int signMask;
    private boolean AudioPlayerStoppingPhase = false;

    // private FileOutputStream IN;

    public SunAudioPlay(AudioFormat format) {
	this(format, computeBufferSize(format));
    }

    protected static int computeBufferSize(AudioFormat f) {
	return 8000;
    }

    public SunAudioPlay(AudioFormat format, int length) {
	super(format, length);

	silence = new byte[bufLength];
	for (int i = 0; i < bufLength; i++)
	    silence[i] = 127;

	endOfMediaPaddingLength = END_OF_MEDIA_PADDING_LENGTH;
	if (endOfMediaPaddingLength > silence.length)
	    endOfMediaPaddingLength = silence.length;

	// BB
	ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
	DataOutputStream tempData = new DataOutputStream(tempOut);
	try {
	    tempData.writeInt(SUN_MAGIC);
	    tempData.writeInt(HDR_SIZE);
	    tempData.writeInt(FILE_LENGTH);
	    tempData.writeInt(ENCODING);
	    tempData.writeInt(SAMPLE_RATE);
	    tempData.writeInt(CHANNELS);
	} catch (Exception e) {
	}

	byte[] buf = tempOut.toByteArray();

	write(buf, 0, buf.length);
    }

    public boolean initialize() {
	String encoding = format.getEncoding();
	int sampleRate = format.getSampleRate();

	if (((format.getChannels() == 1) || (format.getChannels() == 2))
		&& ((sampleRate == 8000) || (sampleRate == 11025)
			|| (sampleRate == 11127) || (sampleRate == 16000)
			|| (sampleRate == 22050) || (sampleRate == 22254)
			|| (sampleRate == 22255) || (sampleRate == 32000)
			|| (sampleRate == 44100) || (sampleRate == 48000))
		&& ((encoding.equals(AudioFormat.JAUDIO_G711_ULAW)
			&& (sampleRate == 8000) && (format.getChannels() == 1))
			|| encoding.equals(AudioFormat.JAUDIO_LINEAR) || encoding
			    .equals(AudioFormat.JAUDIO_LINEAR8))) {

	    if (encoding.equals(AudioFormat.JAUDIO_LINEAR)
		    || encoding.equals(AudioFormat.JAUDIO_LINEAR8)) {
		fConvertToULAW = true;
	    }
	    // isUlaw = true;
	} else {
	    Log.e("EkhoTV", "AudioPlay:Unsupported Audio Format");
	    // isUlaw = false;
	    return false;
	}

	int rateIn = sampleRate;
	int rateOut = 8000;
	boolean isSigned = format.isSigned();
	boolean isBigEndian = format.isBigEndian();
	inputSampleSize = format.getSampleSize();
	numberOfInputChannels = format.getChannels();

	if (sampleRate != 8000) {

	    int pcmType = RateConversion.RATE_CONVERSION_LITTLE_ENDIAN_FORMAT;

	    if (isBigEndian) {
		pcmType = RateConversion.RATE_CONVERSION_BIG_ENDIAN_FORMAT;
	    }

	    if (1 == inputSampleSize) {
		pcmType = RateConversion.RATE_CONVERSION_BYTE_FORMAT;
	    }

	    rateConversion = new RateConversion();

	    // the input buffer is one second long
	    int bufferLength = (numberOfInputChannels * inputSampleSize * sampleRate);

	    if (RateConversion.RATE_CONVERSION_OK != rateConversion.init(
		    bufferLength, rateIn, rateOut, numberOfInputChannels, 1,
		    pcmType, isSigned)) {
		rateConversion = null;
		return false;

	    }

	    int length = rateConversion.getDrainMaxLength();
	    conversionBuffer = new byte[length];

	} else { // 8 Khz

	    if ((isBigEndian) || (1 == inputSampleSize)) {
		lsbOffset = 1;
		msbOffset = 0;
	    } else {
		lsbOffset = -1;
		msbOffset = 1;
	    }

	    if (isSigned) {
		inputBias = 0;
		signMask = 0xffffffff;
	    } else {
		inputBias = 32768;
		signMask = 0x0000ffff;
	    }

	}

	// 6706556, start the AudioPlayer here instead of in the write() method.
	try {
	    audioStream = new sun.audio.AudioStream(this);
	    start();
	} catch (Exception e) {
	    System.err.println("Exception: " + e);
	    audioStream = null;
	    return false;
	}
	return true;
    }

    public void finalize() throws Throwable {
	rateConversion.close();
	rateConversion = null;
	super.finalize();
	stop();
    }

    public synchronized boolean start() {
	if ((audioStream != null) && !started) {
	    sun.audio.AudioPlayer.player.start(audioStream);
	    started = true;
	}
	return started;
    }

    public synchronized void pause() {
	flush();
	if (audioStream != null) {
	    AudioPlayerStoppingPhase = true;
	    sun.audio.AudioPlayer.player.stop(audioStream);
	}
	AudioPlayerStoppingPhase = false;
	paused = true;
    }

    public synchronized void resume() {
	if ((audioStream != null) && (!started || paused)) {
	    sun.audio.AudioPlayer.player.start(audioStream);
	}
	paused = false;
    }

    // $$ AudioRenderer's abortPrefetch() calls this method
    public synchronized void stop() {
	flush();
	if (audioStream != null) {
	    AudioPlayerStoppingPhase = true;
	    sun.audio.AudioPlayer.player.stop(audioStream);
	}
	AudioPlayerStoppingPhase = false;
	buffer = null;
    }

    public void drain() {

	int remain;
	int len;

	synchronized (syncObject) {

	    if (null != rateConversion) {
		remain = rateConversion.drain(conversionBuffer, 0);
		int off = 0;
		while (remain > 0 && !flushing) {
		    len = write(conversionBuffer, off, remain);
		    off += len;
		    remain -= len;
		}
	    }

	    remain = endOfMediaPaddingLength;
	    // pad the end of the media with silence
	    // (used to drain sun.audio.AudioPlayer.player)

	    while (remain > 0 && !flushing) {
		len = write(silence, 0, remain);
		remain -= len;
	    }
	    // drain the JMF buffer.
	    while (in != out && !paused) {
		try {
		    syncObject.wait();
		} catch (InterruptedException e) {
		}
	    }

	}

    }

    // Clean the buffer.
    public/* synchronized */void flush() {
	synchronized (syncObject) {
	    in = 0;
	    out = 0;
	    if (null != rateConversion) {
		rateConversion.reset();
	    }
	    // zeroCount = 0;
	    flushing = true;
	    syncObject.notifyAll();
	}
    }

    public AudioFormat getFormat() {
	return format;
    }

    public long getSamplesPlayed() {
	return (audioStream == null ? 0 : samplesPlayed);
    }

    public long getTick() {
	/*
	 * double samples = (double)samplesPlayed; //samples =
	 * samples/(double)format.getSampleRate(); // SunAudioPlay plays at 8
	 * Khz samples = samples/(double)8000; // Log.e("EkhoTV",
	 * "AudioPlay.getTick() " + samples); return (audioStream == null ? 0 :
	 * (long)(samples * 1000000000L) );
	 */
	return (audioStream == null ? 0 : samplesPlayed * 125000L);
    }

    public int getSampleSize() {
	// Log.e("EkhoTV", "AudioPlay.getSampleSize()");
	return format.getSampleSize();
    }

    public void setGain(float g) {

	gain = g;
	if (!com.sun.tv.media.renderer.audio.AudioRenderer.useGainControl()) {
	    return;
	}

	try { // sun.audio supports the setVolume
	    Class c = Class.forName("sun.audio.AudioPlayer");
	    Class p[] = new Class[2];
	    p[0] = Integer.TYPE;
	    p[1] = Integer.TYPE;
	    Method m = c.getDeclaredMethod("setVolume", p);

	    Object args[] = new Object[2];
	    args[0] = new Integer((int) (gain * 255));
	    args[1] = new Integer(0);
	    m.invoke(sun.audio.AudioPlayer.player, args);
	} catch (Exception e) {
	    // Log.e("EkhoTV",
	    // "Sorry - Volume control not supported on this platform.");
	}
    }

    public void setMute(boolean m) {
	// Log.e("EkhoTV", "AudioPlay.setMute()");
	isMuted = m;
    }

    public float getGain() {
	// Log.e("EkhoTV", "AudioPlay.getGain()");
	return gain;
    }

    public boolean isMuted() {
	return isMuted;
    }

    // $$ Non blocking read
    public/* synchronized */int read(byte b[], int off, int len) {
	// Log.e("EkhoTV", "AP:needs: " + len + " available: " + available());
	// Log.e("EkhoTV", "AP: read3: " + Thread.currentThread() + ": " +
	// Thread.currentThread().getPriority() +
	// ": paused, avail: " + paused + ": " + available());
	if (len <= 0) {
	    return 0;
	}

	if (!firstRead) {
	    if (available() <= len) {
		if (audioStream == null)
		    return 0;
		int size = format.getSampleSize();
		len = (available() / 2) / size * size;
	    }
	    firstRead = true;
	}

	if (paused) {
	    // Log.e("EkhoTV", "AP read: PAUSED,available is " + available());
	    // There is not need to count zero's here since the clock has
	    // already stopped and decoupled from the time base at this point.
	    return 0;
	}

	int rlen = 1;

	synchronized (syncObject) {

	    // If read would block then return 0
	    if (available() == 0) {
		// $$ Log.e("EkhoTV", "AP: read3 will block. returning 0");
		// $$ It appears that sometimes the JavaSound thread is
		// calling read (which returns 0 as the buffer is empty)
		// so often that other threads don't have a chance to
		// fill the buffer. So I am putting a wait here

		try {
		    syncObject.wait(20); // 20 millisec.
		} catch (InterruptedException e) {
		}

		// Log.e("EkhoTV", "buffer is empty ");

		if (AudioPlayerStoppingPhase) {
		    return -1; // release the audio player -> it should be
			       // stopped
		}

		// System.err.println("underflow: " + len/2);
		// zeroCount += len/2;

		return 0;
	    }

	    // This read will not block
	    int c = read();
	    if (c < 0) {
		return -1;
	    }
	    b[off] = (byte) c;

	    if (in != out) {
		int avail, need, size;

		len--; // 1 byte read and copied.
		if (out < in) {
		    avail = (in - out);
		    if (avail > len)
			avail = len;
		    System.arraycopy(buffer, out, b, off + 1, avail);
		    out += avail;
		    rlen += avail;
		} else if (out > in) {
		    avail = bufLength - out;
		    if (avail >= len) {
			avail = len;
			System.arraycopy(buffer, out, b, off + 1, avail);

			out += avail;
			if (out >= bufLength)
			    out = 0;
			rlen += avail;
		    } else {
			System.arraycopy(buffer, out, b, off + 1, avail);
			out += avail;
			if (out >= bufLength)
			    out = 0;
			int copied = avail;
			rlen += avail;
			need = (len - avail);
			avail = (in - out);
			if (need <= avail)
			    size = need;
			else
			    size = avail;
			System.arraycopy(buffer, 0, b, off + 1 + copied, size);
			out += size;
			rlen += size;
		    }
		}
	    }

	    // Notify if there's any waiting writer.
	    syncObject.notifyAll();
	}

	if (isMuted)
	    System.arraycopy(silence, 0, b, off, rlen);

	// System.err.println("AR: done read: " + rlen);

	samplesPlayed += rlen;
	return rlen;
    }

    // Write an array of bytes to the buffer. Block until there's
    // enough space in the buffer.
    public/* synchronized */int write(byte data[], int off, int len) {

	if (len <= 0)
	    return 0;

	int wlen = 0;

	synchronized (syncObject) {

	    // Block if the buffer is full.
	    while ((in + 1) % buffer.length == out && !flushing) {
		try {
		    syncObject.wait();
		} catch (InterruptedException e) {
		}
	    }

	    if (flushing) {
		flushing = false;
		return 0;
	    }

	    if (true) {
		int canWrite, actualWrite, actualWrite1, length1;
		if (in < out) {
		    canWrite = out - in - 1;
		    actualWrite = (canWrite < len) ? canWrite : len;
		    System.arraycopy(data, off, buffer, in, actualWrite);
		    in += actualWrite;
		    wlen += actualWrite;
		} else {
		    if (out == 0)
			length1 = bufLength - in - 1;
		    else
			length1 = bufLength - in;

		    if (length1 >= len) {
			actualWrite = len;
			System.arraycopy(data, off, buffer, in, actualWrite);
			in += actualWrite;
			if (in >= bufLength)
			    in = 0;
			wlen += actualWrite;
		    } else {
			actualWrite = length1;
			System.arraycopy(data, off, buffer, in, actualWrite);
			in += actualWrite;
			if (in >= bufLength)
			    in = 0;
			wlen += actualWrite;
			len -= actualWrite;
			actualWrite1 = actualWrite;

			if (out > 0) {
			    canWrite = out - in - 1;
			    actualWrite = (canWrite < len) ? canWrite : len;
			    System.arraycopy(data, off + actualWrite1, buffer,
				    0, actualWrite);
			    wlen += actualWrite;
			    in = actualWrite;
			}
		    }
		}
	    }

	    // Notify the waiting reader.
	    syncObject.notifyAll();
	}

	/**
	 * 6706556, start the AudioPlayer at the end of initialize() instead.
	 * synchronized(this) { if (!started) { if (audioStream != null) {
	 * sun.audio.AudioPlayer.player.start(audioStream); started = true; } }
	 * }
	 **/

	return wlen;
    }

    public boolean needConversion() {
	return fConvertToULAW;
    }

    public int convertData(byte data[], int offset, int len) {

	int outputLength;

	if (null != rateConversion) {
	    outputLength = rateConversion.process(data, 0, len, data, 0);
	    return outputLength;
	}

	outputLength = convertToULAW(data, offset, len);
	return outputLength;

    }

    int convertToULAW(byte input[], int inputOffset, int inputLength) {

	int sample, signBit, inputSample;
	int i;
	byte[] dst = input; // to avoid holding another buffer
	int dstIndex = inputOffset;

	for (i = inputOffset + msbOffset; i < (inputLength + inputOffset);) {

	    if (1 == inputSampleSize) {
		inputSample = input[i++] << 8;

		if (2 == numberOfInputChannels) {
		    inputSample = ((inputSample & signMask) + ((input[i++] << 8) & signMask)) >> 1;
		}
	    } else {
		inputSample = (input[i] << 8) + (0xff & input[i + lsbOffset]);
		i += 2;

		if (2 == numberOfInputChannels) {
		    inputSample = ((inputSample & signMask) + (((input[i] << 8) + (0xff & input[i
			    + lsbOffset])) & signMask)) >> 1;
		    i += 2;
		}
	    }

	    sample = (int) ((short) (inputSample + inputBias));

	    if (sample >= 0) { // Sample=abs(sample)
		signBit = 0x80; // sign bit
	    } else {
		sample = -sample;
		signBit = 0x00;
	    }
	    sample = (132 + sample) >> 3; // bias

	    if (sample < 0x20) {
		dst[dstIndex++] = (byte) (signBit | (7 << 4) | (31 - (sample >> 0)));
	    } else if (sample < 0x0040) {
		dst[dstIndex++] = (byte) (signBit | (6 << 4) | (31 - (sample >> 1)));
	    } else if (sample < 0x0080) {
		dst[dstIndex++] = (byte) (signBit | (5 << 4) | (31 - (sample >> 2)));
	    } else if (sample < 0x0100) {
		dst[dstIndex++] = (byte) (signBit | (4 << 4) | (31 - (sample >> 3)));
	    } else if (sample < 0x0200) {
		dst[dstIndex++] = (byte) (signBit | (3 << 4) | (31 - (sample >> 4)));
	    } else if (sample < 0x0400) {
		dst[dstIndex++] = (byte) (signBit | (2 << 4) | (31 - (sample >> 5)));
	    } else if (sample < 0x0800) {
		dst[dstIndex++] = (byte) (signBit | (1 << 4) | (31 - (sample >> 6)));
	    } else if (sample < 0x1000) {
		dst[dstIndex++] = (byte) (signBit | (0 << 4) | (31 - (sample >> 7)));
	    } else {
		dst[dstIndex++] = (byte) (signBit | (0 << 4) | (31 - (0xfff >> 7)));
	    }
	}

	return inputLength / (numberOfInputChannels * inputSampleSize);
    }

    /*
     * private void saveInput(byte [] indata, int length) { try { if (IN ==
     * null) IN = new FileOutputStream("audio.pcm"); IN.write(indata, 0,
     * length); IN.flush(); } catch (Exception e) { Log.e("EkhoTV",
     * "Frame not saved: "+e); } }
     */

}
