/*
 * @(#)FilterNotSupportedException.java	1.13 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.navigation;

import javax.tv.service.SIException;

/**
 * This exception indicates that the specified <code>ServiceFilter</code> is not
 * supported.
 * 
 * @see javax.tv.service.navigation.ServiceFilter
 */
public class FilterNotSupportedException extends SIException {

    /**
     * Constructs a <code>FilterNotSupportedException</code> with no detail
     * message.
     */
    public FilterNotSupportedException() {
	super();
    }

    /**
     * Constructs a <code>FilterNotSupportedException</code> with a detail
     * message.
     * 
     * @param reason
     *            The reason why this exception was thrown.
     */
    public FilterNotSupportedException(String reason) {
	super(reason);
    }
}
