/*
 * @(#)ServiceInformationType.java	1.4 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service;

/**
 * 
 * This class represents values of service information (SI) formats.
 */
public class ServiceInformationType {

    private String name = null;

    /**
     * Creates a service information type object.
     * 
     * @param name
     *            The string name of this type (e.g., "ATSC_PSIP").
     */
    protected ServiceInformationType(String name) {
	this.name = name;
	if (name == null) {
	    throw new NullPointerException("Name is null");
	}
    }

    /**
     * Provides the string name of the SI type. For the type objects defined in
     * this class, the string name will be identical to the class variable name.
     */
    public String toString() {
	return name;
    }

    /**
     * ATSC PSIP format.
     */
    public static final ServiceInformationType ATSC_PSIP;

    /**
     * DVB SI format.
     */
    public static final ServiceInformationType DVB_SI;

    /**
     * SCTE SI format.
     */
    public static final ServiceInformationType SCTE_SI;

    /**
     * Unknown format.
     */
    public static final ServiceInformationType UNKNOWN;

    // Needed for compilation
    static {
	ATSC_PSIP = new ServiceInformationType("ATSC_PSIP");
	DVB_SI = new ServiceInformationType("DVB_SI");
	SCTE_SI = new ServiceInformationType("SCTE_SI");
	UNKNOWN = new ServiceInformationType("UNKNOWN");
    }
}
