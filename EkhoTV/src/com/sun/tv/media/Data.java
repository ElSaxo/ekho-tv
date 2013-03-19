/*
 * @(#)Data.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media;


/**
 * Base class for audio/video data.
 * 
 * @version 1.18, 98/03/28
 */
abstract public class Data {

    protected long presentationTime = 0;
    protected long duration = 0;
    private boolean discard = false;

    /**
     * getLength() == EOM signifies the last chunk of data from the media.
     */
    public final static int EOM = -1;

    /**
     * @return the data format.
     */
    abstract public Format getFormat();

    /**
     * set data format.
     */
    abstract public boolean setFormat(Format f);

    /**
     * Retrieve the internal buffer. An Object is returned. I could be of byte[]
     * or int[]. "instanceof" can be used to find out.
     * 
     * @return the buffer object.
     */
    abstract public Object getBuffer();

    /**
     * @return the size of the buffer.
     */
    abstract public int getBufferSize();

    /**
     * @return the length of data stored in the byte array. EOM if it's the last
     *         chunk of data from the media.
     */
    abstract public int getLength();

    /**
     * Mark the length of data stored in the byte array.
     * 
     * @param l
     *            length of the data.
     */
    abstract public void setLength(int l);

    /**
     * @return the presentation time of this particular segment of media data.
     */
    public long getPresentationTime() {
	return presentationTime;
    }

    /**
     * Set the presentation time of this particular segment of media data.
     * 
     * @param t
     *            the presentation time.
     */
    public void setPresentationTime(long t) {
	presentationTime = t;
    }

    /**
     * @return the duration of this data.
     */
    public long getDuration() {
	return duration;
    }

    /**
     * Set the duration of the data.
     * 
     * @param duration
     *            the media duration.
     */
    public void setDuration(long duration) {
	this.duration = duration;
    }

    /**
     * Set whether this data should be discarded or not.
     */
    public void setDiscard(boolean f) {
	discard = f;
    }

    /**
     * Indicate whether this data should be discarded or not.
     */
    public boolean getDiscard() {
	return discard;
    }

}
