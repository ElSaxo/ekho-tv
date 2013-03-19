/*
 * @(#)SelectionFailedCausedEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.service.selection;

import javax.tv.locator.Locator;
import javax.tv.service.selection.SelectionFailedEvent;
import javax.tv.service.selection.ServiceContext;

public class SelectionFailedCausedEvent extends SelectionFailedEvent implements
	CausedLocatorsInterface {
    Locator[] causedLocators;

    public SelectionFailedCausedEvent(ServiceContext source, int reason,
	    Locator[] causedLocators) {
	super(source, reason);
	this.causedLocators = causedLocators;
    }

    public Locator[] getCausedLocators() {
	return causedLocators;
    }
}
