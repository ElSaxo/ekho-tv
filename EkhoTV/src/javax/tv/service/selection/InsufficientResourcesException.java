/*
 * @(#)InsufficientResourcesException.java	1.15 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.selection;

/**
 * <code>InsufficientResourcesException</code> is generated when sufficient
 * resources for an operation are not available.
 **/
public class InsufficientResourcesException extends ServiceContextException {

    /**
     * 
     * Constructs an <code>InsufficientResourcesException</code> with no detail
     * message.
     */
    public InsufficientResourcesException() {
	super();
    }

    /**
     * 
     * Constructs an <code>InsufficientResourcesException</code> with a detail
     * message.
     * 
     * @param s
     *            The detail message.
     **/
    public InsufficientResourcesException(String s) {
	super(s);
    }
}
