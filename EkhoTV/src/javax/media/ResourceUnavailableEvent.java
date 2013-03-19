/*
 * @(#)ResourceUnavailableEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * A <code>ResourceUnavailableEvent</code> indicates that a
 * <code>Controller</code> was unable to allocate a resource that it requires
 * for operation.
 * 
 * @see Controller
 * @see ControllerListener
 * @version 1.23, 98/03/28
 */
public class ResourceUnavailableEvent extends ControllerErrorEvent {

    public ResourceUnavailableEvent(Controller from) {
	super(from);
    }

    public ResourceUnavailableEvent(Controller from, String message) {
	super(from, message);
    }

}
