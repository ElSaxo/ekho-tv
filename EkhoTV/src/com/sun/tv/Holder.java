/*
 * @(#)Holder.java	1.8 08/09/15
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 */

package com.sun.tv;

import java.util.Vector;

/**
 * Holder:
 * 
 * An object holder for the producer/consumer model of request/result access.
 * Workes as a queue.
 * 
 */
class Holder extends Object {

    private Vector vector = new Vector();

    /**
     * put -- synchronized put method will wait until there is nothing in this
     * holder. As soon as it's notified that the holder is empty, it'll change
     * the availability to true and set the object to be the one to be held.
     * 
     * @param The
     *            object that needs to be placed in the holder.
     */
    public synchronized void put(Object obj) {
	vector.add(obj);
	notifyAll();
    }

    /**
     * get -- synchronized get method will wait until there is something in this
     * holder. As soon as it's notified of the availability, it'll change the
     * availability to false and return the object.
     * 
     * @return The object that's placed in the holder.
     */
    public synchronized Object get() {
	while (vector.isEmpty()) {
	    try {
		wait();
	    } catch (InterruptedException e) {
	    }
	}
	Object obj = vector.remove(0);
	notifyAll();
	return obj;
    }

    public synchronized void waitTillQueueIsEmpty() {
	while (!vector.isEmpty()) {
	    try {
		wait();
	    } catch (InterruptedException e) {
	    }
	}
    }
}
