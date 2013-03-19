/*
 * @(#)StopTimeSetError.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * <CODE>StopTimeSetError</CODE> is thrown when the stop time has been set on a
 * <I>Started</I>&nbsp;<CODE>Clock</CODE> and <code>setStopTime</code> is
 * invoked again.
 * 
 * @version 1.12, 98/03/28.
 */

public class StopTimeSetError extends MediaError {

    public StopTimeSetError(String reason) {
	super(reason);
    }
}
