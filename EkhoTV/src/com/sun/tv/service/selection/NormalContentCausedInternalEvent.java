/*
 * @(#)NormalContentCausedInternalEvent.java	1.2 08/11/18
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.service.selection;

import javax.tv.locator.Locator;
import javax.tv.service.selection.ServiceContext;

public class NormalContentCausedInternalEvent extends NormalContentCausedEvent
	implements InternalEvent {
    public NormalContentCausedInternalEvent(ServiceContext source,
	    Locator[] causedLocators) {
	super(source, causedLocators);
    }
}
