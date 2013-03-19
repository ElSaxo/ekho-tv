/*
 * @(#)StopByRequestEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * A <code>StopByRequestEvent</code> indicates that the <code>Controller</code>
 * has stopped in response to a <code>stop</code> call. This event is posted as
 * an acknowledgement even if the <code>Controller</code> is already
 * <i>Stopped</i>.
 * 
 * @see Controller
 * @see ControllerListener
 * @version 1.13, 98/03/28.
 */

public class StopByRequestEvent extends StopEvent {

    public StopByRequestEvent(Controller from, int previous, int current,
	    int target, Time mediaTime) {
	super(from, previous, current, target, mediaTime);
    }
}
