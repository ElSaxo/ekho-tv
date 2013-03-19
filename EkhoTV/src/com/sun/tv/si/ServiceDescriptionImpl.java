/*
 * @(#)ServiceDescriptionImpl.java	1.8 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.si;

import java.util.Date;

import javax.tv.locator.Locator;
import javax.tv.locator.LocatorFactory;
import javax.tv.service.SIElement;
import javax.tv.service.Service;
import javax.tv.service.ServiceInformationType;
import javax.tv.service.navigation.ServiceDescription;

import com.sun.tv.LocatorImpl;

/**
 * This <code>SIElement</code> provides a textual description of a
 * <code>Service</code>.
 * <p>
 * 
 * (In ATSC PSIP, this information is obtained from the ETT associated with this
 * service.)
 */
public class ServiceDescriptionImpl implements ServiceDescription, SIElement {

    private String serviceName;
    private String description;
    private Date updatedTime;
    private ServiceImpl service = null;
    private Locator locator = null;

    public ServiceDescriptionImpl(Service serviceImpl, String serviceName,
	    String description, Date updatedTime) {

	this.service = service;
	this.serviceName = serviceName;
	this.description = description;
	this.updatedTime = updatedTime;
    }

    /**
     * Provides a textual description of the <code>Service</code>.
     * 
     * @return A textual description of the <code>Service</code>.
     */
    public String getServiceDescription() {
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
	if (this.locator == null) {
	    try {
		this.locator = LocatorFactory.getInstance().createLocator(
			LocatorImpl.ServiceDescriptionProtocol + serviceName);
	    } catch (Exception e) {
		;
	    }
	}
	return this.locator;
    }

    /**
     * Reports the service information format of this object.
     * 
     * @return The service information format.
     */
    public ServiceInformationType getServiceInformationType() {
	return service.getServiceInformationType();
    }

}
