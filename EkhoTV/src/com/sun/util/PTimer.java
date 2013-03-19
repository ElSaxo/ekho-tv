/*
 * @(#)PTimer.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.util;

/**
 * A class representing a timer.
 * 
 * A timer is responsible for managing a set of timer events specified by timer
 * specifications. When the timer event should be sent, the timer calls the
 * timer specification's notifyListeners method.
 * 
 * @author: Alan Bishop
 * @since: PersonalJava1.0
 */
public abstract class PTimer {

    private static PTimer defaultTimer;

    /**
     * Returns the default timer for the system. There may be one PTimer
     * instance per virtual machine, one per applet, one per call to getTimer,
     * or some other platform dependent implementation.
     * 
     * @return A non-null PTimer object.
     */
    public static PTimer getTimer() {
	if (defaultTimer != null) {
	    return defaultTimer;
	}
	synchronized (PTimer.class) {
	    if (defaultTimer == null) {
		defaultTimer = new DefaultPTimer();
	    }
	    return defaultTimer;
	}
    }

    /**
     * @deprecated since Personal Java Spec 1.2, replaced by the new
     *             {@link #scheduleTimerSpec} method.
     * 
     */
    public abstract void schedule(PTimerSpec t);

    /**
     * Begins monitoring a PTimerSpec.
     * <p>
     * When the timer specification should go off, the timer will call
     * PTimerSpec.notifyListeners.
     * </p>
     * 
     * <p>
     * Returns the real PTimerSpec that got scheduled. If you schedule a spec
     * that implies a smaller granularity than this timer can provide, or a
     * repeat timer spec that has a smaller repeating interval than this timer
     * can provide, the timer should round to the closest value and return that
     * value as a {@link PTimerSpec} object. An interested application can use
     * accessor methods {@link #getMinRepeatInterval} and
     * {@link #getGranularity} to obtain the Timer's best knowledge of the
     * Timer's limitation on granularity and repeat interval. If you schedule an
     * absolute specification that should have gone off already, it will go off
     * immediately. If the scheduled specification can not be met at all, the
     * exception {@link PTimerScheduleFailedException} should be thrown.
     * </p>
     * 
     * <p>
     * You may schedule a timer specification with multiple timers. You may
     * schedule a timer specification with the same timer multiple times (in
     * which case it will go off multiple times). If you modify a timer
     * specification after it has been scheduled with any timer, the results are
     * unspecified.
     * </p>
     * 
     * @param t
     *            The timer specification to begin monitoring.
     * @return The real PTimerSpec that was scheduled.
     * @exception PTimerScheduleFailedException
     *                is thrown when the scheduled spec can not be met at all.
     */
    public abstract PTimerSpec scheduleTimerSpec(PTimerSpec t)
	    throws PTimerScheduleFailedException;

    /**
     * Removes a timer specification from the set of monitored specifications.
     * The descheduling happens as soon as practical, but may not happen
     * immediately. If the timer specification has been scheduled multiple times
     * with this timer, all the schedulings are cancelled. @ param t The timer
     * specification to end monitoring.
     */
    public abstract void deschedule(PTimerSpec t);

    /**
     * Report the minimum interval that this timer can repeat tasks. For
     * example, it's perfectly reasonable for a Timer to specify that the
     * minimum interval for a repeatedly performed task is 1000 msec in between
     * every run. This is to avoid possible system overloading.
     * 
     * @return The timer's best knowledge of minimum repeat interval in
     *         mili-seconds. Return -1 if this timer doesn't know its repeating
     *         interval limitation.
     */
    public abstract long getMinRepeatInterval();

    /**
     * Report the granularity of this timer, i.e. the length of time between
     * "tick"s of this timer.
     * 
     * @return The timer's best knowledge of the granularity in mili-seconds.
     *         Return -1 if this timer doesn't know its granularity.
     * 
     */
    public abstract long getGranularity();

}
