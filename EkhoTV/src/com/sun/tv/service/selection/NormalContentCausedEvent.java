/*
 * @(#)NormalContentCausedEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.service.selection;

import javax.tv.locator.Locator;
import javax.tv.service.selection.NormalContentEvent;
import javax.tv.service.selection.ServiceContext;

public class NormalContentCausedEvent extends NormalContentEvent implements
	CausedLocatorsInterface {
    Locator[] causedLocators;

    public NormalContentCausedEvent(ServiceContext source,
	    Locator[] causedLocators) {
	super(source);
	this.causedLocators = causedLocators;
    }

    public Locator[] getCausedLocators() {
	return causedLocators;
    }
}
