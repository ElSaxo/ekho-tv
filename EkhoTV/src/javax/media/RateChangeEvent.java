/*
 * @(#)RateChangeEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * A <code>RateChangeEvent</code> is a <code>ControllerEvent</code> that is
 * posted when a <code>Controller's</code> rate changes.
 * 
 * @see Controller
 * @see ControllerListener
 * @version 1.13, 98/03/28.
 */
public class RateChangeEvent extends ControllerEvent {

    float rate;

    public RateChangeEvent(Controller from, float newRate) {
	super(from);
	rate = newRate;
    }

    /**
     * Get the new rate of the <code>Controller</code> that generated this
     * event.
     * 
     * @return The <code>Controller's</code> new rate.
     */
    public float getRate() {
	return rate;
    }

    /**
     * Returns the String representation of this event's values.
     */
    public String toString() {
	return getClass().getName() + "[source=" + eventSrc + ",rate=" + rate
		+ "]";
    }
}
