/*
 * @(#)ServiceIterator.java	1.11 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.navigation;

import java.util.NoSuchElementException;

import javax.tv.service.Service;

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
public interface ServiceIterator {

    /**
     * 
     * Resets the iterator to the beginning of the list, such that
     * <code>hasPrevious()</code> returns <code>false</code> and
     * <code>nextService()</code> returns the first <code>Service</code> in the
     * list (if the list is not empty).
     * 
     * */
    public abstract void toBeginning();

    /**
     * 
     * Sets the iterator to the end of the list, such that
     * <code>hasNext()</code> returns <code>false</code> and
     * <code>previousService()</code> returns the last <code>Service</code> in
     * the list (if the list is not empty).
     */
    public abstract void toEnd();

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
    public abstract Service nextService();

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
    public abstract Service previousService();

    /**
     * Tests if there is a <code>Service</code> in the next position in the
     * list.
     * 
     * @return <code>true</code> if there is a <code>Service</code> in the next
     *         position in the list; <code>false</code> otherwise.
     */
    public abstract boolean hasNext();

    /**
     * 
     * Tests if there is a <code>Service</code> in the previous position in the
     * list.
     * 
     * @return <code>true</code> if there is a <code>Service</code> in the
     *         previous position in the list; <code>false</code> otherwise.
     */
    public abstract boolean hasPrevious();

}
