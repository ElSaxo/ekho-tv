/*
 * @(#)ClockStoppedException.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * A <code>ClockStoppedException</code> is thrown when a method that expects the
 * <I>Clock</I> to be <I>Started</I> is called on a <I>Stopped</I>&nbsp;
 * <code>Clock</code>. For example, this exception is thrown if
 * <code>mapToTimeBase</code> is called on a <I>Stopped</I>&nbsp;
 * <code>Clock</code>.
 * 
 * @version 1.14, 98/03/28
 */

public class ClockStoppedException extends MediaException {

    public ClockStoppedException() {
	super();
    }

    public ClockStoppedException(String reason) {
	super(reason);
    }
}
