/*
 * @(#)ServiceMinorNumber.java	1.12 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service;

/**
 * This interface extends the basic <code>ServiceNumber</code> interface to
 * provide the minor number of two-part service numbers described in
 * <em>major.minor</em> format.
 * <p>
 * 
 * Service and ServiceDetails objects may optionally implement this interface.
 * <p>
 * 
 * The major number of a service is obtained from the
 * <code>ServiceNumber.getServiceNumber</code> method.
 * 
 * @see Service
 * 
 * @see javax.tv.service.navigation.ServiceDetails
 * 
 * @see ServiceNumber#getServiceNumber
 * 
 * @see <a
 *      href="../../../overview-summary.html#guidelines-opinterfaces">Optionally
 *      implemented interfaces</a>
 */
public interface ServiceMinorNumber extends ServiceNumber {

    /**
     * Reports the minor number of the service.
     * 
     * @return The minor number of this service.
     */
    public int getMinorNumber();
}
