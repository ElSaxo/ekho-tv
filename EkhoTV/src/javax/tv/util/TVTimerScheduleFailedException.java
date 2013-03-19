/*
 * @(#)TVTimerScheduleFailedException.java	1.4 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.util;

/**
 * An exception thrown by the <code>TVTimer.schedule()</code> method when a
 * timer specification cannot be met.
 */
public class TVTimerScheduleFailedException extends java.lang.Exception {

    /**
     * Constructs a TVTimerScheduleFailedException with null as its error detail
     * message.
     */
    public TVTimerScheduleFailedException() {
	super();
    }

    /**
     * Constructs a TVTimerScheduleFailedException with the specified detail
     * message. The error message string <code>s</code> can later be retrieved
     * by <code>java.lang.Throwable.getMessage()</code> method.
     * 
     * @param s
     *            The detail message.
     */
    public TVTimerScheduleFailedException(String s) {
	super(s);
    }
}
