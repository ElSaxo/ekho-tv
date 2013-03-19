/*
 * @(#)SIException.java	1.13 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service;

/**
 * The base class for exceptions related to service information.
 */
public class SIException extends Exception {

    /**
     * Constructs an <code>SIException</code> with no detail message.
     */
    public SIException() {
	super();
    }

    /**
     * Constructs an <code>SIException</code> with a detail message.
     * 
     * @param reason
     *            The reason why this exception was thrown.
     */
    public SIException(String reason) {
	super(reason);
    }
}
