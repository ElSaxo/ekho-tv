/*
 * @(#)DurationUpdateEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * <CODE>DurationUpdateEvent</CODE> is posted by a <CODE>Controller</CODE> when
 * its duration changes.
 * 
 * @see Controller
 * @see ControllerListener
 * @version 1.12, 98/03/28.
 */
public class DurationUpdateEvent extends ControllerEvent {

    Time duration;

    public DurationUpdateEvent(Controller from, Time newDuration) {
	super(from);
	duration = newDuration;
    }

    /**
     * Get the duration of the media that this <CODE>Controller</CODE> is using.
     * 
     * @return The duration of this <CODE>Controller's</CODE> media.
     */
    public Time getDuration() {
	return duration;
    }

    /**
     * Returns the String representation of this event's values.
     */
    public String toString() {
	return getClass().getName() + "[source=" + eventSrc + ",duration="
		+ duration;
    }
}
