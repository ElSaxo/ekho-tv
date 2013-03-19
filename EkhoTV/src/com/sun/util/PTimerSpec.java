/*
 * @(#)PTimerSpec.java	1.4 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.util;

import java.util.Enumeration;
import java.util.Vector;

/**
 * <p>
 * A class representing a timer specification. A timer specification declares
 * when a PTimerWentOffEvent should be sent. These events are sent to the
 * listeners registered on the specification.
 * </p>
 * 
 * <p>
 * PTimer specifications may be <b>absolute</b> or <b>delayed</b>. Absolute
 * specifications go off at the specified time. Delayed specifications go off
 * after waiting the specified amount of time.
 * </p>
 * 
 * <p>
 * Delayed specifications may be repeating or non-repeating. Repeating
 * specifications automatically reschedule theselves after going off.
 * </p>
 * 
 * <p>
 * Repeating specifications may be regular or non-regular. Regular
 * specifications attempt to go off at fixed intervals of time, irrespective of
 * system load or how long it takes to notify the listeners. Non-regular
 * specifications wait the specified amount of time after all listeners have
 * been called before firing again.
 * </p>
 * 
 * <p>
 * For example, create a repeating specification going off every 100ms. Imagine
 * that it takes 5ms to notify the listeners. If the specification is regular,
 * the listeners will be notified after 100ms, 200ms, 300ms, and so on. If the
 * specification is non-regular, the listeners will be notified after 100ms,
 * 205ms, 310ms, and so on.
 * </p>
 * 
 * @author: Alan Bishop
 * @since: PersonalJava1.0
 */
public class PTimerSpec {

    private boolean absolute;
    private boolean repeat;
    private boolean regular;
    private long time;

    // If a subclass provides overriding definitions of
    // addPTimerWentOffListener
    // removePTimerWentOffListener
    // notifyListeners
    // then it may use this field to store its implementation.
    //
    // In the default implementation, it either contains a single instance of
    // a listener or it contains an instance of Vector, holding multiple
    // listeners.
    // This representation is subject to change.
    protected Object listeners;

    /**
     * Creates a timer specification. It initially is absolute, non-repeating,
     * regular specification set to go off at time 0.
     */
    public PTimerSpec() {
	setAbsolute(true);
	setRepeat(false);
	setRegular(true);
	setTime(0);
    }

    /**
     * Sets this specification to be absolute or delayed.
     */
    public void setAbsolute(boolean absolute) {
	this.absolute = absolute;
    }

    /**
     * Checks if this specification is absolute.
     */
    public boolean isAbsolute() {
	return absolute;
    }

    /**
     * Sets this specification to be repeating or non-repeating
     */
    public void setRepeat(boolean repeat) {
	this.repeat = repeat;
    }

    /**
     * Checks if this specification is repeating
     */
    public boolean isRepeat() {
	return repeat;
    }

    /**
     * Sets this specification to be regular or non-regular
     */
    public void setRegular(boolean regular) {
	this.regular = regular;
    }

    /**
     * Checks if this specification is regular
     */
    public boolean isRegular() {
	return regular;
    }

    /**
     * Sets when this specification should go off. For absolute specifications,
     * this is a time in milliseconds since midnight, January 1, 1970 UTC. For
     * delayed specifications, this is a delay time in milliseconds.
     */
    public void setTime(long time) {
	if (time < 0) {
	    throw new IllegalArgumentException("time value cannot be negative");
	}
	this.time = time;
    }

    /**
     * Returns the absolute or delay time when this specification will go off.
     */
    public long getTime() {
	return time;
    }

    // listeners

    /**
     * Adds a listener to this timer specification.
     * 
     * @param l
     *            the listener to add
     */
    public void addPTimerWentOffListener(PTimerWentOffListener l) {
	if (l == null) {
	    throw new NullPointerException();
	}
	synchronized (this) {
	    if (listeners == null) {
		listeners = l;
	    } else {
		Vector v;
		if (listeners instanceof Vector) {
		    v = (Vector) listeners;
		} else {
		    v = new Vector(2);
		    v.addElement(listeners);
		}
		v.addElement(l);
		listeners = v;
	    }
	}
    }

    /**
     * Removes a listener to this timer specification. Silently does nothing if
     * the listener was not listening on this specification.
     * 
     * @param l
     *            the listener to remove
     */
    public void removePTimerWentOffListener(PTimerWentOffListener l) {
	if (l == null) {
	    throw new NullPointerException();
	}
	synchronized (this) {
	    if (listeners == null) {
		return;
	    }

	    if (listeners instanceof Vector) {
		Vector v = (Vector) listeners;
		v.removeElement(l);
		if (v.size() == 1) {
		    listeners = v.firstElement();
		}
	    } else if (listeners == l) {
		listeners = null;
	    }
	}
    }

    // convenience functions

    /**
     * Sets this specification to go off at the given absolute time. This is a
     * convenience function equivalent to setAbsolute(true), setTime(when),
     * setRepeat(false).
     * 
     * @param when
     *            the absolute time for the specification to go off
     */
    public void setAbsoluteTime(long when) {
	setAbsolute(true);
	setTime(when);
	setRepeat(false);
    }

    /**
     * Sets this specification to go off after the given delay time. This is a
     * convenience function equivalent to setAbsolute(false), setTime(delay),
     * setRepeat(false).
     * 
     * @param delay
     *            the relative time for the specification to go off
     */
    public void setDelayTime(long delay) {
	setAbsolute(false);
	setTime(delay);
	setRepeat(false);
    }

    // for the benefit of timer implementations

    /**
     * Calls all listeners registered on this timer specification. This function
     * is primarily for the benefit of those writing implementations of PTimers.
     * 
     * @param source
     *            the PTimer that decided that this specification should go off
     */
    public void notifyListeners(PTimer source) {
	Vector v = null;
	PTimerWentOffListener singleton = null;

	synchronized (this) {
	    if (listeners instanceof Vector) {
		v = (Vector) ((Vector) listeners).clone();
	    } else {
		singleton = (PTimerWentOffListener) listeners;
	    }
	}
	if (v != null) {
	    Enumeration e = v.elements();
	    while (e.hasMoreElements()) {
		PTimerWentOffListener l = (PTimerWentOffListener) e
			.nextElement();
		l.timerWentOff(new PTimerWentOffEvent(source, this));
	    }
	} else if (singleton != null) {
	    singleton.timerWentOff(new PTimerWentOffEvent(source, this));
	}
    }
}
