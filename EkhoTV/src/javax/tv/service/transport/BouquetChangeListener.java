/*
 * @(#)BouquetChangeListener.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.transport;

import javax.tv.service.SIChangeListener;

/**
 * This interface is implemented by applications wishing to receive notification
 * of changes to <code>Bouquet</code> data.
 */
public interface BouquetChangeListener extends SIChangeListener {

    /**
     * Notifies the <code>BouquetChangeListener</code> of a change to a
     * <code>Bouquet</code>.
     * 
     * @param event
     *            A <code>BouquetChangeEvent</code> describing what changed and
     *            how.
     */
    public abstract void notifyChange(BouquetChangeEvent event);
}
