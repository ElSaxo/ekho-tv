/*
 * @(#)MediaTimeSetEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * A <code>MediaTimeSetEvent</code> is posted by a <code>Controller</code> when
 * its media-time has been set with the <code>setMediaTime</code> method.
 * 
 * 
 * @see Controller
 * @see ControllerListener
 * @version 1.15, MediaTimeSetEvent.java.
 */
public class MediaTimeSetEvent extends ControllerEvent {

    Time mediaTime;

    public MediaTimeSetEvent(Controller from, Time newMediaTime) {
	super(from);
	mediaTime = newMediaTime;
    }

    /**
     * Get the new media time of the <code>Controller</code> that generated this
     * event.
     * 
     * @return The <code>Controller's</code> new media time.
     */
    public Time getMediaTime() {
	return mediaTime;
    }

    /**
     * Returns the String representation of this event's values.
     */
    public String toString() {
	return getClass().getName() + "[source=" + eventSrc + ",mediaTime="
		+ mediaTime + "]";
    }
}
