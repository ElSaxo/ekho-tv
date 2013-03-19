/*
 * @(#)PTimerScheduleFailedException.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.util;

/**
 * An exception thrown by the schedule() method of PTimer when a timer
 * specification can not be met.
 */
public class PTimerScheduleFailedException extends java.lang.Exception {

    /**
     * Constructs a PTimerScheduleFailedException with null as its error detail
     * message.
     */
    public PTimerScheduleFailedException() {
	super();
    }

    /**
     * Constructs a PTimerScheduleFailedException with the specified detail
     * message. The error message string s can later be retreived by
     * Trowable.getMessage() method of java.lang.Throwable.
     * 
     * @param s
     *            the detail message.
     */
    public PTimerScheduleFailedException(String s) {
	super(s);
    }
}
