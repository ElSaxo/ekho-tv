/*
 * @(#)NewRFCachingInputStream.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media.protocol.reliable.caching;

import java.io.File;
import java.io.IOException;

import javax.media.protocol.PullSourceStream;
import javax.media.protocol.Seekable;

import android.util.Log;

import com.sun.tv.media.MediaPullSourceStream;
import com.sun.tv.media.util.LoopThread;
import com.sun.tv.media.util.RingFile;

public class NewRFCachingInputStream extends CachingInputStream implements
	Seekable {
    protected RingFile rb;
    protected int frameSize;
    protected File f;
    private int seekPosition = 0;

    public NewRFCachingInputStream(PullSourceStream p, String pname) {
	super(p);
	f = new File(pname);
	DEBUG = false;
    }

    public boolean isRandomAccess() {
	return true;
    }

    public void setRingBuffer(RingFile f) {
	rb = f;
    }

    public Object clone() {
	NewRFCachingInputStream str = new NewRFCachingInputStream(sourceStream,
		f.getAbsolutePath() + f.getName());

	str.setRingBuffer(rb);
	return str;
    }

    public RingFile getRingBuffer() {
	return rb;
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

    public boolean canReadFromCache() {
	if (rb != null)
	    return !rb.willReadBlock();
	else
	    return false;
    }

    public void waitUntilCacheReady() {
	if (rb != null)
	    rb.waitUntilReadWontBlock();
    }

    public boolean willReadBlock() {
	if (rb != null)
	    return rb.willReadBlock();
	else
	    return false;
    }

    public void setBufferSize(int watermark) {
	int ringFileSize = 1000000000; // About 1000 Megabytes
	bufferSize = -1;
	if (watermark < 50000)
	    watermark = 50000;
	// Log.e("EkhoTV", "CALLING setBufferSize: watermark " + watermark);
	rb = new RingFile(f, ringFileSize);
	frameSize = 20000;
	if (rb != null) {
	    if (rb.isValid())
		bufferSize = ringFileSize;
	    rb.setID(ID); // debugging
	    rb.setLowWatermark(0);
	    rb.setHighWatermark(watermark);
	}
    }

    public int available() {
	if (rb != null) {
	    if (rb.drainCondition())
		return 0;
	    else
		return rb.getLength();
	} else
	    return 0;
    }

    public void sync() {
	long offset = 0;
	eosReached = false;
	rb.setEnd(-1L);
	offset = ((MediaPullSourceStream) sourceStream).tell();
	if (rb.flushed()) {
	    Log.e("EkhoTV",
		    "!!!!!!Ring File flushed, resetting the offset!!!!!!!!!!!!!!!!!!!");
	    rb.setCurrentIndex(offset);
	}
    }

    // Note: The thread that calls dispose, first kills (using stop() method)
    // the thread that calls download, i.e download thread.
    public int download() throws IOException {
	int bytesRead = 0;
	int off = 0, totalWriteTo = 0;
	int writeTo = -1, request;
	byte buffer[] = new byte[frameSize];

	try {
	    rb.setDownloadThread((LoopThread) Thread.currentThread());
	    // Log.e("EkhoTV", "NewRFCachingInputStream: sourceStream is  " +
	    // sourceStream);
	    if ((bytesRead = sourceStream.read(buffer, 0, buffer.length)) != -1) {
		if (DEBUG)
		    PRINT_DEBUG_MSG("### RAMC.download (" + ID + ") request = "
			    + bytesRead);
		// rb.setCurrentIndex(offset);
		request = bytesRead;
		while (totalWriteTo != bytesRead) {
		    writeTo = rb.write(buffer, off, request);
		    if (writeTo == -1) {
			rb.setEos();
			System.err
				.println("!!!!NO MORE DISKSPACE AVAILABLE FOR CACHING!!!! ");

			eosReached = true;
			// $$ TODO: Use a return value like -2 to distinguish
			// between
			// -1 (EOF) and -2 (No diskspace for caching)
			// This can be used to popup a warning to the user.
			return -1;
		    }
		    totalWriteTo += writeTo;
		    off += writeTo;
		    request -= writeTo;
		}
	    } else {
		rb.setEos();
		// Log.e("EkhoTV", "EOS reached");
		eosReached = true;
	    }
	} catch (NullPointerException e) {
	    LoopThread lt = ((LoopThread) Thread.currentThread());
	    lt.kill();
	}
	if (DEBUG)
	    PRINT_DEBUG_MSG("###  RAMC.download (" + ID + "P) " + totalWriteTo
		    + " bytes");
	return bytesRead;
    }

    // Read Method
    public int read(byte buffer[], int off, int len) throws IOException {

	if (DEBUG)
	    PRINT_DEBUG_MSG("###  RAMC.read (" + ID + "C) request = " + len);
	int actualReadLen = -1;

	if (rb == null) {
	    // Log.e("EkhoTV", "Ring File is null");
	    return -1;
	}
	actualReadLen = rb.read(buffer, off, len);
	if (DEBUG)
	    PRINT_DEBUG_MSG("###  RAMC.read " + actualReadLen + " bytes read");

	if ((actualReadLen == 0) && (rb.isEosReached())) {
	    return -1;
	}
	return actualReadLen;
    }

    public boolean canSeek(long where) {
	{
	    if (!canReadFromCache()) {
		return false; // / Bug fix
	    }
	}
	if (rb != null) {
	    int watermark = rb.getHighWatermark();
	    // $$ TODO: cleanup
	    {
		watermark = 0;
	    }
	    return ((rb.getFileSize() >= (where + watermark)) || rb
		    .isEosReached());
	} else {
	    return false;
	}
    }

    public void newSeekLocation(long offset) {
	seekPosition = (int) offset;
    }

    public long seek(long where) {
	seekPosition = (int) where;

	try {
	    if (rb != null) {
		// $$ TODO: cleanup
		int watermark = rb.getHighWatermark();
		{
		    watermark = 0;
		}
		while ((rb.getFileSize() < (seekPosition + watermark))
			&& (!rb.isEosReached())) {
		    try {
			// Log.e("EkhoTV", "rfc: seek: sleep for 100msec");
			Thread.currentThread().sleep(100);
		    } catch (InterruptedException e) {
		    }
		}
		if (seekPosition >= rb.getFileSize())
		    seekPosition = rb.getFileSize() - 1;
		long whereAmI = rb.seek(seekPosition);
		return whereAmI;
	    } else {
		return -1L;
	    }
	} catch (NullPointerException e) {
	    // Log.e("EkhoTV",
	    // "newrf: seek: caught NullPointerException, rb is " + rb);
	    return -1L;
	}
    }

    public long skip(long distance) throws IOException {
	// Log.e("EkhoTV", "skip: Asked to skip " + distance);
	return seek(tell() + distance);
    }

    public long tell() {
	if (rb != null) {
	    return rb.tell();
	} else {
	    return -1L;
	}
    }

    public long getContentLength() {
	if (rb == null)
	    return 0L;
	return (long) rb.getFileSize();
    }

    public void dispose() {
	// Log.e("EkhoTV", Thread.currentThread() + " newrf: In dispose: rb is "
	// + rb);
	if (rb != null)
	    rb.dispose();
	rb = null;
	try {
	    ((MediaPullSourceStream) sourceStream).close();
	} catch (IOException e) {
	}
    }
}
