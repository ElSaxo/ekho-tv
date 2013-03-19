/*
 * @(#)MediaException.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * A <code>MediaException</code> indicates an unexpected error condition in a
 * JavaMedia method.
 * 
 * @version 1.11, 98/03/28
 */

public class MediaException extends Exception {

    public MediaException() {
	super();
    }

    public MediaException(String reason) {
	super(reason);
    }
}
