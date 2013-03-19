/*
 * @(#)MediaPullSourceStream.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.Seekable;

import com.sun.tv.media.protocol.InputSourceStream;

public abstract class MediaPullSourceStream extends InputSourceStream implements
	Seekable {

    protected URL url;
    protected long contentLength = LENGTH_UNKNOWN;

    public MediaPullSourceStream(InputStream s, ContentDescriptor type, URL r,
	    long length) {
	super(s, type);
	if (length >= 0)
	    contentLength = length;
	url = r;
    }

    public boolean isRandomAccess() {
	return true;
    }

    public URL getURL() {
	return url;
    }

    public long getContentLength() {
	return contentLength;
    }

    abstract public long skip(long where) throws IOException;

    /**
     * seek to this point in the stream
     * 
     * @param where
     *            The position to seek to, in bytes.
     * @return The new stream position, in bytes.
     */
    abstract public long seek(long where);

    /**
     * Obtain the current pointer in the stream.
     */
    abstract public long tell();

    /**
     * Returns an zero length array because no controls are supported.
     * 
     * @return a zero length <code>Object</code> array.
     */
    public Object[] getControls() {
	return new Object[0];
    }

    /**
     * Returns <code>null</code> because no controls are implemented.
     * 
     * @return <code>null</code>.
     */
    public Object getControl(String controlName) {
	return null;
    }

}
