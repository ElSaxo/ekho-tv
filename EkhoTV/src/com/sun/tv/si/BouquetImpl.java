/*
 * @(#)BouquetImpl.java	1.9 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

/**
 * TBD
 * @note, bascially done, check if we want to implement the 
 * CAIdentification interface here.
 */

package com.sun.tv.si;

import java.util.Date;

import javax.tv.locator.Locator;
import javax.tv.locator.LocatorFactory;
import javax.tv.service.ServiceInformationType;
import javax.tv.service.transport.Bouquet;

import com.sun.tv.LocatorImpl;

/**
 * This class implements the Bouquet interface which represents information
 * about a bouquet (a collection of services which can span transport stream and
 * network boundaries).
 * <p>
 * 
 * A <code>BouquetImpl</code> object may optionally implement the
 * <code>CAIdentification</code> interface. Note that bouquets are not supported
 * in ATSC.
 * 
 * @see javax.tv.service.navigation.CAIdentification
 */
public class BouquetImpl implements Bouquet {

    private String name = null;
    private int bouquetID = -1;
    private Date updatedTime = null;
    private Locator locator = null;
    private ServiceInformationType siType;

    /**
     * This constructor creates the Bouquet object, with all the class
     * variables, passed into the constructor.
     * 
     * @see com.sun.tv.si.SIManagerImpl
     */
    public BouquetImpl(String name, int bouquetID,
	    ServiceInformationType siType, Date updatedTime) {

	this.name = name;
	this.bouquetID = bouquetID;
	this.updatedTime = updatedTime;
	this.siType = siType;
    }

    /**
     * This method returns the ID of this bouquet definition.
     * 
     * @return A number identifying this bouquet
     */
    public int getBouquetID() {
	return bouquetID;
    }

    /**
     * This method returns the name of this bouquet.
     * 
     * @return A string representing the name of this bouquet
     */
    public String getName() {
	return this.name;
    }

    /**
     * Gets the complete Locator of this SI Element. Each SI Element (such as
     * BroadcastService, ProgramEvent, etc.) in the MPEG-2 domain is identified
     * by a Locator. This identification is encapsulated by the Locator object
     * which may use a URL format, specific MPEG numbers, such as network ID,
     * etc., or other mechanisms.
     * 
     * @return Locator representing this SI Element
     * 
     * @see LocatorImpl
     */
    public Locator getLocator() {
	if (this.locator == null) {
	    try {
		this.locator = LocatorFactory.getInstance().createLocator(
			LocatorImpl.BouquetProtocol + name);
	    } catch (Exception e) {
		;
	    }
	}
	return this.locator;
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
     * Reports the service information format of this object.
     * 
     * @return The service information format.
     */
    public ServiceInformationType getServiceInformationType() {
	return this.siType;
    }
}
