/*
 * @(#)BadHeaderException.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media;

public class BadHeaderException extends Exception {

    /**
     * BadHeaderException Construct an BadHeaderException without a message.
     */
    public BadHeaderException() {
	super();
    }

    /**
     * BadHeaderException Construct an BadHeaderException with a detail message.
     * A detail message is a string that describes this particular exception.
     * 
     * @param s
     *            detail message string
     */
    public BadHeaderException(String s) {
	super(s);
    }
}
