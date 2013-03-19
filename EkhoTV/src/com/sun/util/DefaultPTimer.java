/*
 * @(#)DefaultPTimer.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.util;

class DefaultPTimer extends PTimer implements Runnable {

    // this is a list sorted in order of execution. Note that in order to
    // preserve some fairness in the list, timer items that were scheduled
    // to go off before the current time may occur in non-increasing order.
    // (explanation: a newly scheduled item that should go off immediately
    // actually goes off after all other existing items in the list that should
    // go off immediately).
    private DefaultTimerItem timerList;

    // the currently executing specification (only if it is a non-regular
    // repeating one).
    private PTimerSpec current;

    DefaultPTimer() {
	this(8);
    }

    DefaultPTimer(int maxPriority) {
	Thread t = new Thread(this, "Default Timer");
	ThreadGroup g = Thread.currentThread().getThreadGroup();

	t.setPriority(Math.min(maxPriority, g.getMaxPriority()));
	t.start();
    }

    /**
     * @deprecated since Personal Java Spec 1.2, replaced by the new
     *             {@link #scheduleTimerSpec} method.
     * 
     */
    public void schedule(PTimerSpec t) {
	DefaultTimerItem ti = new DefaultTimerItem();

	// Create the specification. Cache values from the timer specification
	// so that a change in the specification can't corrupt our internal
	// structures.
	ti.specification = t;
	long now = System.currentTimeMillis();
	if (t.isAbsolute()) {
	    ti.when = t.getTime();
	} else {
	    ti.delay = t.getTime();
	    ti.when = ti.delay + now;
	}
	ti.repeat = t.isRepeat() && !t.isAbsolute();
	ti.regular = t.isRegular();

	synchronized (this) {
	    scheduleLocked(ti, now);
	}
    }

    public PTimerSpec scheduleTimerSpec(PTimerSpec t)
	    throws PTimerScheduleFailedException {
	DefaultTimerItem ti = new DefaultTimerItem();

	// Create the specification. Cache values from the timer specification
	// so that a change in the specification can't corrupt our internal
	// structures.
	ti.specification = t;
	long now = System.currentTimeMillis();
	if (t.isAbsolute()) {
	    ti.when = t.getTime();
	} else {
	    ti.delay = t.getTime();
	    ti.when = ti.delay + now;
	}
	ti.repeat = t.isRepeat() && !t.isAbsolute();
	ti.regular = t.isRegular();

	synchronized (this) {
	    scheduleLocked(ti, now);
	}
	return t;
    }

    private void scheduleLocked(DefaultTimerItem ti, long now) {

	DefaultTimerItem cur = timerList;
	DefaultTimerItem prev = null;

	long schedTime = ti.when;
	if (schedTime < now) {
	    schedTime = now;
	}

	// by using "<" rather than "<=", we make sure that timer specifications
	// that repeat too often can't clog the entire queue.
	while (cur != null && cur.when < schedTime) {
	    prev = cur;
	    cur = cur.next;
	}
	ti.next = cur;
	if (prev != null) {
	    prev.next = ti;
	} else {
	    timerList = ti;
	}
	notify();
    }

    // Removes the item from the list. If it was the first one, the timer
    // thread will wake up as scheduled, but then notice that the item is gone.
    // Since we allow duplicates, we need to search for repeats in the list.
    public void deschedule(PTimerSpec t) {
	synchronized (this) {

	    if (current == t) {
		current = null;
		return;
	    }

	    DefaultTimerItem cur = timerList;
	    DefaultTimerItem prev = null;

	    while (cur != null) {
		if (cur.specification == t) {
		    // Matched. Remove it.
		    if (prev == null) {
			timerList = timerList.next;
		    } else {
			prev.next = cur.next;
		    }
		} else {
		    prev = cur;
		}
		cur = cur.next;
	    }
	}
    }

    public long getMinRepeatInterval() {
	return (long) -1;
    }

    public long getGranularity() {
	return (long) -1;
    }

    public void run() {
	while (true) {
	    try {
		DefaultTimerItem ti = null;
		long delta;
		synchronized (this) {
		    long now = System.currentTimeMillis();
		    if (timerList == null) {
			ti = null;
			wait();
		    } else if ((delta = timerList.when - now) > 0L) {
			ti = null;
			wait(delta);
		    } else {
			ti = timerList;
			timerList = timerList.next;

			if (ti.repeat) {
			    if (ti.regular) {
				// go ahead and reschedule while we hold the
				// lock
				// if we're a regular event.
				ti.when += ti.delay;

				scheduleLocked(ti, now);
			    } else {
				current = ti.specification;
			    }
			}
		    }
		}

		if (ti != null) {
		    PTimerSpec spec = ti.specification;

		    spec.notifyListeners(this);
		    if (ti.repeat && !ti.regular) {

			// if current is null, that means someone descheduled
			// the item while we were executing it.
			synchronized (this) {
			    if (current != null) {
				long now = System.currentTimeMillis();
				ti.when = now + ti.delay;

				scheduleLocked(ti, now);
				current = null;
			    }
			}
		    }
		}
	    } catch (Exception ex) {
	    }
	}
    }

    public String toString() {
	String s;

	synchronized (this) {
	    if (timerList == null) {
		s = "no timer specifications";
	    } else {
		s = "next timer event in "
			+ (timerList.when - System.currentTimeMillis()) + "ms";
	    }
	}
	return "[DefaultTimer: " + s + "]";
    }
}

class DefaultTimerItem {
    // The timer specification
    PTimerSpec specification;

    // The absolute time when this item should fire
    long when;

    // The delay time for repeating items
    long delay;

    // The next item in the list
    DefaultTimerItem next;

    // Whether or not this specification is repeatable
    boolean repeat;

    // Whether or not this specification is regular
    boolean regular;
}
