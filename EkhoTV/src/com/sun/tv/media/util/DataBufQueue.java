/*
 * @(#)DataBufQueue.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.util;

import com.sun.tv.media.Data;

/**
 * DataBufQueue It implements a circular FIFO queue of data buffers. It
 * implements the blocking semantics for used by the putData(), getContainer()
 * etc. Each buffer is like a bucket. At any given time, there are filled and
 * free buckets. getFree() will get a free bucket so you can fill it with data.
 * After you fill a bucket, you call putback to check the filled bucket back to
 * the queue. Then call getFilled() to retrieve the next filled bucket to use.
 * Invariant: # of filled + # of freed <= n
 * 
 * @version 1.16, 98/03/28
 */
public class DataBufQueue {

    // Use circular queue instead of vector for performance.
    private CircularQueue filled;
    private CircularQueue free;
    private Data[] dataArray;
    private int numBuffers = 0;
    private int maxAllocated = 0;
    private boolean closed = false;

    public DataBufQueue(int n) {
	filled = new CircularQueue(n);
	free = new CircularQueue(n);
	dataArray = new Data[n];
	maxAllocated = n;
    }

    // Utility method for debugging
    public int getNumBuffers() {
	return numBuffers;
    }

    /*
     * The number of data buffer added to the queue should be the same as the
     * number used to initially construct the queue.
     */
    public void addNewBuffer(Data data) {
	if (numBuffers >= maxAllocated) {
	    System.err.println("DataBufQueue is full: " + maxAllocated);
	    return;
	}
	dataArray[numBuffers] = data;
	numBuffers++;
	free.put(data);
	/**
	 * DEBUG Log.e("EkhoTV", "addNewBuffer: " + data + ": " +
	 * data.getBuffer() + ": " + data.getBufferSize());
	 **/
    }

    /*
     * Remove the given buffer from the management of this queue.
     */
    public void removeOldBuffer(Data data) {
	int i;
	for (i = 0; i < numBuffers; i++) {
	    if (dataArray[i] == data) {
		dataArray[i] = null;

		// shift the dataArray to fill the deleted spot.
		for (; i < numBuffers - 1; i++)
		    dataArray[i] = dataArray[i + 1];
		numBuffers--;
		dataArray[numBuffers] = null;
		break;
	    }
	}
    }

    public void replaceOldBuffer(Data oldData, Data newData) {
	removeOldBuffer(oldData);
	if (numBuffers >= dataArray.length)
	    numBuffers = dataArray.length - 1;
	dataArray[numBuffers] = newData;
	numBuffers++;
    }

    /*
     * Remove an existing buffer from the management of this queue.
     */
    public Data removeOldBuffer() {
	Data data;
	if ((data = tryGetFree()) != null) {
	    removeOldBuffer(data);
	}
	return data;
    }

    // Reset the queue. This method may not be needed.
    public synchronized void reset() {
	Data data;
	while ((data = tryGetFilled()) != null)
	    putbackFree(data);
    }

    /**
     * Return true if none of the buffer is filled.
     */
    public boolean noFilled() {
	return filled.isEmpty();
    }

    /**
     * Return true if all of the buffers are filled.
     */
    public boolean allFilled() {
	return filled.isFull();
    }

    /**
     * Return true if no free buffer is available to be filled.
     */
    public boolean noFree() {
	return free.isEmpty();
    }

    /**
     * Get a free buffer from the queue to fill it with data. Block if there is
     * no free buffer.
     */
    public synchronized Data getFree() {
	try {
	    while (free.isEmpty()) {
		wait();
	    }
	} catch (InterruptedException e) {
	    System.err.println("getFree() " + e);
	}
	boolean needNotify = false;
	if (free.isFull())
	    needNotify = true;
	Data data = (Data) free.get();
	if (needNotify)
	    notifyAll();
	return data;
    }

    /**
     * Get the next filled buffer to use. Block if there is no filled buffer.
     */
    public synchronized Data getFilled() {
	try {
	    while (filled.isEmpty()) {
		wait();
		if (closed) { // abort condition
		    return null;
		}
	    }
	} catch (InterruptedException e) {
	    System.err.println("getFilled() " + e);
	}
	boolean needNotify = false;
	if (filled.isFull())
	    needNotify = true;
	Data data = (Data) filled.get();
	if (needNotify)
	    notifyAll();
	return data;
    }

    /**
     * Get a free buffer if there's one available. Otherwise just returns null.
     */
    public synchronized Data tryGetFree() {
	if (noFree())
	    return null;
	return getFree();
    }

    /**
     * Get a filled buffer if there's one available. Otherwise just returns
     * null.
     */
    public synchronized Data tryGetFilled() {
	if (noFilled())
	    return null;
	return getFilled();
    }

    /**
     * Put back a filled buffer to be used later. Block if the filled queue is
     * full.
     */
    public synchronized void putbackFilled(Data data) {
	try {
	    while (filled.isFull()) {
		wait();
	    }
	} catch (InterruptedException e) {
	    System.err.println("putbackFilled() " + e);
	}

	boolean needNotify = false;
	if (filled.isEmpty())
	    needNotify = true;
	filled.put(data);
	if (needNotify)
	    notifyAll();
    }

    /**
     * Put back a free buffer to be used later. Block if the freed queue is
     * full.
     */
    public synchronized void putbackFree(Data data) {
	try {
	    while (free.isFull()) {
		wait();
	    }
	} catch (InterruptedException e) {
	    System.err.println("putbackFree() " + e);
	}

	boolean needNotify = false;
	if (free.isEmpty())
	    needNotify = true;
	free.put(data);
	if (needNotify)
	    notifyAll();
    }

    // Utility method for debugging
    public int getFreeLength() {
	return free.length();
    }

    // Utility method for debugging
    public int getFilledLength() {
	return filled.length();
    }

    public synchronized void close() {
	closed = true;
	notifyAll();
    }
}
