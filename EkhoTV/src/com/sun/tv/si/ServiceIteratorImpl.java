/*
 * @(#)ServiceIteratorImpl.java	1.5 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.si;

import java.util.NoSuchElementException;
import java.util.Vector;

import javax.tv.service.Service;
import javax.tv.service.navigation.ServiceIterator;
import javax.tv.service.navigation.ServiceList;

/**
 * <code>ServiceIterator</code> permits iteration over an ordered list of
 * <code>Service</code> objects. Applications may use the
 * <code>ServiceIterator</code> interface to browse a <code>ServiceList</code>
 * forward or backward.
 * <p>
 * 
 * Upon initial usage, <code>hasPrevious()</code> will return <code>false</code>
 * and <code>nextService()</code> will return the first <code>Service</code> in
 * the list, if present.
 * 
 * @see ServiceList
 */
public class ServiceIteratorImpl implements ServiceIterator {

    int theIndex;
    Vector services;

    /**
  *
  */
    public ServiceIteratorImpl(Vector list) {
	if (list != null) {
	    services = list;
	} else {
	    services = new Vector();
	}
	theIndex = -1;
    }

    /**
     * 
     * Resets the iterator to the beginning of the list, such that
     * <code>hasPrevious</code> returns <code>false</code>.
     * 
     * */
    public void toBeginning() {
	theIndex = -1;
    }

    /**
     * 
     * Sets the iterator to the end of the list, such that <code>hasNext</code>
     * returns <code>false</code>.
     */
    public void toEnd() {
	theIndex = services.size();
    }

    /**
     * 
     * Reports the next <code>Service</code> object in the list. This method may
     * be called repeatedly to iterate through the list.
     * 
     * @return The <code>Service</code> object at the next position in the list.
     * 
     * @throws NoSuchElementException
     *             If the iteration has no next <code>Service</code>.
     */
    public Service nextService() {
	if (hasNext() == false) {
	    String msg = "next index (" + theIndex + ") is out of range.";
	    throw new NoSuchElementException(msg);
	}
	theIndex++;
	return (Service) services.elementAt(theIndex);
    }

    /**
     * 
     * Reports the previous <code>Service</code> object in the list. This method
     * may be called repeatedly to iterate through the list in reverse order.
     * 
     * @return The <code>Service</code> object at the previous position in the
     *         list.
     * 
     * @throws NoSuchElementException
     *             If the iteration has no previous <code>Service</code>.
     */
    public Service previousService() {
	if (hasPrevious() == false) {
	    String msg = "next index (" + theIndex + ") is out of range.";
	    throw new NoSuchElementException(msg);
	}
	theIndex--;
	return (Service) services.elementAt(theIndex);
    }

    /**
     * Tests if there is a <code>Service</code> in the next position in the
     * list.
     * 
     * @return <code>true</code> if there is a <code>Service</code> in the next
     *         position in the list; <code>false</code> otherwise.
     */
    public boolean hasNext() {
	int index = theIndex + 1;
	return !(index < 0 || index > services.size() - 1);
    }

    /**
     * 
     * Tests if there is a <code>Service</code> in the previous position in the
     * list.
     * 
     * @return <code>true</code> if there is a <code>Service</code> in the
     *         previous position in the list; <code>false</code> otherwise.
     */
    public boolean hasPrevious() {
	int index = theIndex - 1;
	return !(index < 0 || index > services.size() - 1);
    }
}
