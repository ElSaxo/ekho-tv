/*
 * @(#)IncompatibleTimeBaseException.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * An <CODE>IncompatibleTimeBaseException</CODE> is generated when
 * <CODE>Clock.setTimeBase</CODE> is invoked using a <CODE>TimeBase</CODE> that
 * the <CODE>Clock</CODE> cannot support. This happens for certain types of
 * <CODE>Players</CODE> that can only be driven by their own internal clocks,
 * such as certain commercial video servers.
 * <p>
 * 
 * <B>Note:</B> A <CODE>Player</CODE> might throw this exception when
 * <CODE>addController</CODE> is called because of the implied
 * <CODE>setTimeBase</CODE> in <CODE>addController</CODE>.
 * 
 * @see Clock
 * @see Player
 * @version 1.11, 98/03/28.
 * 
 */

public class IncompatibleTimeBaseException extends MediaException {

    public IncompatibleTimeBaseException() {
	super();
    }

    public IncompatibleTimeBaseException(String reason) {
	super(reason);
    }
}
