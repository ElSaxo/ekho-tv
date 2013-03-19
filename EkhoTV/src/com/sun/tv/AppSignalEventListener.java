/*
 * @(#)AppSignalEventListener.java	1.7 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv;

import java.util.EventListener;

import javax.tv.xlet.Xlet;

/**
 * This interface is used by subsystems that are interested in receiving events
 * from the service selection module. These will be used to signal when new
 * applications have been added to the stream.
 */

public interface AppSignalEventListener extends EventListener {

    /**
     * Called when a new application signal event is created.
     */
    public abstract Xlet signalReceived(AppSignalEvent evt);

}
