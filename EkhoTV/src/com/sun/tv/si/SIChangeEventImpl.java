/*
 * @(#)SIChangeEventImpl.java	1.7 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.si;

import javax.tv.service.SIChangeEvent;
import javax.tv.service.SIChangeType;
import javax.tv.service.SIElement;

/**
 * 
 * SIChangeEventImpl objects are sent to SIChangeListeners to signal detected
 * changes in the SI Database.
 * <p>
 * 
 * Note that while the SI database may detect changes, notification of which
 * specific <code>SIElement</code> has changed is not guaranteed.
 */
public class SIChangeEventImpl extends SIChangeEvent {

    /**
     * Constructs an SIChangeEventImpl object.
     * 
     * @param source
     *            The SI entity in which the change occurred.
     * 
     * @param type
     *            The type of change that occurred.
     * 
     * @param e
     *            The SIElement that changed, or <code>null</code> if this is
     *            unknown.
     */
    public SIChangeEventImpl(Object source, SIChangeType type, SIElement e) {
	super(source, type, e);
    }

}
