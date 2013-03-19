/*
 * @(#)IncompatibleSourceException.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * An <CODE>IncompatibleSourceException</CODE> is thrown by a
 * <CODE>MediaHandler</CODE> when <code>setSource</code> is invoked and the
 * <code>MediaHandler</code> cannot support the <code>DataSource</code>.
 * <p>
 **/

public class IncompatibleSourceException extends MediaException {

    public IncompatibleSourceException() {
	super();
    }

    public IncompatibleSourceException(String reason) {
	super(reason);
    }
}
