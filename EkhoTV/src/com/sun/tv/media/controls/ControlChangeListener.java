/*
 * @(#)ControlChangeListener.java	1.4 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.controls;


/**
 * This interface is a part of the porting layer implementation for JavaTV.
 * Listener for changes in the state of a Control.
 */
public interface ControlChangeListener {

    /**
     * Gets called whenever the state of a Control changes.
     */
    public void controlChanged(ControlChangeEvent e);

}
