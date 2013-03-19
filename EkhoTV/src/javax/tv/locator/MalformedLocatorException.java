/*
 * @(#)MalformedLocatorException.java	1.8 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.locator;

/**
 * This exception is thrown to indicate that a malformed locator string has been
 * used. Either no legal mapping could be determined for the specified string,
 * or the string could not be parsed.
 */
public class MalformedLocatorException extends Exception {

    /**
     * Constructs a <code>MalformedLocatorException</code> with no detail
     * message.
     */
    public MalformedLocatorException() {
	super();
    }

    /**
     * 
     * Constructs a <code>MalformedLocatorException</code> with the specified
     * detail message.
     * 
     * @param reason
     *            The reason the exception was raised.
     */
    public MalformedLocatorException(String reason) {
	super(reason);
    }
}
