/*
 * @(#)StringControl.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.controls;


/**
 * A StringControl holds a string value and can be used to display status
 * information pertaining to the player. In most cases this will be a read-only
 * control.
 */
public interface StringControl extends AtomicControl {

    /**
     * ??? Sets the string value for this control. Returns the actual string
     * that was set.
     */
    String setValue(String value);

    /**
     * Returns the string value for this control.
     */
    String getValue();

    String setTitle(String title);

    String getTitle();
}
