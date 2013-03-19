/*
 * @(#)AudioPlay.java	1.4 08/11/27
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.jmf.audio;

import java.io.InputStream;

public abstract class AudioPlay extends InputStream {
    protected int bufLength;
    protected byte buffer[];
    protected static int EOM = -1;
    protected boolean paused = false;
    protected boolean flushing = false;
    protected boolean started = false;
    protected boolean firstRead = false;
    protected AudioFormat format;
    int in = 0;
    int out = 0;
    boolean eom = false;
    // 6706556. This lock synchronizes I/O operations in read(), write(),
    // flush() and drain()
    // while "this" object synchronizes the AudioPlayer start/stop operations.
    protected Object syncObject = new Object();

    public AudioPlay(AudioFormat format, int length) {
	this.format = format;
	bufLength = length;
	buffer = new byte[bufLength];
    }

    public abstract boolean initialize();

    public void finalize() throws Throwable {
	super.finalize();
	stop();
    }

    public int available() {
	if (in == out) {
	    return 0;
	} else {
	    if (in > out)
		return in - out;
	    else
		return bufLength - (out - in);
	}
    }

    // Read a byte of data. Block if there is no data to read.
    public/**synchronized**/int read() {
	int ret = -1;

	synchronized (syncObject) {
	    // Block if the buffer is empty.
	    while (in == out) {
		if (eom) {
		    eom = false;
		    return EOM;
		}
		try {
		    syncObject.wait();
		} catch (InterruptedException e) {
		}
	    }
	    ret = buffer[out++] & 0xFF;
	    if (out >= buffer.length) {
		out = 0;
	    }
	}

	return ret;
    }

    public abstract long getTick();

    public abstract void setGain(float g);

    public abstract void setMute(boolean m);

    public abstract float getGain();

    public abstract boolean isMuted();

    public abstract int read(byte b[], int off, int len);

    public abstract int write(byte data[], int off, int len);

    public abstract boolean needConversion();

    public abstract int convertData(byte[] data, int offset, int len);

    public abstract void pause();

    public abstract void resume();

    public abstract void stop();

    public abstract void drain();

    public abstract void flush();
}
