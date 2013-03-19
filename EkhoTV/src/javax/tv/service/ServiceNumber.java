/*
 * @(#)ServiceNumber.java	1.14 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service;

/**
 * This interface is used to identify services by service (or channel) numbers.
 * The service number may represent a receiver-specific service designation or a
 * broadcaster-specific service designation delivered as a private descriptor.
 * <p>
 * 
 * Service and ServiceDetails objects may optionally implement this interface.
 * <code>ServiceNumber</code> is extended by <code>ServiceMinorNumber</code> to
 * report two-part ATSC channel numbers.
 * 
 * @see Service
 * 
 * @see javax.tv.service.navigation.ServiceDetails
 * 
 * @see ServiceMinorNumber
 * 
 * @see <a
 *      href="../../../overview-summary.html#guidelines-opinterfaces">Optionally
 *      implemented interfaces</a>
 */
public interface ServiceNumber {

    /**
     * Reports the service number of a service.
     * 
     * @return The number of the service.
     */
    public int getServiceNumber();
}
