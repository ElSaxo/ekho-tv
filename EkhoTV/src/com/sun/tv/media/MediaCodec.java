/*
 * @(#)MediaCodec.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

/**
 * This is an implementation base class of the Codec interface to convert
 * encoded/compressed audio or video data to a format that can be understood by
 * a rendering node or device.
 */
abstract public class MediaCodec implements Codec {

    private CodecOutput output;

    public boolean initialize(CodecOutput n, MediaFormat format) {
	output = n;
	return initialize(format);
    }

    abstract protected boolean initialize(MediaFormat format);

    /**
     * Decode the incoming bits. Call notifyFrame() when a "frame" is completely
     * decoded.
     * 
     * @param inData
     *            input compressed data.
     * @return whether the decoding is successful or not.
     */
    abstract public boolean decode(Data inData);

    /**
     * This method is invoked by the framework before decode() is called to
     * verify that the input data matches the current expected input format. For
     * codecs that can deal with changing formats, this is the place the codec
     * is allowed to have a glimpse of the inpt data and change the decoding
     * format before actually decoding the data in decode(in, out).
     * 
     * @param inData
     *            the input data.
     * @return true if the given data is of the expected input format.
     */
    public boolean checkFormat(Data inData) {
	return true;
    }

    /**
     * Returns the format of the input data that the codec is expecting.
     * 
     * @see #decode
     */
    abstract public Format getInputFormat();

    /**
     * Returns the format of the output produced by the codec after a call to
     * <I>decode(...)</I>.
     * 
     * @see #decode
     */
    abstract public Format getOutputFormat();

    /**
     * Returns the minimum scaling factor the codec can support.
     * 
     * @return the (linear) scaling factor.
     */
    public float getMinScale() {
	return (float) 1.0;
    }

    /**
     * Returns the maximum scaling factor the codec can support.
     * 
     * @return the (linear) scaling factor.
     */
    public float getMaxScale() {
	return (float) 1.0;
    }

    /**
     * Returns the current scaling factor.
     * 
     * @return the (linear) scaling factor.
     */
    public float getScale() {
	return (float) 1.0;
    }

    /**
     * Set the current scaling factor.
     * 
     * @return the (lineas) scaling factor set.
     */
    public float setScale(float scale) {
	return (float) 1.0;
    }

    /**
     * Get an output buffer from the downstream node. This is a utility method
     * for use by subclasses of MediaCodec for the actual codec implementation.
     * 
     * @param fmt
     *            specifies the format of the buffer requested.
     * @return the buffer.
     */
    protected Data getOutputBuffer(Format fmt) {
	return output.getOutputBuffer(fmt);
    }

    /**
     * A non-blocking version of getOutputBuffer. This is a utility method for
     * use by subclasses of MediaCodec for the actual codec implementation.
     * 
     * @param fmt
     *            specifies the format of the buffer requested.
     * @return the buffer.
     */
    protected Data tryGetOutputBuffer(Format fmt) {
	return output.tryGetOutputBuffer(fmt);
    }

    /**
     * Call this when a "frame" is done decoding to put the decoded "frame" to
     * the rendering node. This is a utility method for use by subclasses of
     * MediaCodec for the actual codec implementation.
     * 
     * @param decoded
     *            the decoded "frame".
     */
    protected void decodeUpdate(Data decoded) {
	output.decodeUpdate(decoded);
    }

    /**
     * Call this when the decoding of a "frame" fails. This is a utility method
     * for use by subclasses of MediaCodec for the actual codec implementation.
     * 
     * @param decoded
     *            the buffer to be returned back to the renderer.
     */
    protected void decodeFailureUpdate(Data decoded) {
	output.decodeFailureUpdate(decoded);
    }
}
