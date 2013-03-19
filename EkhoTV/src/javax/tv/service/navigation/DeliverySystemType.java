/*
 * @(#)DeliverySystemType.java	1.4 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.navigation;

/**
 * This class represents values of various types of delivery systems, for
 * example, satellite, cable, etc.
 */
public class DeliverySystemType {

    private String name = null;

    /**
     * Creates a delivery system type object.
     * 
     * @param name
     *            The string name of this type (e.g., "SATELLITE").
     */
    protected DeliverySystemType(String name) {
	this.name = name;
	if (name == null) {
	    throw new NullPointerException("Name is null");
	}
    }

    /**
     * Provides the string name of delivery system type. For the type objects
     * defined in this class, the string name will be identical to the class
     * variable name.
     */
    public String toString() {
	return name;
    }

    /**
     * Satellite delivery system type.
     */
    public static final DeliverySystemType SATELLITE;

    /**
     * Cable delivery system type.
     */
    public static final DeliverySystemType CABLE;

    /**
     * Terrestrial delivery system type.
     */
    public static final DeliverySystemType TERRESTRIAL;

    /**
     * Unknown delivery system type.
     */
    public static final DeliverySystemType UNKNOWN;

    // Needed for compilation
    static {
	SATELLITE = new DeliverySystemType("SATELLITE");
	CABLE = new DeliverySystemType("CABLE");
	TERRESTRIAL = new DeliverySystemType("TERRESTRIAL");
	UNKNOWN = new DeliverySystemType("UNKNOWN");
    }

}
