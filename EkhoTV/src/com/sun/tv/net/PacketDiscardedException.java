/*
 * @(#)PacketDiscardedException.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.net;

/**
 * Thrown when an IP packet passed up by lower layer protocol is discarded
 */

class PacketDiscardedException extends java.io.IOException {

    /**
     * Constructs an exception with no specified detail message.
     */

    PacketDiscardedException() {
	super();
    }

    /**
     * Constructs an exception with the specified detail message.
     * 
     * @param s
     *            the detail message
     */

    PacketDiscardedException(String s) {
	super(s);
    }

}
