/*
 * @(#)MediaThread.java	1.5 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.util;

//A.D.import com.sun.tv.media.BuildInfo;

/**
 * This class is a part of the porting layer implementation for JavaTV. A thread
 * class where all JMF created threads should based on.
 */
public class MediaThread extends Thread {

    static protected MediaThreadGroup mediaThreadGroup;
    protected volatile boolean aborted = false;

    static {
	// try {
	// JMFSecurity.enablePrivilege.invoke(JMFSecurity.privilegeManager,
	// JMFSecurity.threadArgs);
	// JMFSecurity.enablePrivilege.invoke(JMFSecurity.privilegeManager,
	// JMFSecurity.threadGroupArgs);
	// } catch (Exception e) {
	// }

	/*
	 * A.D. if (BuildInfo.usePureJava()) mediaThreadGroup = null; else A.D.
	 */
	mediaThreadGroup = new MediaThreadGroup();
    }

    public MediaThread() {
	this("JMF thread");
    }

    public MediaThread(String name) {
	// A.D. super(BuildInfo.usePureJava() ? null : mediaThreadGroup, name);
	super(mediaThreadGroup, name); // A.D.
    }

    public MediaThread(Runnable r) {
	this(r, "JMF thread");
    }

    public MediaThread(Runnable r, String name) {
	// A.D. super(BuildInfo.usePureJava() ? null : mediaThreadGroup, r,
	// name);
	super(mediaThreadGroup, r, name); // A.D.
    }

    static public MediaThreadGroup getMediaThreadGroup() {
	return mediaThreadGroup;
    }

    /**
     * This should be used for Manager, events threads etc. -- the mechanism to
     * maintain the players.
     */
    public void useControlPriority() {
	// A.D. if (!BuildInfo.usePureJava())
	setPriority(mediaThreadGroup.getControlPriority());
	// System.err.println("set thread priority: " + getName() + " : " +
	// getPriority());
    }

    /**
     * This should be used for threads handling the audio medium.
     */
    public void useAudioPriority() {
	// A.D. if (!BuildInfo.usePureJava())
	setPriority(mediaThreadGroup.getAudioPriority());
	// System.err.println("set thread priority: " + getName() + " : " +
	// getPriority());
    }

    /**
     * This should be used for threads handling the video medium.
     */
    public void useVideoPriority() {
	// A.D. if (!BuildInfo.usePureJava())
	setPriority(mediaThreadGroup.getVideoPriority());
	// System.err.println("set thread priority: " + getName() + " : " +
	// getPriority());
    }

    /**
     * This should be used for threads handling network packets. e.g. RTP
     */
    public void useNetworkPriority() {
	// A.D. if (!BuildInfo.usePureJava())
	setPriority(mediaThreadGroup.getNetworkPriority());
	// System.err.println("set thread priority: " + getName() + " : " +
	// getPriority());
    }

    public synchronized void abortThread() {
	aborted = true;
	notifyAll();
    }
}
