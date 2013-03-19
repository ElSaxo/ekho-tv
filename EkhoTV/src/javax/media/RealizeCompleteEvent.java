/*
 * @(#)RealizeCompleteEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * A <code>RealizeCompleteEvent</code> is posted when a <code>Controller</code>
 * finishes <I>Realizing</I>. This occurs when a <code>Controller</code> moves
 * from the <i>Realizing</i> state to the <i>Realized</i> state, or as an
 * acknowledgement that the <code>realize</code> method was called and the
 * <code>Controller</code> is already <i>Realized</i>.
 * 
 * @see Controller
 * @see ControllerListener
 * @version 1.16, 98/03/28
 */
public class RealizeCompleteEvent extends TransitionEvent {

    public RealizeCompleteEvent(Controller from, int previous, int current,
	    int target) {
	super(from, previous, current, target);
    }
}
