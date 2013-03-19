/*
 * @(#)ClockStartedError.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * <code>ClockStartedError</code> is thrown by a <i>Started</i>&nbsp;
 * <code>Clock</code> when a method is invoked that is not legal on a
 * <code>Clock</code> in the <i>Started</i> state. For example, this error is
 * thrown if <code>syncStart</code> or <code>setTimeBase</code> is invoked on a
 * <I>Started</I>&nbsp;<code>Clock</code>. <code>ClockStartedError</code> is
 * also thrown if <code>addController</code> is invoked on a
 * <I>Started</I>&nbsp;<code>Player</code>.
 * 
 * @see Player
 * @see Controller
 * @see Clock
 * @version 1.17, 98/03/28.
 */

public class ClockStartedError extends MediaError {

    /**
     * Construct a <CODE>ClockStartedError</CODE> that contains the specified
     * reason message.
     */
    public ClockStartedError(String reason) {
	super(reason);
    }

    /**
     * Construct a <CODE>ClockStartedError</CODE> with no message.
     */
    public ClockStartedError() {
	super();
    }
}
