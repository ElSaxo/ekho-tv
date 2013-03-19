/*
 * @(#)RestartingEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * A <code>RestartingEvent</code> indicates that a <code>Controller</code> has
 * moved from the <i>Started</i> state back to the <i>Prefetching</i> state (a
 * <i>Stopped</i> state) and intends to return to the <i>Started</i> state when
 * <i>Prefetching</i> is complete. This occurs when a <i>Started</i>&nbsp;
 * <code>Player</code> is asked to change its rate or media time and to fulfill
 * the request must prefetch its media again.
 * 
 * @see Controller
 * @see ControllerListener
 * @version 1.16, 98/03/28.
 */

public class RestartingEvent extends StopEvent {

    public RestartingEvent(Controller from, int previous, int current,
	    int target, Time mediaTime) {
	super(from, previous, current, target, mediaTime);
    }
}
