/*
 * @(#)DataLostException.java	1.9 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.protocol;

import java.io.IOException;

/**
 * Signals that streaming data has been lost.
 * 
 */
public class DataLostException extends IOException {

    /**
     * Constructs the exception with no detail message.
     * 
     */
    public DataLostException() {
	super();
    }

    /**
     * Constructs the exception with the given detail message.
     * 
     * @param reason
     *            The reason for the exception.
     */
    public DataLostException(String reason) {
	super(reason);
    }
}
