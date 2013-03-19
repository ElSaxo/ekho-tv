/*
 * @(#)Seekable.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media.protocol;

/**
 * A <code>SourceStream</code> will implement this interface if it is capable of
 * seeking to a particular position in the stream.
 * 
 * @see SourceStream
 * @version 1.8, 98/03/28.
 */

public interface Seekable {

    /**
     * Seek to the specified point in the stream.
     * 
     * @param where
     *            The position to seek to.
     * @return The new stream position.
     */
    long seek(long where);

    /**
     * Obtain the current point in the stream.
     */
    long tell();

    /**
     * Find out if this source can position anywhere in the stream. If the
     * stream is not random access, it can only be repositioned to the
     * beginning.
     * 
     * @return Returns <CODE>true</CODE> if the stream is random access,
     *         <CODE>false</CODE> if the stream can only be reset to the
     *         beginning.
     */
    boolean isRandomAccess();

}
