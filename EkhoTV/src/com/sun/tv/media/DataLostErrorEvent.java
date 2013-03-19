/*
 * @(#)DataLostErrorEvent.java	1.4 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerListener;

/**
 * A <CODE>DataLostErrorEvent</CODE> is posted when a <code>Controller</code>
 * has lost data.
 * 
 * @see Controller
 * @see ControllerListener
 * @version 1.1, 98/04/07
 */
public class DataLostErrorEvent extends ControllerClosedEvent {
    public DataLostErrorEvent(Controller from) {
	super(from);
    }

    public DataLostErrorEvent(Controller from, String why) {
	super(from, why);
    }
}
