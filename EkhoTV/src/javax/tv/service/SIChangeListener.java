/*
 * @(#)SIChangeListener.java	1.16 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service;

import java.util.EventListener;

/**
 * This is the super-interface of the interfaces by which applications may
 * receive notification of changes to <code>SIElement</code> data. Applications
 * that wish to receive notification of changes to <code>SIElement</code> data
 * must implement the appropriate <code>SIChangeListener</code> sub-interface.
 */
public interface SIChangeListener extends EventListener {
}
