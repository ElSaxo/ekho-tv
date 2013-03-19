/*
 * @(#)InputConnectable.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

/**
 * <b>Tentative</b><br>
 * <i> This interface is currently under development and is not yet recommended
 * for use. It is included here as an indication of direction and solicitation
 * for comment. </i>
 * <p>
 * 
 * InputConnectable defines the data movement and format typing interface for
 * input ports.
 * 
 * @see OutputConnectable
 * 
 * @version 1.28, 98/03/28
 */
public interface InputConnectable extends Connectable {

    /**
     * <b>Tentative.</b><br>
     * This method is not generally useful to the casual <i>applet</i> writer.
     * Connect an OutputConnectable to this InputConnectable. This method should
     * only be called by the Manager when it is connecting this
     * InputConnectable.
     */
    public void connectTo(OutputConnectable port);

    /**
     * <b>Tentative.</b><br>
     * Return the OutputConnectable this InputConnectable is connected to. If
     * this Connectable is unconnected return null.
     */
    public OutputConnectable connectedTo();

    /**
     * <b>Tentative.</b><br>
     * Attempt to put a data packet into this InputConnectable. Return true if
     * put succeeded and false if the put would have blocked.
     * 
     * @param obj
     *            Container with media data for the connection.
     */
    public boolean tryPutData(Object obj);

    /**
     * <b>Tentative.</b><br>
     * Put a data packet into this InputConnectable. Block until the put
     * completes.
     * 
     * @param obj
     *            Container with media data for the connection.
     */
    public void putData(Object obj);

    /**
     * <b>Tentative.</b><br>
     * Attempt to get a empty data container. Return a container or null if the
     * get would have blocked.
     */
    public Object tryGetContainer();

    /**
     * <b>Tentative.</b><br>
     * Get an empty data container object. Blocks until a container is
     * available.
     */
    public Object getContainer();

    /**
     * <b>Tentative.</b><br>
     * Attempt to get a empty data container. Return a container or null if the
     * get would have blocked.
     */
    public Object tryGetContainer(Format format);

    /**
     * <b>Tentative.</b><br>
     * Get an empty data container object. Blocks until a container is
     * available.
     */
    public Object getContainer(Format format);

    /**
     * <b>Tentative.</b><br>
     * Return an unused container to this input connectable. Typically this is
     * only called when the <a href=OutputConnectable.html>OutputConnectable</a>
     * connected to this input connectable is trying to flush some containers
     * that it was holding onto.
     * 
     * @param obj
     *            An unused data container
     */
    public void putContainer(Object obj);

    /**
     * <b>Tentative.</b><br>
     * Remove all down stream data associated with this connectable.
     * <b>flush</b> indicates that this input connectable should:
     * <ul>
     * <li>Purge any data the connectable is holding onto from previous
     * <b>putData</b> or <b>tryPutData</b> requests.
     * 
     * <li>Return all containers that are currently being held by output
     * connectables associated with the processor that owns this input
     * connectable. This is done with the <b>putContainer</b> method.
     * 
     * <li>Propagate the flush request to the input connectables connected to
     * the appropriate output connectables of the processor associated with this
     * input connectable.
     * 
     * <li>When all attached input connectables have returned from the flush
     * call, indicating that they have flushed all their downstream connections,
     * <b>flush</b> should return indicating flush complete.
     * 
     * </ul>
     */
    public abstract void flush();
}
