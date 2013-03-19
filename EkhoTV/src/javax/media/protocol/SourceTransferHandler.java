/*
 * @(#)SourceTransferHandler.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media.protocol;

/**
 * Implements the callback from a <CODE>PushSourceStream</CODE>.
 * 
 * @see PushSourceStream
 * @version 1.7, 98/03/28.
 */

public interface SourceTransferHandler {

    /**
     * Transfer new data from a <CODE>PushSourceStream</CODE>.
     * 
     * @param stream
     *            The stream that is providing the data.
     */
    public void transferData(PushSourceStream stream);
}
