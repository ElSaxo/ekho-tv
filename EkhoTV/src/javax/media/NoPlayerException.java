/*
 * @(#)NoPlayerException.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * A <code>NoPlayerException</code> is thrown when a <code>PlayerFactory</code>
 * can't find a <code>Player</code> for a particular <CODE>URL</CODE> or
 * <CODE>MediaLocator</CODE>.
 * 
 * @version 1.10, 98/03/28.
 */

public class NoPlayerException extends MediaException {

    public NoPlayerException() {
	super();
    }

    public NoPlayerException(String reason) {
	super(reason);
    }
}
