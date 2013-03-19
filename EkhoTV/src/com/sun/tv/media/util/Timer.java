/*
 * @(#)Timer.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.util;

import javax.media.Clock;
import javax.media.TimeBase;

import com.sun.tv.media.SystemTimeBase;

/**
 * Timer A timer thread that will wake up every specified interval and do some
 * processing. To use it, you will: - subclass from it; - set a wakeup interval;
 * - overwrite the processTimer() callback; - invoke the start() method. It can
 * also be slaved to a given time base by using the setTimeBase() method.
 * 
 * @version 1.13, 98/03/28
 */
public abstract class Timer extends LoopThread {

    private long interval; // in millisecond.
    private TimeBase timeBase;
    private Clock clock = null;

    public Timer(long t) {
	timeBase = new SystemTimeBase();
	setNextInterval(t);
	setName("Timer thread");
    }

    /**
     * Construct the Timer based on the given clock. All reference time will be
     * computed from the given clock. setTimeBase() is a no op.
     */
    public Timer(Clock c, long t) {
	this(t);
	clock = c;
	timeBase = null;
    }

    /**
     * Resume the timerLoop at the beginning for the timerLoop where
     * waitHereIfPaused() is called.
     */
    public synchronized void restart() {
	paused = false;
	restarted = true;
	notifyAll();
    }

    /**
     * Set the next wake-up interval in millisecond.
     */
    public void setNextInterval(long t) {
	interval = (t < 0 ? 0 : t);
    }

    /**
     * Slave the Timer to the given time base.
     */
    public void setTimeBase(TimeBase tb) {
	if (clock == null)
	    timeBase = tb;
    }

    /**
     * Return the TimeBase that the timer is slaved to.
     */
    public TimeBase getTimeBase() {
	return (clock == null ? timeBase : clock.getTimeBase());
    }

    /**
     * Return the current time base time in millisec.
     */
    protected long getTime() {
	return (clock == null ? timeBase.getNanoseconds() / 1000000 : clock
		.getTimeBase().getNanoseconds() / 1000000);
    }

    /**
     * Implement the abstract method from the LoopThread superclass.
     */
    public boolean process() {
	return true;
    }

    /**
     * process callback function.
     */
    protected abstract void processTimer();

    /**
     * Wake up every "interval" to do some processing. The processTimer()
     * callback will be invoked.
     */
    public void run() {
	long last, now, next, elapsed;

	next = interval;

	for (;;) {

	    // Wait here if pause() was invoked. Until restart() is called,
	    // the thread will wait here indefinitely.
	    waitHereIfPaused();

	    // Check the time before and after the sleep() and processTimer()
	    // to compute the elapsed time during the wait operation together
	    // with the time required for the processing.
	    last = getTime();

	    // Sleep for the given interval.
	    if (next > 0) {
		try {
		    sleep(next);
		} catch (InterruptedException e) {
		    System.err.println("Timer: " + e);
		}
	    }

	    // Check to see if time has elapsed. If time has not
	    // elapsed, we won't need to process the timer.
	    if (next == 0 || ((now = getTime()) > last)) {
		processTimer();
		now = getTime();
	    }

	    if (restarted) {
		// If the thread has been restarted, we'll reset the
		// elapsed time to be the same as the interval.
		elapsed = next;
		restarted = false;
	    } else {
		elapsed = now - last;
	    }

	    if (elapsed == 0 && next > 0) {
		// Time has not elapsed, keep waiting.
		; // next = next;
	    } else if (elapsed > next) {
		// The elapsed time is longer than the given time.
		// We'll need to shorten the next interval by that
		// much.
		if (elapsed - next > interval) {
		    // If the delay is already longer than the next
		    // call time. There's not much we can do. Just
		    // set the next time to be the smallest.
		    next = 0;
		} else {
		    next = interval - (elapsed - next);
		}
	    } else {
		// The elapsed time is shorter than the given time.
		// We'll need to add that amount to the next interval.
		next = interval + next - elapsed;
	    }

	    if (paused)
		doPause();

	    if (killed)
		doKilled();
	}

    }

}
