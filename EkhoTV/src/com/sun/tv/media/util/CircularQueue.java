/*
 * @(#)CircularQueue.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.util;


/**
 * CircularQueue It implements a circular FIFO queue of objects.
 * 
 * @version 1.6, 98/03/28
 */
public class CircularQueue {

    private Object buf[]; // A buffer queue of objects.
    private int head = 0, tail = 0; // Put to tail, read from head.
    private int num = 0;

    public CircularQueue(int n) {
	buf = new Object[n];
    }

    /**
     * Get an object from the queue.
     */
    public synchronized Object get() {
	if (num == 0)
	    return null;
	Object obj = buf[head];
	buf[head] = null;
	head = (head + 1) % buf.length;
	num--;
	return obj;
    }

    /**
     * Put an object into the queue.
     */
    public synchronized void put(Object obj) {
	if (num == buf.length) {
	    throw new ArrayIndexOutOfBoundsException("CircularQueue.put()");
	}
	buf[tail] = obj;
	tail = (tail + 1) % buf.length;
	num++;
    }

    public boolean isEmpty() {
	return num == 0;
    }

    public boolean isFull() {
	return num == buf.length;
    }

    public int length() {
	return num;
    }

}
