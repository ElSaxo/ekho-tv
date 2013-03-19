/*
 * @(#)XletState.java	1.5 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv;

/**
 * This class is for tracking xlet's current state.
 */
public class XletState {
    public static final int LOADED = 0;
    public static final int PAUSED = 1;
    public static final int ACTIVE = 2;
    public static final int DESTROYED = 3;

    private int state = -1;

    XletState() {
	super();
    }

    /**
     * get current state.
     */
    public int getState() {
	return state;
    }

    /**
     * set new state.
     */
    public void setState(int newState) {
	state = newState;
    }
}
