/*
 * @(#)NetworkChangeListener.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.transport;

import javax.tv.service.SIChangeListener;

/**
 * This interface is implemented by applications wishing to receive notification
 * of changes to <code>Network</code> data.
 */
public interface NetworkChangeListener extends SIChangeListener {

    /**
     * Notifies the <code>NetworkChangeListener</code> of a change to a
     * <code>Network</code>.
     * 
     * @param event
     *            A <code>NetworkChangeEvent</code> describing what changed and
     *            how.
     */
    public abstract void notifyChange(NetworkChangeEvent event);
}
