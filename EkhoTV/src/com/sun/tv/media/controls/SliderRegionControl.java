/*
 * @(#)SliderRegionControl.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.controls;


/**
 * A SliderRegionControl can be used to highlight a section of the slider.
 */
public interface SliderRegionControl extends AtomicControl {

    /**
     * Sets the long value for this control. Returns the actual long that was
     * set.
     */
    long setMaxValue(long value);

    /**
     * Returns the long value for this control.
     */
    long getMaxValue();

    /**
     * Sets the long value for this control. Returns the actual long that was
     * set.
     */
    long setMinValue(long value);

    /**
     * Returns the long value for this control.
     */
    long getMinValue();

    boolean isEnable();

    void setEnable(boolean f);

}
