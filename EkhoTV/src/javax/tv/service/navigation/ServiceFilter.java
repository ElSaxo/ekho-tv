/*
 * @(#)ServiceFilter.java	1.18 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.navigation;

import javax.tv.service.Service;

/**
 * This class represents a set filtering criteria used to generate a
 * <code>ServiceList</code>. <code>ServiceFilter</code> is extended to create
 * concrete filters based on various criteria. Applications may also extend this
 * class to define custom filters, although custom filters may not be supported
 * on certain filtering operations.
 * 
 * @see ServiceList
 */
public abstract class ServiceFilter {

    /**
     * Constructs the filter.
     */
    protected ServiceFilter() {
    }

    /**
     * Tests if a particular service passes this filter. Subtypes of
     * <code>ServiceFilter</code> override this method to provide the logic for
     * a filtering operation on individual <code>Service</code> objects.
     * 
     * @param service
     *            A <code>Service</code> to be evaluated against the filtering
     *            algorithm.
     * 
     * @return <code>true</code> if <code>service</code> satisfies the filtering
     *         algorithm; <code>false</code> otherwise.
     */
    public abstract boolean accept(Service service);
}
