/*
 * @(#)AppSignalFactory.java	1.5 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv;

/**
 * This class is used by applications to add themselves as a listener to
 * application signalling events. It should be implemented such that it
 * communicates with the underlying native code that scans the broadcast stream
 * for application signalling events.
 */
public class AppSignalFactory {

    /**
     * Add an AppSignalEventListener. All <i>listeners</i> who have added
     * themselves as listeners will be notified when new application signalling
     * events are broadcast.
     * 
     * @param listener
     *            An object that implements the AppSignalEventListener
     *            interface.
     */
    public static void addAppSignalEventListener(AppSignalEventListener listener) {

	// create internal structures if not already created
	// set up native code to talk listen for signals

    }

    /**
     * Remove an AppSignalEventListener. This method will remove the object from
     * the list of event listeners.
     */
    public static void removeAppSignalListener(AppSignalEventListener listener) {
    }
}
