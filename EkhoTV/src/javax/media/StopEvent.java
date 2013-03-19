/*
 * @(#)StopEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * <code>StopEvent</code> is a <code>ControllerEvent</code> that indicates that
 * a <code>Controller</code> has stopped.
 * 
 * @see Controller
 * @see ControllerListener
 * @version 1.30, 98/03/28
 */
public class StopEvent extends TransitionEvent {

    private Time mediaTime;

    /**
     * @param from
     *            The <code>Controller</code> that generated this event.
     * @param mediaTime
     *            The <I>media time</I> at which the <code>Controller</code>
     *            stopped.
     */
    public StopEvent(Controller from, int previous, int current, int target,
	    Time mediaTime) {
	super(from, previous, current, target);
	this.mediaTime = mediaTime;
    }

    /**
     * Get the clock time (<I>media time</I>) that was passed into the
     * constructor.
     * 
     * @return The <i>mediaTime</i> at which the <code>Controller</code>
     *         stopped.
     */
    public Time getMediaTime() {
	return mediaTime;
    }

    /**
     * Returns the String representation of this event's values.
     */
    public String toString() {
	return getClass().getName() + "[source=" + eventSrc + ",previous="
		+ stateName(previousState) + ",current="
		+ stateName(currentState) + ",target=" + stateName(targetState)
		+ ",mediaTime=" + mediaTime + "]";
    }
}
