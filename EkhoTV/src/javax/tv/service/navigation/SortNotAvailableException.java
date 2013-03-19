/*
 * @(#)SortNotAvailableException.java	1.14 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.navigation;

import javax.tv.service.SIException;

/**
 * This exception indicates that the requested sorting method is not available
 * for the particular <code>ServiceList</code>, for example, sorting by service
 * numbers.
 * 
 * @see ServiceList
 */
public class SortNotAvailableException extends SIException {

    /**
     * Constructs a <code>SortNotAvailableException</code> with no detail
     * message.
     */
    public SortNotAvailableException() {
	super();
    }

    /**
     * Constructs a <code>SortNotAvailableException</code> with a detail
     * message.
     * 
     * @param reason
     *            The reason this exception was thrown.
     */
    public SortNotAvailableException(String reason) {
	super(reason);
    }
}
