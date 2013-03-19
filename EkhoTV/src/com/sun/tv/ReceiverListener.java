/*
 * @(#)ReceiverListener.java	1.4 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv;

import java.util.EventListener;

import javax.tv.service.SIChangeEvent;

/**
 * This interface is implemented by applications wishing to receive notification
 * of changes to <code>SIElement</code> data.
 */
public interface ReceiverListener extends EventListener {

    public void notifyChange(SIChangeEvent event);

}
