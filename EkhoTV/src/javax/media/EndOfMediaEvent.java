/*
 * @(#)EndOfMediaEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * An <code>EndOfMediaEvent</code> indicates that the <code>Controller</code>
 * has reached the end of its media and is stopping.
 * 
 * @see Controller
 * @see ControllerListener
 * @version 1.23, 98/03/28.
 */

public class EndOfMediaEvent extends StopEvent {

    public EndOfMediaEvent(Controller from, int previous, int current,
	    int target, Time mediaTime) {
	super(from, previous, current, target, mediaTime);
    }
}
