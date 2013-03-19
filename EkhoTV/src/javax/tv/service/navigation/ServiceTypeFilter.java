/*
 * @(#)ServiceTypeFilter.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.navigation;

import javax.tv.service.Service;
import javax.tv.service.ServiceType;

/**
 * <code>ServiceTypeFilter</code> represents a <code>ServiceFilter</code> based
 * on a particular <code>ServiceType</code>. A <code>ServiceList</code>
 * resulting from this filter will include only <code>Service</code> objects of
 * the specified service type.
 * 
 * @see ServiceType
 * @see ServiceList
 */
public final class ServiceTypeFilter extends ServiceFilter {

    private ServiceType type;

    /**
     * Constructs the filter based on a particular <code>ServiceType</code>.
     * 
     * @param type
     *            A <code>ServiceType</code> object indicating the type of
     *            services to be included in a resulting service list.
     */
    public ServiceTypeFilter(ServiceType type) {
	this.type = type;
	if (type == null) {
	    throw new NullPointerException();
	}
    }

    /**
     * Reports the <code>ServiceType</code> used to create this filter.
     * 
     * @return The <code>ServiceType</code> used to create this filter.
     */
    public ServiceType getFilterValue() {
	return this.type;
    }

    /**
     * Tests if the given service passes the filter.
     * 
     * @param service
     *            An individual <code>Service</code> to be evaluated against the
     *            filtering algorithm.
     * 
     * @return <code>true</code> if <code>service</code> is of the type
     *         indicated by the filter value; <code>false</code> otherwise.
     */
    public boolean accept(Service service) {
	ServiceType theType = service.getServiceType();
	if (theType == null) {
	    throw new NullPointerException("accept: ServiceType == null");
	}

	return theType == type; // TBD: is this equality a good one?
    }
}
