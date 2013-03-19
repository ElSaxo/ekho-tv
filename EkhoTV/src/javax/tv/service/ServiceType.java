/*
 * @(#)ServiceType.java	1.4 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service;

/**
 * This class represents service type values such as "digital television",
 * "digital radio", "NVOD reference service", "NVOD time-shifted service",
 * "analog television", "analog radio", "data broadcast" and "application".
 * These basic service types can be extended by subclassing.
 * <p>
 * 
 * (These values are mappable to the ATSC service type in the VCT table and the
 * DVB service type in the Service Descriptor.)
 */
public class ServiceType {

    private String name = null;

    /**
     * Creates a service type object.
     * 
     * @param name
     *            The string name of this type (e.g., "DIGITAL_TV").
     */
    protected ServiceType(String name) {
	this.name = name;
	if (name == null) {
	    throw new NullPointerException("Name is null");
	}
    }

    /**
     * Provides the string name of the type. For the type objects defined in
     * this class, the string name will be identical to the class variable name.
     */
    public String toString() {
	return name;
    }

    /**
     * Digital TV service type.
     */
    public static final ServiceType DIGITAL_TV;

    /**
     * Digital radio service type.
     */
    public static final ServiceType DIGITAL_RADIO;

    /**
     * NVOD reference service type.
     */
    public static final ServiceType NVOD_REFERENCE;

    /**
     * NVOD time-shifted service type.
     */
    public static final ServiceType NVOD_TIME_SHIFTED;

    /**
     * Analog TV service type.
     */
    public static final ServiceType ANALOG_TV;

    /**
     * Analog radio service type.
     */
    public static final ServiceType ANALOG_RADIO;

    /**
     * Data broadcast service type identifying a data service.
     */
    public static final ServiceType DATA_BROADCAST;

    /**
     * Data application service type identifying an interactive application.
     */
    public static final ServiceType DATA_APPLICATION;

    /**
     * Unknown service type.
     */
    public static final ServiceType UNKNOWN;

    // Needed for compilation
    static {
	DIGITAL_TV = new ServiceType("DIGITAL_TV");
	DIGITAL_RADIO = new ServiceType("DIGITAL_RADIO");
	NVOD_REFERENCE = new ServiceType("NVOD_REFERENCE");
	NVOD_TIME_SHIFTED = new ServiceType("NVOD_TIME_SHIFTED");
	ANALOG_TV = new ServiceType("ANALOG_TV");
	ANALOG_RADIO = new ServiceType("ANALOG_RADIO");
	DATA_BROADCAST = new ServiceType("DATA_BROADCAST");
	DATA_APPLICATION = new ServiceType("DATA_APPLICATION");
	UNKNOWN = new ServiceType("UNKNOWN");
    }
}
