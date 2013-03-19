/*
 * @(#)URLSourceStream.java	1.4 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media.protocol;

import java.io.IOException;
import java.net.URLConnection;

import com.sun.tv.media.protocol.InputSourceStream;

/**
 * Create a PullSourceStream from a URLConnection.
 * 
 **/
class URLSourceStream extends InputSourceStream {

    protected URLConnection conn;

    public URLSourceStream(URLConnection conn, ContentDescriptor type)
	    throws IOException {
	/* Initialize with the stream. */
	super(conn.getInputStream(), type);
	this.conn = conn;
    }

    /**
     * Obtain the number of bytes available on this stream.
     * 
     * @return the content length for this stream.
     */
    public long getContentLength() {
	/* See if we actually know the content length */
	long len = conn.getContentLength();
	len = (len == -1) ? SourceStream.LENGTH_UNKNOWN : len;

	return len;
    }
}
