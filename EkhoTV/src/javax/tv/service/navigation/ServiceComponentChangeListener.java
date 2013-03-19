/*
 * @(#)ServiceComponentChangeListener.java	1.4 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.navigation;

import javax.tv.service.SIChangeListener;

/**
 * This interface is implemented by applications wishing to receive notification
 * of changes to <code>ServiceComponent</code> data.
 */
public interface ServiceComponentChangeListener extends SIChangeListener {

    /**
     * Notifies the <code>ServiceComponentChangeListener</code> of a change to a
     * <code>ServiceComponent</code>.
     * 
     * @param event
     *            A <code>ServiceComponentChangeEvent</code> describing what
     *            changed and how.
     */
    public abstract void notifyChange(ServiceComponentChangeEvent event);
}
