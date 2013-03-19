/*
 * @(#)MediaDecoder.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

/**
 * The MediaDecoder node that can be connected to the JMF signal graph. It has
 * one input and one output following the signal flow graph semantics.
 */
abstract public class MediaDecoder extends MediaBufferedFilter implements
	CodecOutput {

    private Codec codec; // Pluggin codec.

    /**
     * Given a format, check to see if the decoder supports it or not.
     * 
     * @param format
     *            input format of the bit stream.
     */
    public abstract boolean supports(MediaFormat format);

    /**
     * Sets the pluggin codec
     * 
     * @param c
     *            pluggin codec.
     */
    public void setCodec(Codec c) {
	codec = c;
    }

    /**
     * Gets the pluggin codec.
     * 
     * @return the pluggin codec.
     */
    public Codec getCodec() {
	return codec;
    }

    /**
     * Returns the output buffer from the downstream node.
     * 
     * @param fmt
     *            specifies the format of the buffer requested.
     * @return the buffer.
     */
    public Data getOutputBuffer(Format fmt) {
	InputConnectable ic = output.connectedTo();
	return (Data) ic.getContainer(fmt);

    }

    /**
     * A non-blocking version of getOutputBuffer.
     * 
     * @param fmt
     *            specifies the format of the buffer requested.
     * @return the buffer.
     */
    public Data tryGetOutputBuffer(Format fmt) {
	InputConnectable ic = output.connectedTo();
	return (Data) ic.tryGetContainer(fmt);
    }

    /**
     * Gets called when a "frame" is done decoding.
     * 
     * @param decoded
     *            the decoded "frame".
     */
    public void decodeUpdate(Data decoded) {
	InputConnectable ic = output.connectedTo();
	ic.putData(decoded);
    }

    /**
     * Gets called when the decoding of a "frame" failed.
     * 
     * @param decoded
     *            the buffer to be returned back to the renderer.
     */
    public void decodeFailureUpdate(Data decoded) {
	InputConnectable ic = output.connectedTo();
	ic.putContainer(decoded);
    }

    /**
     * Gets called by the upstream node with a compressed chunk of data.
     * 
     * @param in
     *            the compressed data.
     */
    public void putData(Data in) {
	if (codec != null) {
	    codec.checkFormat(in);
	    codec.decode(in);
	}
	bufQ.putbackFree(in);
    }

    /**
     * Put the data through without decoding.
     * 
     * @param in
     *            the input data.
     */
    public void putThrough(Data in) {

	flushing = false;

	InputConnectable ic = output.connectedTo();
	Data out = (Data) ic.getContainer(in.getFormat());

	if (flushing) {
	    ic.putContainer(out);
	    bufQ.putbackFree(in);
	    flushing = false;
	    return;
	}

	if (in.getLength() > 0)
	    System.arraycopy(in.getBuffer(), 0, out.getBuffer(), 0,
		    in.getLength());
	out.setLength(in.getLength());
	out.setFormat(in.getFormat());
	ic.putData(out);
	bufQ.putbackFree(in);
    }

    /**
     * Overrides MediaBufferedFilter's processData(). Not used.
     */
    protected boolean processData(Data in, Data out) {
	return true;
    }

    /**
     * Returns the minimum scaling factor supported by the codec.
     * 
     * @return (linear) scaling factor.
     */
    public float getMinScale() {
	return codec.getMinScale();
    }

    /**
     * Returns the maximum scaling factor supported by the codec.
     * 
     * @return (linear) scaling factor.
     */
    public float getMaxScale() {
	return codec.getMaxScale();
    }

    /**
     * Returns the current scaling factor.
     * 
     * @return the (linear) scaling factor.
     */
    public float getScale() {
	return codec.getScale();
    }

    /**
     * Sets the current scaling factor.
     * 
     * @return the (lineas) scaling factor set.
     */
    public synchronized float setScale(float scale) {
	return codec.setScale(scale);
    }

}
