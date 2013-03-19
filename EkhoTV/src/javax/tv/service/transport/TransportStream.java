/*
 * @(#)TransportStream.java	1.13 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.transport;

import javax.tv.service.SIElement;

/**
 * This interface provides information about a transport stream.
 */
public interface TransportStream extends SIElement {

    /**
     * Reports the ID of this transport stream.
     * 
     * @return A number identifying this transport stream.
     */
    public abstract int getTransportStreamID();

    /**
     * Reports the textual name or description of this transport stream.
     * 
     * @return A string representing the name of this transport stream, or an
     *         empty string if no information is available.
     */
    public abstract String getDescription();

}
