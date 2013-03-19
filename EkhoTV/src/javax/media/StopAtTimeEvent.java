/*
 * @(#)StopAtTimeEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * A <code>StopAtTimeEvent</code> indicates that the <code>Controller</code> has
 * stopped because it reached its stop time.
 * 
 * @see Controller
 * @see ControllerListener
 * @version 1.13, 98/03/28.
 */

public class StopAtTimeEvent extends StopEvent {

    public StopAtTimeEvent(Controller from, int previous, int current,
	    int target, Time mediaTime) {
	super(from, previous, current, target, mediaTime);
    }
}
