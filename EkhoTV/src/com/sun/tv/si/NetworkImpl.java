/*
 * @(#)NetworkImpl.java	1.9 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.si;

import java.util.Date;

import javax.tv.locator.InvalidLocatorException;
import javax.tv.locator.Locator;
import javax.tv.locator.LocatorFactory;
import javax.tv.service.SIRequest;
import javax.tv.service.SIRequestor;
import javax.tv.service.ServiceInformationType;
import javax.tv.service.transport.Network;
import javax.tv.service.transport.TransportStream;

import com.sun.tv.LocatorImpl;
import com.sun.tv.receiver.Settings;

/**
 * This interface provides descriptive information about a network of transport
 * streams.
 */
public class NetworkImpl implements Network {

    private String name = null;
    private int networkID = -1;
    private Date updatedTime = null;
    private Locator locator = null;
    private ServiceInformationType siType;

    public NetworkImpl(String name, int networkID,
	    ServiceInformationType siType, Date updatedTime) {

	this.name = name;
	this.networkID = networkID;
	this.siType = siType;
	this.updatedTime = updatedTime;
    }

    /**
     * This method returns the ID of this Network
     * 
     * @return A number identifying this network
     */
    public int getNetworkID() {
	return this.networkID;
    }

    /**
     * This method returns the name of this network.
     * 
     * @return A string representing the name of this network.
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
     */
    public Locator getLocator() {
	if (this.locator == null) {
	    try {
		this.locator = LocatorFactory.getInstance().createLocator(
			LocatorImpl.NetworkProtocol + name);
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

    /**
     * Retrieves an array of <code>TransportStream</code> objects representing
     * the transport streams carried in this <code>Network</code>. Only
     * <code>TransportStream</code> instances for which the caller has
     * <code>javax.tv.service.ReadPermission</code> on the underlying locator
     * will be present in the array. If this <code>Network</code> does not
     * aggregate transport streams, the result is a zero-length array.
     * <p>
     * 
     * This method delivers its results asynchronously.
     * 
     * @param locator
     *            A locator referencing a <code>Network</code> from which to
     *            retrieve transport stream information.
     * 
     * @param requestor
     *            The <code>SIRequestor</code> to be notified when this
     *            retrieval operation completes.
     * 
     * @return An <code>SIRequest</code> object identifying this asynchronous
     *         retrieval request.
     * 
     * @throws InvalidLocatorException
     *             If <code>locator</code> does not reference a valid
     *             <code>Network</code> on the <code>Transport</code>
     *             implementing this interface.
     * 
     * @throws SecurityException
     *             if the caller does not have
     *             <code>javax.tv.service.ReadPermission(locator)</code>.
     * 
     * @see TransportStream
     * @see javax.tv.service.ReadPermission
     */
    public SIRequest retrieveTransportStreams(SIRequestor requestor) {
	if (requestor == null) {
	    throw new NullPointerException("SIRequestor null");
	}

	// TBD check on the array of transport streams.
	Locator streamsLocator = LocatorImpl
		.transformToTransportStream(getLocator());

	int reqKind = Settings.REQ_TRANSPORT_STREAM;
	return new SIRequestImpl(requestor, streamsLocator, reqKind, this);
    }
}
