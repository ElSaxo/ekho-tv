/*
 * @(#)MediaProcessor.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import javax.media.Control;

/**
 * <b>Tentative</b><br>
 * <i> This interface is currently under development and is not yet recommended
 * for use. It is included here as an indication of direction and solicitation
 * for comment. </i>
 * <p>
 * 
 * A MediaProcessor is a collection of Connectables, Controls, and Processing
 * behavior.
 * 
 * @see Control
 * @see Connectable
 * @see InputConnectable
 * @see OutputConnectable
 * @version 1.13, 98/03/28
 */
public interface MediaProcessor {

    public final static long LATENCY_UNKNOWN = Long.MAX_VALUE;

    /**
     * <b>Tentative.</b><br>
     * Return a list of <b>Control</b> objects this media processor exposes. If
     * this media processor has no control objects an array of length zero is
     * returned.
     * 
     * @return list of media processor controls.
     */
    public Control[] getControls();

    /**
     * <b>Tentative.</b><br>
     * How far in advance of presentation does this media processor need a frame
     * of data.
     * 
     * @return latency in nanoseconds.
     */
    public long getLatency();

    /**
     * <b>Tentative.</b><br>
     * Return an array of strings containing this media processor's input port
     * names (both connected and unconnected). If this media processor contains
     * no inputs an array of length zero is returned.
     * 
     * @return list of input names.
     */
    public String[] listInputs();

    /**
     * <b>Tentative.</b><br>
     * Return an array of strings containing this media processor's output port
     * names (both connected and unconnected). If this media processor contains
     * no outputs an array of length zero is returned.
     * 
     * @return list of output names.
     */
    public String[] listOutputs();

    /**
     * <b>Tentative.</b><br>
     * Return the specified port. Ports and port/name mapping are typically
     * constructed in the media processor's constructor. Returns null if the
     * string doesn't match any of the port's InputConnectables.
     * 
     * @param portName
     *            the name of the port
     * @return InputConnectable associated with this name.
     */
    public InputConnectable getInputPort(String portName);

    /**
     * <b>Tentative.</b><br>
     * Return the specified port. Ports and port/name mapping are typically
     * constructed in the media processor's constructor. Returns null if the
     * String doesn't match any of the media processor's OutputConnectables.
     * 
     * @param portName
     *            the name of the port
     * @return OutputConnectable associated with this name.
     */
    public OutputConnectable getOutputPort(String portName);

    /**
     * Flushing the data pipe. This should involve the following: 1) flush the
     * local buffers. 2) propagate the flush to down stream nodes.
     */
    public void flush();

}
