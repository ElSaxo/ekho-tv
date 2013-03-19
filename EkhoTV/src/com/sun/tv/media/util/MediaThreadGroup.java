/*
 * @(#)MediaThreadGroup.java	1.4 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.util;

/**
 * This class is a part of the porting layer implementation for JavaTV. A thread
 * group class where all JMF threads are based on.
 */
public class MediaThreadGroup extends ThreadGroup {

    private int controlPriority;
    private int audioPriority;
    private int videoPriority;
    private int networkPriority;

    MediaThreadGroup() {
	super(MediaThreadGroup.getRootThreadGroup(), "JMF thread group");
	setMaxPriority(Thread.MAX_PRIORITY);

	controlPriority = getMaxPriority() - 1;

	// videoPriority = Thread.MIN_PRIORITY + 1;
	// audioPriority = videoPriority + 1;
	// networkPriority = audioPriority + 1;

	audioPriority = Thread.MAX_PRIORITY - 5;
	videoPriority = Thread.NORM_PRIORITY - 2; /*
						   * To be less than the
						   * Appletpriority
						   */
	networkPriority = audioPriority + 1;

    }

    /**
     * Recursively traverse up the thread group tree to find the root. This will
     * allow us to set the priority to the max possible.
     */
    static private ThreadGroup getRootThreadGroup() {
	ThreadGroup g = Thread.currentThread().getThreadGroup();
	for (; g.getParent() != null; g = g.getParent())
	    ;
	return g;
    }

    /**
     * This should be used for Manager, events threads etc. -- the mechanism to
     * maintain the players.
     */
    public int getControlPriority() {
	return controlPriority;
    }

    /**
     * This should be used for threads handling the audio medium.
     */
    public int getAudioPriority() {
	return audioPriority;
    }

    /**
     * This should be used for threads handling the video medium.
     */
    public int getVideoPriority() {
	return videoPriority;
    }

    /**
     * This should be used for threads handling network packets. e.g. RTP
     */
    public int getNetworkPriority() {
	return networkPriority;
    }

}
