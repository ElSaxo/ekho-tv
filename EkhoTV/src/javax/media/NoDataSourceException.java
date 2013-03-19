/*
 * @(#)NoDataSourceException.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * A <code>NoDataSourceException</code> is thrown when a <code>DataSource</code>
 * can't be found for a particular <CODE>URL</CODE> or <CODE>MediaLocator</CODE>
 * .
 * 
 * @version 1.10, 98/03/28.
 */

public class NoDataSourceException extends MediaException {

    public NoDataSourceException() {
	super();
    }

    public NoDataSourceException(String reason) {
	super(reason);
    }
}
