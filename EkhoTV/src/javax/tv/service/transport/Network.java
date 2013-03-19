/*
 * @(#)Network.java	1.15 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.transport;

import javax.tv.service.SIElement;
import javax.tv.service.SIRequest;
import javax.tv.service.SIRequestor;

/**
 * This interface provides descriptive information concerning a network.
 */
public interface Network extends SIElement {

    /**
     * Reports the ID of this network.
     * 
     * @return A number identifying this network.
     */
    public abstract int getNetworkID();

    /**
     * Reports the name of this network.
     * 
     * @return A string representing the name of this network, or an empty
     *         string if the name is unavailable.
     */
    public abstract String getName();

    /**
     * Retrieves an array of <code>TransportStream</code> objects representing
     * the transport streams carried in this <code>Network</code>. Only
     * <code>TransportStream</code> instances <code>ts</code> for which the
     * caller has <code>javax.tv.service.ReadPermission(ts.getLocator())</code>
     * will be present in the array. If no <code>TransportStream</code>
     * instances meet this criteria or if this <code>Network</code> does not
     * aggregate transport streams, the result is an
     * <code>SIRequestFailureType</code> of <code>DATA_UNAVAILABLE</code>.
     * <p>
     * 
     * This method delivers its results asynchronously.
     * 
     * @param requestor
     *            The <code>SIRequestor</code> to be notified when this
     *            retrieval operation completes.
     * 
     * @return An <code>SIRequest</code> object identifying this asynchronous
     *         retrieval request.
     * 
     * @see TransportStream
     * @see javax.tv.service.ReadPermission
     */
    public abstract SIRequest retrieveTransportStreams(SIRequestor requestor);
}
