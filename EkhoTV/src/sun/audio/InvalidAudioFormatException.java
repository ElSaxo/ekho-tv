/*
 * @(#)InvalidAudioFormatException.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package sun.audio;

import java.io.IOException;

/**
 * Signals an invalid audio stream for the stream handler.
 */
class InvalidAudioFormatException extends IOException {
    /**
     * Constructor.
     */
    public InvalidAudioFormatException() {
	super();
    }

    /**
     * Constructor with a detail message.
     */
    public InvalidAudioFormatException(String s) {
	super(s);
    }
}
