/*
 * @(#)SourceStream.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media.protocol;


/**
 * Abstracts a single stream of media data.
 * 
 * <h2>Stream Controls</h2>
 * 
 * A <code>SourceStream</code> might support an operation that is not part of
 * the <code>SourceStream</code> definition. For example a stream might support
 * seeking to a particular byte in the stream. Some operations are dependent on
 * the stream data, and support cannot be determined until the stream is in use.
 * <p>
 * 
 * To obtain all of the objects that provide control over a stream use
 * <code>getControls</code>. To determine if a particular kind of control is
 * available, and obtain the object that implements the control use
 * <code>getControl</code>.
 * 
 * 
 * @see DataSource
 * @see PushSourceStream
 * @see PullSourceStream
 * @see Seekable
 * 
 * @version 1.14, 98/03/28.
 */

public interface SourceStream extends Controls {

    public static final long LENGTH_UNKNOWN = -1;

    /**
     * Get the current content type for this stream.
     * 
     * @return The current <CODE>ContentDescriptor</CODE> for this stream.
     */
    public ContentDescriptor getContentDescriptor();

    /**
     * Get the size, in bytes, of the content on this stream. LENGTH_UNKNOWN is
     * returned if the length is not known.
     * 
     * @return The content length in bytes.
     */
    public long getContentLength();

    /**
     * Find out if the end of the stream has been reached.
     * 
     * @return Returns <CODE>true</CODE> if there is no more data.
     */
    public boolean endOfStream();

}
