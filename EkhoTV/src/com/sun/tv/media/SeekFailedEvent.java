/*
 * @(#)SeekFailedEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import javax.media.Controller;
import javax.media.StopEvent;
import javax.media.Time;

/**
 * A <code>SeekFailedEvent</code> indicates that the <code>Controller</code>
 * could not start at the current media time (set using setMediaTime).
 */

public class SeekFailedEvent extends StopEvent {

    public SeekFailedEvent(Controller from, int previous, int current,
	    int target, Time mediaTime) {
	super(from, previous, current, target, mediaTime);
    }
}
