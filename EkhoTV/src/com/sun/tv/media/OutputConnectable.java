/*
 * @(#)OutputConnectable.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import javax.media.Manager;

/**
 * <b>Tentative</b><br>
 * <i> This interface is currently under development and is not yet recommended
 * for use. It is included here as an indication of direction and solicitation
 * for comment. </i>
 * <p>
 * 
 * OutputConnectable defines the data movement and format typing interface for
 * output ports.
 * 
 * @see InputConnectable
 * @see Manager
 * 
 * @version 1.22, 98/03/28
 */
public interface OutputConnectable extends Connectable {

    /**
     * <b>Tentative.</b><br>
     * This method is not generally useful to the casual <i>applet</i> writer.
     * Connects an InputConnectable to this OutputConnectable. This method
     * should only be called by the Manager when it is connecting this
     * OutputConnectable.
     * 
     * @param port
     *            InputConnectable to connect to.
     */
    public void connectTo(InputConnectable port);

    /**
     * <b>Tentative.</b><br>
     * Return the InputConnectable this OutputConnectable is connected to. If
     * this Connectable is unconnected return null.
     * 
     * @return the InputConnectable this is connected to.
     */
    public InputConnectable connectedTo();

}
