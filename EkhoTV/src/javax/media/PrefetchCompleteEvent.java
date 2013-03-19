/*
 * @(#)PrefetchCompleteEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * A <code>PrefetchCompleteEvent</code> is posted when a <code>Controller</code>
 * finishes <I>Prefetching</I>. This occurs when a <code>Controller</code> moves
 * from the <i>Prefetching</i> state to the <i>Prefetched</i> state, or as an
 * acknowledgement that the <code>prefetch</code> method was called and the
 * <code>Controller</code> is already <i>Prefetched</i>.
 * 
 * @see Controller
 * @see ControllerListener
 * @version 1.22, 98/03/28.
 */
public class PrefetchCompleteEvent extends TransitionEvent {

    public PrefetchCompleteEvent(Controller from, int previous, int current,
	    int target) {
	super(from, previous, current, target);
    }
}
