/*
 * @(#)InvalidTrackIDException.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media.content;

public class InvalidTrackIDException extends Exception {

    /**
     * InvalidTrackIDException Construct an InvalidTrackIDException without a
     * message.
     */
    public InvalidTrackIDException() {
	super();
    }

    /**
     * InvalidTrackIDException Construct an InvalidTrackIDException with a
     * detail message. A detail message is a string that describes this
     * particular exception.
     * 
     * @param s
     *            detail message string
     */
    public InvalidTrackIDException(String s) {
	super(s);
    }
}
