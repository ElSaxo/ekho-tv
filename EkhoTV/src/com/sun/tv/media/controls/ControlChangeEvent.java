/*
 * @(#)ControlChangeEvent.java	1.4 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.controls;

import javax.media.Control;

/**
 * This class is a part of the porting layer implementation for JavaTV. This
 * event contains information about which Control has changed.
 */
public class ControlChangeEvent {

    private Control c;

    /**
     * Creates a ControlChangeEvent with the specified control.
     */
    public ControlChangeEvent(Control c) {
	this.c = c;
    }

    /**
     * Returns the Control that generated this event.
     */
    public Control getControl() {
	return c;
    }
}
