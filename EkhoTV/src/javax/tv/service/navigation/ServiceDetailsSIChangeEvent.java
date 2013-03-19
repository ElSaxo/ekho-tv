/*
 * @(#)ServiceDetailsSIChangeEvent.java	1.4 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.navigation;

import javax.tv.service.SIChangeEvent;
import javax.tv.service.SIChangeType;
import javax.tv.service.SIElement;

/**
 * A <code>ServiceDetailsSIChangeEvent</code> notifies an
 * <code>SIChangeListener</code> of changes to a <code>ServiceDetails</code>.
 * 
 * @see ServiceDetails
 * @see ServiceDetails
 */
public abstract class ServiceDetailsSIChangeEvent extends SIChangeEvent {

    /**
     * Constructs a <code>ServiceDetailsSIChangeEvent</code>.
     * 
     * @param service
     *            The <code>ServiceDetails</code> in which the change occurred.
     * 
     * @param type
     *            The type of change that occurred.
     * 
     * @param e
     *            The <code>SIElement</code> that changed.
     */
    public ServiceDetailsSIChangeEvent(ServiceDetails service,
	    SIChangeType type, SIElement e) {
	super(service, type, e);
    }

    /**
     * Reports the <code>ServiceDetails</code> that generated the event. It will
     * be identical to the object returned by the <code>getSource()</code>
     * method.
     * 
     * @return The <code>ServiceDetails</code> that generated the event.
     */
    public ServiceDetails getServiceDetails() {
	return (ServiceDetails) super.getSource();
    }
}
