/*
 * @(#)SystemThread.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.net.util;

/**
 * A SystemThread is allowed non-standard priority levels, has a specifiable
 * stack size, and is not counted as a user thread.
 */
public class SystemThread extends Thread {

    public SystemThread(String name, int priority, int stackSize) {
	super(name);
	setDaemon(true);
	this.systemPriority = priority;
	this.stackSize = stackSize;
    }

    public SystemThread(String name, int priority) {
	this(name, priority, 0);
    }

    public final int getSystemPriority() {
	return systemPriority;
    }

    private int systemPriority;
    private int stackSize;

    public static final int ClockThreadPriority = MAX_PRIORITY + 6;
    public static final int InterruptThreadPriority = MAX_PRIORITY + 5;
    public static final int MouseThreadPriority = MAX_PRIORITY + 4;
    public static final int NetworkThreadPriority = MAX_PRIORITY + 3;
    public static final int NetworkBackgroundThreadPriority = MAX_PRIORITY + 2;
    public static final int TimerThreadPriority = MAX_PRIORITY + 1;
    public static final int KeyboardThreadPriority = MAX_PRIORITY + 1;
    public static final int TTYDebugThreadPriority = MAX_PRIORITY + 0;
    public static final int ApplicationThreadPriority = MAX_PRIORITY - 1;
    public static final int BackgroundApplicationThreadPriority = MAX_PRIORITY - 2;
    public static final int IdleThreadPriority = MIN_PRIORITY - 1;

}
