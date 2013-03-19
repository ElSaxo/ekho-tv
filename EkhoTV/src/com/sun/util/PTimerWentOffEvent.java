/*
 * @(#)PTimerWentOffEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.util;

/**
 * An event indicating that a timer specification has gone off.
 * 
 * @author: Alan Bishop
 * @since: PersonalJava1.0
 */
public class PTimerWentOffEvent extends java.util.EventObject {

    private PTimerSpec timerSpec;

    /**
     * Creates a new PTimerWentOffEvent with the specified timer and timer
     * specification.
     * 
     * @param source
     *            the timer that sent this event
     * @param spec
     *            the timer specification that went off
     */
    public PTimerWentOffEvent(PTimer source, PTimerSpec spec) {
	super(source);

	timerSpec = spec;
    }

    /**
     * Returns the timer specification for this event.
     */
    public PTimerSpec getTimerSpec() {
	return timerSpec;
    }
}
