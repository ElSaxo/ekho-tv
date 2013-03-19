/*
 * @(#)CodecOutput.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

/**
 * An abstraction of the output interface from a codec.
 */
public interface CodecOutput {

    /**
     * Gets an output buffer to hold the decoded bits. It blocks until the
     * requested output buffer is available.
     * 
     * @param fmt
     *            specifies the format of the buffer requested.
     * @return the buffer.
     */
    public Data getOutputBuffer(Format fmt);

    /**
     * A non-blocking version of getOutputBuffer.
     * 
     * @param fmt
     *            specifies the format of the buffer requested.
     * @return the buffer.
     */
    public Data tryGetOutputBuffer(Format fmt);

    /**
     * Gets called when a "frame" is done decoded.
     * 
     * @param decoded
     *            the decoded "frame".
     */
    public void decodeUpdate(Data decoded);

    /**
     * Gets called when the decoding of a "frame" fails.
     * 
     * @param decoded
     *            the buffer to be returned back to the pool of buffers.
     */
    public void decodeFailureUpdate(Data decoded);

}
