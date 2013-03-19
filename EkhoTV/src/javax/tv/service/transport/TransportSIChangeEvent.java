/*
 * @(#)TransportSIChangeEvent.java	1.6 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.transport;

import javax.tv.service.SIChangeEvent;
import javax.tv.service.SIChangeType;
import javax.tv.service.SIElement;

/**
 * An <code>TransportSIChangeEvent</code> notifies an
 * <code>SIChangeListener</code> of changes detected to the SI on a
 * <code>Transport</code>.
 * <p>
 * 
 * Subtypes <code>ServiceDetailsChangeEvent</code>,
 * <code>TransportStreamChangeEvent</code>, <code>NetworkChangeEvent</code> and
 * <code>BouquetChangeEvent</code> are used to signal changes to service
 * details, transport streams, networks and bouquets, respectively. Changes to
 * program events are signaled through <code>ProgramScheduleChangeEvent</code>.
 * 
 * @see Transport
 */
public abstract class TransportSIChangeEvent extends SIChangeEvent {

    /**
     * Constructs an <code>TransportSIChangeEvent</code>.
     * 
     * @param transport
     *            The <code>Transport</code> on which the change occurred.
     * 
     * @param type
     *            The type of change that occurred.
     * 
     * @param e
     *            The <code>SIElement</code> that changed.
     */
    public TransportSIChangeEvent(Transport transport, SIChangeType type,
	    SIElement e) {
	super(transport, type, e);
    }

    /**
     * Reports the <code>Transport</code> that generated the event. It will be
     * identical to the object returned by the <code>getSource()</code> method.
     * 
     * @return The <code>Transport</code> that generated the event.
     */
    public Transport getTransport() {
	return (Transport) getSource();
    }
}
