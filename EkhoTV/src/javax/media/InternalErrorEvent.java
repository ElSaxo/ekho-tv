/*
 * @(#)InternalErrorEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * An <code>InternalErrorEvent</code> indicates that a <code>Controller</code>
 * failed for implementation-specific reasons. This event indicates that there
 * are problems with the implementation of the <code>Controller</code>.
 * 
 * @see Controller
 * @see ControllerListener
 * @version 1.9, 98/03/28
 */
public class InternalErrorEvent extends ControllerErrorEvent {

    public InternalErrorEvent(Controller from) {
	super(from);
    }

    public InternalErrorEvent(Controller from, String message) {
	super(from, message);
    }

}
