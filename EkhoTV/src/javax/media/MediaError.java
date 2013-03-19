/*
 * @(#)MediaError.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * A <code>MediaError</code> indicates an error condition that occurred through
 * incorrect usage of the API. You should not check for <code>MediaErrors</code>
 * .
 * 
 * @version 1.13, 98/03/28.
 */

public class MediaError extends Error {

    public MediaError() {
	super();
    }

    public MediaError(String reason) {
	super(reason);
    }
}
