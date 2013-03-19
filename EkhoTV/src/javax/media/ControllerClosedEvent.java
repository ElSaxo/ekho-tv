/*
 * @(#)ControllerClosedEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * A <CODE>ControllerClosedEvent</CODE> describes an event that is generated
 * when an a <code>Controller</code> is closed. This implies that the
 * <code>Controller</code> is no longer operational.
 **/

public class ControllerClosedEvent extends ControllerEvent {

    protected String message;

    /**
     * Construct a <CODE>ControllerClosedEvent</CODE>.
     */
    public ControllerClosedEvent(Controller from) {
	super(from);
	message = new String("");
    }

    public ControllerClosedEvent(Controller from, String why) {
	super(from);
	message = why;
    }

    /**
     * Obtain the message describing why this event occurred.
     * 
     * @return Message describing event cause.
     */
    public String getMessage() {
	return message;
    }

}
