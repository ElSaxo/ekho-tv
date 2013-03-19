/*
 * @(#)RAMCachingInputStream.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media.protocol.reliable.caching;

import java.io.IOException;

import javax.media.protocol.PullSourceStream;

import android.util.Log;

import com.sun.tv.media.MediaPullSourceStream;
import com.sun.tv.media.util.RingBuffer;
import com.sun.tv.media.util.Timer;

public class RAMCachingInputStream extends CachingInputStream {

    private RingBuffer rb;
    private int frameSize;

    public RAMCachingInputStream(PullSourceStream p) {
	super(p);
    }

    public void setBufferSize(int lm) {
	bufferSize = -1;
	if (lm > 0) {
	    rb = new RingBuffer(lm,
		    ((MediaPullSourceStream) sourceStream).tell());
	    rb.setID(ID); // debugging
	} else
	    rb = null;

	frameSize = lm / 3;

	if (frameSize > 20000)
	    frameSize = 20000;

	PRINT_DEBUG_MSG("RAM: setBufferSize" + ID + ": buffer size = " + lm);
	if (rb != null)
	    bufferSize = lm;
    }

    public long getStartAvail() {
	long offset = -1L;
	if (rb != null)
	    offset = rb.getStart();
	return offset;
    }

    public long getEndAvail() {
	long offset = -1L;
	if (rb != null)
	    offset = rb.getEnd();
	return offset;
    }

    public int download() throws IOException {
	int bytesRead = 0;
	int off = 0, totalWriteTo = 0;
	int writeTo = 0, request;
	long offset = 0;
	byte buffer[] = new byte[frameSize];

	PRINT_DEBUG_MSG("DN1 ###  RAMC.download (" + ID + "P)" + this);

	rb.setDownloadThread((Timer) Thread.currentThread());
	offset = ((MediaPullSourceStream) sourceStream).tell();
	if ((bytesRead = sourceStream.read(buffer, 0, buffer.length)) != -1) {
	    PRINT_DEBUG_MSG("DN2 ### RAMC.download (" + ID
		    + ") sourceStream has = " + bytesRead + " offset = "
		    + offset);
	    rb.setCurrentIndex(offset);
	    request = bytesRead;
	    while (totalWriteTo != bytesRead) {
		writeTo = rb.write(buffer, off, request);
		totalWriteTo += writeTo;
		off += writeTo;
		request -= writeTo;
	    }
	} else {
	    eosReached = true;
	    Timer dn = (Timer) Thread.currentThread();
	    dn.pause();
	}

	PRINT_DEBUG_MSG("DN3 ###  RAMC.download (" + ID + "P) " + totalWriteTo
		+ " bytes");
	return writeTo;
    }

    // Read Method
    public int read(byte buffer[], int off, int len) throws IOException {

	PRINT_DEBUG_MSG("RD1 ###  RAMC.read (" + ID + "C) request = " + len
		+ " " + this);
	int actualReadLen = -1;
	if (rb == null || (eosReached == true && rb.getLength() == 0))
	    return actualReadLen;

	actualReadLen = rb.read(buffer, off, len);
	PRINT_DEBUG_MSG("RD2 ###  RAMC.read (" + ID + "C)" + actualReadLen
		+ " bytes read");
	return actualReadLen;
    }

    // seek
    // set the current pointer position
    public long seek(long l) {
	if (rb != null)
	    return (rb.seek(l));
	return -1L;
    }

    public long getContentLength() {
	if (rb == null)
	    return 0;
	return rb.getLength();
    }

    public void dispose() {
	Log.e("EkhoTV", "RAMC.dispose()");
	if (rb != null)
	    rb.dispose();
	rb = null;
    }
}
