/*
 * @(#)BooleanControl.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.controls;


/**
 * This control represents a toggle state. The state at any time can either be
 * true or false.
 */
public interface BooleanControl extends AtomicControl {

    /**
     * Returns the current state of the control.
     */
    public boolean getValue();

    /**
     * Sets the state of the control.
     */
    public boolean setValue(boolean value);
}
