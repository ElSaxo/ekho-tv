/*
 * @(#)MediaHandler.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

import java.io.IOException;

import javax.media.protocol.DataSource;

/**
 * <code>MediaHandler</code> is the base interface for objects that read and
 * manage media content delivered from a <code>DataSource</code>.
 * <p>
 * 
 * There are currently two supported types of <code>MediaHandler/code>:
 * <code>Player</code> and <code>MediaProxy</code>.
 **/
public interface MediaHandler {

    /**
     * Set the media source the <code>MediaHandler</code> should use to obtain
     * content.
     * 
     * @param source
     *            The <code>DataSource</code> used by this
     *            <code>MediaHandler</code>.
     * 
     * @exception IOException
     *                Thrown if there is an error using the
     *                <code>DataSource</code>
     * 
     * @exception IncompatibleSourceException
     *                Thrown if this <code>MediaHandler</code> cannot make use
     *                of the <code>DataSource</code>.
     */
    public void setSource(DataSource source) throws IOException,
	    IncompatibleSourceException;

}
