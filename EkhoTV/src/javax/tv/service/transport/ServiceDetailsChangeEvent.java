/*
 * @(#)ServiceDetailsChangeEvent.java	1.4 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.transport;

import javax.tv.service.SIChangeType;
import javax.tv.service.navigation.ServiceDetails;

/**
 * A <code>ServiceDetailsChangeEvent</code> notifies an
 * <code>ServiceDetailsChangeListener</code> of changes detected to a
 * <code>ServiceDetails</code> on a <code>Transport</code>. Specifically, this
 * event signals the addition, removal, or modification of a
 * <code>ServiceDetails</code>.
 * 
 * @see Transport
 * @see ServiceDetails
 */
public class ServiceDetailsChangeEvent extends TransportSIChangeEvent {

    /**
     * Constructs a <code>ServiceDetailsChangeEvent</code>.
     * 
     * @param transport
     *            The <code>Transport</code> on which the change occurred.
     * 
     * @param type
     *            The type of change that occurred.
     * 
     * @param s
     *            The <code>ServiceDetails</code> that changed.
     */
    public ServiceDetailsChangeEvent(Transport transport, SIChangeType type,
	    ServiceDetails s) {
	super(transport, type, s);
    }

    /**
     * Reports the <code>ServiceDetails</code> that changed. It will be
     * identical to the object returned by the inherited
     * <code>SIChangeEvent.getSIElement</code> method.
     * 
     * @return The <code>ServiceDetails</code> that changed.
     */
    public ServiceDetails getServiceDetails() {
	return (ServiceDetails) super.getSIElement();
    }
}
