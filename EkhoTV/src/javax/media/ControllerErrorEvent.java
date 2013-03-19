/*
 * @(#)ControllerErrorEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * A <CODE>ControllerErrorEvent</CODE> describes an event that is generated when
 * an error condition occurs that will cause a <code>Controller</code> to cease
 * functioning. Events should only subclass from
 * <code>ControllerErrorEvent</code> if the error being reported will result in
 * catastrophic failure if action is I not taken, or if the
 * <code>Controller</code> has already failed.
 * 
 * A <CODE>ControllerErrorEvent</CODE> indicates that the
 * <code>Controller</code> is closed.
 * 
 * @see Controller
 * @see ControllerListener
 * @version 1.18, 98/03/28
 */
public class ControllerErrorEvent extends ControllerClosedEvent {

    public ControllerErrorEvent(Controller from) {
	super(from);
    }

    public ControllerErrorEvent(Controller from, String why) {
	super(from, why);
    }

    /**
     * Returns the String representation of this event's values.
     */
    public String toString() {
	return getClass().getName() + "[source=" + eventSrc + ",message="
		+ message + "]";
    }
}
