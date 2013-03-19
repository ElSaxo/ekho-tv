/*
 * @(#)PushDataSource.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media.protocol;

/**
 * Abstracts a data source that manages <CODE>PushDataStreams</CODE>.
 * 
 * @see javax.media.Manager
 * @see javax.media.Player
 * @see javax.media.DefaultPlayerFactory
 * @see DataSource
 * @version 1.7, 98/03/28.
 */

public abstract class PushDataSource extends DataSource {

    /**
     * Get the collection of streams that this source manages. The collection of
     * streams is entirely content dependent. The <code>ContentDescriptor</code>
     * of this <CODE>DataSource</CODE> provides the only indication of what
     * streams can be available on this connection.
     * 
     * @return The collection of streams for this source.
     */
    public abstract PushSourceStream[] getStreams();

}
