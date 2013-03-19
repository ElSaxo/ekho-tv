/*
 * @(#)Codec.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

/**
 * The Codec interface declares a set of methods that any codec will need to
 * implement in order to convert encoded/compressed audio or video data to a
 * format that can be understood by a rendering node or device.
 */
public interface Codec {

    /**
     * Return true if the codec supports the given format.
     * 
     * @param format
     *            input decoding format.
     * @return true if it supports the given format.
     */
    public boolean supports(MediaFormat format);

    /**
     * Initialize the codec. "Connect" the codec to its output.
     * 
     * @param output
     *            output of the codec.
     * @param format
     *            input format of the bit stream. null if it's unknown.
     * @return true if it's successful.
     */
    public boolean initialize(CodecOutput output, MediaFormat format);

    /**
     * This method does the actual data conversion. If the conversion failed for
     * any reason, it returns false.
     * 
     * @param inData
     *            input data.
     * @return true if decoding is successful.
     */
    public boolean decode(Data inData);

    /**
     * This method is invoked by the framework before <I>decode()</I> is called
     * to verify that the input data matches the current expected input format.
     * For codecs that can deal with changing formats, this is the place the
     * codec is allowed to have a glimpse of the inpt data and change the
     * decoding format before actually decoding the data in <I>decode()</I>.
     * 
     * @param inData
     *            the input data.
     * @return true if the given data is of the expected input format; false if
     *         the given data does not have the right input format.
     */
    public boolean checkFormat(Data inData);

    /**
     * Returns the format of the input data that the codec is expecting.
     * 
     * @see #decode
     */
    public Format getInputFormat();

    /**
     * Returns the format of the output produced by the codec after a call to
     * <I>decode(...)</I>.
     * 
     * @see #decode
     */
    public Format getOutputFormat();

    /**
     * Returns the minimum scaling factor the codec can support.
     * 
     * @return the (linear) scaling factor.
     */
    public float getMinScale();

    /**
     * Returns the maximum scaling factor the codec can support.
     * 
     * @return the (linear) scaling factor.
     */
    public float getMaxScale();

    /**
     * Returns the current scaling factor.
     * 
     * @return the (linear) scaling factor.
     */
    public float getScale();

    /**
     * Set the current scaling factor.
     * 
     * @return the (lineas) scaling factor set.
     */
    public float setScale(float scale);

    /**
     * Flush the decoder. Clear any holding buffers.
     */
    public void flush();
}
