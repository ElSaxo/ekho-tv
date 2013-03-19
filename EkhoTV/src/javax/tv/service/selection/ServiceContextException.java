/*
 * @(#)ServiceContextException.java	1.14 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.selection;

/**
 * The base class for exceptions related to service contexts.
 */

public class ServiceContextException extends java.lang.Exception {

    /**
     * Constructs a <code>ServiceContextException</code> with no detail message.
     */
    public ServiceContextException() {
	super();
    }

    /**
     * Constructs a <code>ServiceContextException</code> with a detail message.
     * 
     * @param reason
     *            The reason this exception was thrown.
     */
    public ServiceContextException(String reason) {
	super(reason);
    }
}
