/*
 * @(#)Parser.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import java.io.IOException;

import javax.media.protocol.PullSourceStream;

public interface Parser {

    /**
     * Setting the source for the Parser to read from.
     * 
     * @param pss
     *            the source stream
     */
    public void setSourceStream(PullSourceStream pss);

    /**
     * This method parses the file format and set the formats.
     */
    public void readHeader() throws BadHeaderException, IOException;

    /**
     * This method returns the number of tracks.
     */
    public Track[] getTracks();

    /**
     * This method to add track into track list.
     */
    public void addTrack(Track t);

    /**
     * This method returns the length of the media file in bytes.
     */
    public int getLength();

    /**
     * True if streamable. (Header information is at front)
     */
    public boolean isStreamable();

    /**
     * True if interleaved.
     */
    public boolean isInterleaved();

    /**
     * clean up memory.
     */
    public void dispose();

}
