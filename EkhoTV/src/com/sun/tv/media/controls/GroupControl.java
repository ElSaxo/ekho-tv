/*
 * @(#)GroupControl.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.controls;

import javax.media.Control;

/**
 * A GroupControl is a parent to a set of smaller controls. This is a base class
 * interface for group controls such as VolumeControl, ColorControl,
 * PlaybackControl, etc.
 */
public interface GroupControl extends AtomicControl {

    /**
     * Returns any controls that might constitute this control.
     */
    public Control[] getControls();
}
