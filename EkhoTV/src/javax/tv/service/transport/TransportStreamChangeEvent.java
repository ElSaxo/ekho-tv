/*
 * @(#)TransportStreamChangeEvent.java	1.5 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.transport;

import javax.tv.service.SIChangeType;

/**
 * A <code>TransportStreamChangeEvent</code> notifies an
 * <code>TransportStreamChangeListener</code> of changes detected in a
 * <code>TransportStreamCollection</code>. Specifically, this event signals the
 * addition, removal, or modification of a <code>TransportStream</code>.
 * 
 * @see TransportStreamCollection
 * @see TransportStream
 */
public class TransportStreamChangeEvent extends TransportSIChangeEvent {

    /**
     * Constructs a <code>TransportStreamChangeEvent</code>.
     * 
     * @param collection
     *            The transport stream collection in which the change occurred.
     * 
     * @param type
     *            The type of change that occurred.
     * 
     * @param ts
     *            The <code>TransportStream</code> that changed.
     */
    public TransportStreamChangeEvent(TransportStreamCollection collection,
	    SIChangeType type, TransportStream ts) {
	super(collection, type, ts);
    }

    /**
     * Reports the <code>TransportStreamCollection</code> that generated the
     * event. It will be identical to the object returned by the
     * <code>getTransport()</code> method.
     * 
     * @return The <code>TransportStreamCollection</code> that generated the
     *         event.
     */
    public TransportStreamCollection getTransportStreamCollection() {
	return (TransportStreamCollection) getTransport();
    }

    /**
     * Reports the <code>TransportStream</code> that changed. It will be
     * identical to the object returned by the inherited
     * <code>SIChangeEvent.getSIElement</code> method.
     * 
     * @return The <code>TransportStream</code> that changed.
     */
    public TransportStream getTransportStream() {
	return (TransportStream) getSIElement();
    }

}
