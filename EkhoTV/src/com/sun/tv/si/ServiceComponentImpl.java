/*
 * @(#)ServiceComponentImpl.java	1.13 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.si;

import java.util.Date;

import javax.tv.locator.Locator;
import javax.tv.locator.LocatorFactory;
import javax.tv.service.Service;
import javax.tv.service.ServiceInformationType;
import javax.tv.service.guide.ProgramEvent;
import javax.tv.service.navigation.ServiceComponent;
import javax.tv.service.navigation.StreamType;

import com.sun.tv.LocatorImpl;

/**
 * This interface represents a abstraction of an Elementary Stream. It provides
 * information about individual components of the Service. It may be used to
 * select the appropriate components of the Service.
 */
public class ServiceComponentImpl implements ServiceComponent {

    private String name = null;
    private StreamType streamType = null;
    private String language = null;
    private Service service = null;
    private Date updatedTime;
    private Locator locator = null;
    private ServiceInformationType siType;
    private ProgramEvent program = null;
    private boolean autorun = true;

    private int selectionFailedReason = 0;

    public ServiceComponentImpl(String name, String language,
	    StreamType streamType, Service service, ProgramEvent program,
	    ServiceInformationType siType, boolean autorun, Date updatedTime) {

	this.name = name;
	this.streamType = streamType;
	this.service = service;
	this.program = program;
	this.siType = siType;
	this.autorun = autorun;
	this.updatedTime = updatedTime;

	if (language != null && language.length() > 3) {
	    this.language = new String(language.getBytes(), 0, 3);
	} else {
	    this.language = language;
	}
    }

    /**
     * Returns a name associated with this component. The Component Descriptor
     * (DVB) or Component Name Descriptor (ATSC) may be used if present. A
     * generic name (e.g. video, first audio, etc.) may be used otherwise.
     * 
     * @return A string representing the component name. The string is empty if
     *         no name can be associated with this component.
     */
    public String getName() {
	return this.name;
    }

    /**
     * Called to identified the language used for the elementary stream.
     * 
     * @return A string representing a language code defining the language
     *         associated with this component. It contains a three-character
     *         code as specified by ISO 639.2/B. Empty string is returned when
     *         there is no language associated with this component.
     */
    public String getAssociatedLanguage() {
	return (this.language == null) ? "" : this.language;
    }

    /**
     * Provides the stream type of this component. (E.g. "video", "audio", etc.)
     * 
     * @return Stream type of this component.
     */
    public StreamType getStreamType() {
	return this.streamType;
    }

    /**
     * Provides the Service to which this ServiceComponentImpl belongs. The
     * result may be null if the Service cannot be determined.
     * 
     * @return The Service to which this ServiceComponentImpl belongs.
     */
    public Service getService() {
	return this.service;
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
	if (this.locator != null)
	    return this.locator;

	try {
	    String locatorStr = LocatorImpl.ServiceComponentProtocol + name
		    + LocatorImpl.ServiceProtocol + service.getName();
	    this.locator = LocatorFactory.getInstance().createLocator(
		    locatorStr);
	} catch (Exception e) {
	    ;
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
     * Provides the ProgramEvent to which this ServiceComponentImpl belongs. The
     * result may be null if the ProgramEvent cannot be determined.
     * 
     * @return The ProgramEvent to which this ServiceComponentImpl belongs or
     *         null if this ServiceComponent does not belong to a ProgramEvent.
     */
    public ProgramEvent getProgramEvent() {
	return this.program;
    }

    public boolean isAutoRun() {
	return this.autorun;
    }

    public void setSelectionFailedReason(int reason) {
	this.selectionFailedReason = reason;
    }

    public int getSelectionFailedReason() {
	return this.selectionFailedReason;
    }
}
