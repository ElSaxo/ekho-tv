/*
 * @(#)NetworkChangeEvent.java	1.5 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.transport;

import javax.tv.service.SIChangeType;

/**
 * A <code>NetworkChangeEvent</code> notifies an
 * <code>NetworkChangeListener</code> of changes detected in a
 * <code>NetworkCollection</code>. Specifically, this event signals the
 * addition, removal, or modification of a <code>Network</code>.
 * 
 * @see NetworkCollection
 * @see Network
 */
public class NetworkChangeEvent extends TransportSIChangeEvent {

    /**
     * Constructs a <code>NetworkChangeEvent</code>.
     * 
     * @param collection
     *            The network collection in which the change occurred.
     * 
     * @param type
     *            The type of change that occurred.
     * 
     * @param n
     *            The <code>Network</code> that changed.
     */
    public NetworkChangeEvent(NetworkCollection collection, SIChangeType type,
	    Network n) {
	super(collection, type, n);
    }

    /**
     * Reports the <code>NetworkCollection</code> that generated the event. It
     * will be identical to the object returned by the
     * <code>getTransport()</code> method.
     * 
     * @return The <code>NetworkCollection</code> that generated the event.
     */
    public NetworkCollection getNetworkCollection() {
	return (NetworkCollection) getTransport();
    }

    /**
     * Reports the <code>Network</code> that changed. It will be identical to
     * the object returned by the inherited
     * <code>SIChangeEvent.getSIElement</code> method.
     * 
     * @return The <code>Network</code> that changed.
     */
    public Network getNetwork() {
	return (Network) getSIElement();
    }
}
