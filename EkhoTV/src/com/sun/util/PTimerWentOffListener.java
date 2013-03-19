/*
 * @(#)PTimerWentOffListener.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.util;

/**
 * A listener interested in timer specifications going off.
 * 
 * @author: Alan Bishop
 * @since: PersonalJava1.0
 */
public interface PTimerWentOffListener {

    /**
     * Notifies the listener that a timer specification went off.
     * 
     * @param e
     *            the event specifying which timer and which timer specification
     *            went off.
     */
    void timerWentOff(PTimerWentOffEvent e);
}
