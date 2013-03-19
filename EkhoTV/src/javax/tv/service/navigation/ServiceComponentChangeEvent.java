/*
 * @(#)ServiceComponentChangeEvent.java	1.5 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.navigation;

import javax.tv.service.SIChangeType;

/**
 * A <code>ServiceComponentChangeEvent</code> notifies an
 * <code>ServiceComponentChangeListener</code> of changes to a
 * <code>ServiceComponent</code> detected in a <code>ServiceDetails</code>.
 * Specifically, this event signals the addition, removal, or modification of a
 * <code>ServiceComponent</code>.
 * 
 * @see ServiceDetails
 * @see ServiceComponent
 */
public class ServiceComponentChangeEvent extends ServiceDetailsSIChangeEvent {

    /**
     * Constructs a <code>ServiceComponentChangeEvent</code>.
     * 
     * @param service
     *            The <code>ServiceDetails</code> in which the change occurred.
     * 
     * @param type
     *            The type of change that occurred.
     * 
     * @param c
     *            The <code>ServiceComponent</code> that changed.
     */
    public ServiceComponentChangeEvent(ServiceDetails service,
	    SIChangeType type, ServiceComponent c) {
	super(service, type, c);
    }

    /**
     * Reports the <code>ServiceComponent</code> that changed. It will be
     * identical to the object returned by the inherited
     * <code>SIChangeEvent.getSIElement</code> method.
     * 
     * @return The <code>ServiceComponent</code> that changed.
     */
    public ServiceComponent getServiceComponent() {
	return (ServiceComponent) getSIElement();
    }
}
