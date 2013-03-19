/*
 * @(#)ProgramEventDescriptionImpl.java	1.11 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.si;

import java.util.Date;

import javax.tv.locator.Locator;
import javax.tv.service.SIElement;
import javax.tv.service.ServiceInformationType;
import javax.tv.service.guide.ProgramEvent;
import javax.tv.service.guide.ProgramEventDescription;

import com.sun.tv.LocatorImpl;

/**
 * 
 * This <code>SIElement</code> provides a textual description of a
 * <code>ProgramEvent</code>.
 * <p>
 * 
 * (In ATSC PSIP, this information is obtained from the Extended Text Table; in
 * DVB SI, from the Short Event Descriptor.)
 * 
 */
public class ProgramEventDescriptionImpl implements ProgramEventDescription,
	SIElement {

    private Date updatedTime;
    private String description;
    private ProgramEvent program = null;

    public ProgramEventDescriptionImpl(ProgramEvent program, String name,
	    String description, Date updatedTime) {

	this.description = description;
	this.updatedTime = updatedTime;
	this.program = program;
    }

    /**
     * Provides a textual description of the <code>ProgramEvent</code>.
     * 
     * @return A textual description of the <code>ProgramEvent</code>.
     */
    public String getProgramEventDescription() {
	return this.description;
    }

    /**
     * Returns the time when this object was last updated from data in the
     * broadcast.
     * 
     * @return The date of the last update in UTC format, or <code>null</code>
     *         if unknown.
     */
    public Date getUpdateTime() {
	return this.updatedTime;
    }

    /**
     * Gets the complete Locator of this SI Element. Each SI Element (such as
     * BroadcastService, ProgramEvent, etc.) in the MPEG-2 domain is identified
     * by a Locator. This identification is encapsulated by the Locator object
     * which may use a URL format, specific MPEG numbers, such as network ID,
     * etc., or other mechanisms.
     * 
     * @return Locator representing this SI Element
     */
    public Locator getLocator() {
	Locator locator = (Locator) program.getLocator();
	return LocatorImpl.transformToProgramEventDescription(locator);
    }

    /**
     * Reports the service information format of this object.
     * 
     * @return The service information format.
     */
    public ServiceInformationType getServiceInformationType() {
	ServiceImpl service = (ServiceImpl) program.getService();
	return service.getServiceInformationType();
    }
}
