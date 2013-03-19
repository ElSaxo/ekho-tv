/*
 * @(#)TVTimerWentOffEvent.java	1.6 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.util;

/**
 * An event indicating that a timer specification has gone off.
 * 
 */
public class TVTimerWentOffEvent extends java.util.EventObject {
    private TVTimerSpec spec = null;

    /**
     * Creates a new TVTimerWentOffEvent with the specified timer and timer
     * specification.
     * 
     * @param source
     *            the timer that sent this event
     * @param spec
     *            the timer specification that went off
     */
    public TVTimerWentOffEvent(TVTimer source, TVTimerSpec spec) {
	super(source);
	this.spec = spec;
    }

    /**
     * Returns the timer specification for this event.
     */
    public TVTimerSpec getTimerSpec() {
	return spec;
    }
}
