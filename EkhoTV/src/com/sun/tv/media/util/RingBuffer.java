/*
 * @(#)RingBuffer.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media.util;

import java.io.IOException;

import android.util.Log;

/**
 * RingBuffer
 */
public class RingBuffer {

    private byte buf[]; // A buffer queue of objects.
    private long headOffset, tailOffset;
    private int head = 0, tail = 0; // Put to tail.
    private int num = 0; // bytes available
    private long currentIndex;
    private Timer downloadThread;
    private boolean flushed;

    public String ID; // debugging
    public boolean DEBUG = false;

    // For debugging only
    public void setID(String id) {
	ID = id;
    }

    public RingBuffer(int n, long h) {
	buf = new byte[n];
	headOffset = h;
	tailOffset = h;
	head = 0;
	tail = 0;
	currentIndex = 0;
	flushed = false;
	ID = new String("NONE");
    }

    public void setCurrentIndex(long l) {
	PRINT_DEBUG_MSG("RB.setCurrentIndex to " + l);
	currentIndex = l;
    }

    public long getStart() {
	return headOffset;
    }

    public long getEnd() {
	return tailOffset;
    }

    public synchronized int write(byte buffer[], int off, int size)
	    throws IOException {

	int len = size, i;

	while (isFull()) {
	    try {
		wait();
	    } catch (InterruptedException e) {
	    }
	}

	if (flushed) {
	    flushed = false;
	    return 0;
	}

	PRINT_DEBUG_MSG("RB.write: " + ID + ": currentIndex = " + currentIndex);
	if (len > getAvailable())
	    len = getAvailable();

	if ((tail + len) < buf.length)
	    System.arraycopy(buffer, off, buf, tail, len);
	else {
	    int curLen = buf.length - tail;
	    System.arraycopy(buffer, off, buf, tail, curLen);
	    System.arraycopy(buffer, off + curLen, buf, 0, len - curLen);
	}
	num += len;
	tail = (tail + len) % buf.length;

	if (tailOffset == -1)
	    return 0;

	tailOffset = currentIndex + len;

	PRINT_DEBUG_MSG("RB.write: total = " + getLength());
	if (isFull())
	    notify();
	return len;
    }

    public synchronized int read(byte buffer[], int off, int len)
	    throws IOException {

	PRINT_DEBUG_MSG("RB.read:" + ID + "; total =" + getLength());
	int avail = len;
	if (isEmpty())
	    notify();
	while (isEmpty()) {
	    try {
		wait();
	    } catch (InterruptedException e) {
	    }
	}
	if (len > getLength())
	    avail = getLength();

	if ((head + len) < buf.length)
	    System.arraycopy(buf, head, buffer, off, avail);
	else {
	    int curLen = buf.length - head;
	    System.arraycopy(buf, head, buffer, off, curLen);
	    System.arraycopy(buf, 0, buffer, off + curLen, len - curLen);
	}

	head = (head + avail) % buf.length;

	if (headOffset == -1)
	    return 0;

	headOffset += avail;
	num -= avail;

	PRINT_DEBUG_MSG("RB.read: total " + getLength() + " request = " + len
		+ "; " + avail + " bytes read");
	notify();
	return avail;
    }

    // Assumption: bytes are being written in consecutive order
    public synchronized long seek(long hoff) {
	int diff;

	if (num == 0)
	    return -1;

	PRINT_DEBUG_MSG("RB.seek to (hoff) is " + hoff);
	PRINT_DEBUG_MSG("RB.seek: head: " + head + " tail: " + tail);
	PRINT_DEBUG_MSG("RB.seek: headOffset: " + headOffset + " tailOffset: "
		+ tailOffset);
	if (hoff > headOffset && hoff <= tailOffset) {
	    diff = (int) (hoff - headOffset);
	    head = (head + (int) diff) % buf.length;
	    if (num > diff)
		num -= diff;
	    headOffset += diff; // getHeadOffset();
	}

	if (headOffset != hoff) {
	    flush();
	}

	PRINT_DEBUG_MSG("RB.seek:" + ID + " headOffset is " + headOffset);
	return headOffset;
    }

    //
    // Private api.
    //
    private boolean isEmpty() {
	return num == 0;
    }

    private boolean isFull() {
	return num == buf.length;
    }

    public int getLength() {
	return num;
    }

    public void dispose() {
	buf = null;
    }

    private int getBufferSize() {
	return buf.length;
    }

    // return the number of space in the buffer queue in bytes.
    private int getAvailable() {
	return (buf.length - num);
    }

    public synchronized void flush() {
	PRINT_DEBUG_MSG("flush called");
	downloadThread.pause();
	flushed = true;
	num = 0;
	head = 0;
	tail = 0;
	headOffset = -1;
	tailOffset = -1;
    }

    public synchronized void setDownloadThread(Timer t) {
	downloadThread = t;
    }

    private void PRINT_DEBUG_MSG(String str) {
	if (DEBUG)
	    Log.e("EkhoTV", str);
    }
}
