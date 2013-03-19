/*
 * @(#)TVTimerWentOffListener.java	1.5 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.util;

/**
 * A listener interested in timer specifications going off.
 * 
 */
public interface TVTimerWentOffListener {

    /**
     * Notifies the listener that a timer specification went off.
     * 
     * @param e
     *            The event specifying which timer and which timer specification
     *            went off.
     */
    void timerWentOff(TVTimerWentOffEvent e);
}
