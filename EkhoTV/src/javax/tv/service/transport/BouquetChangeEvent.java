/*
 * @(#)BouquetChangeEvent.java	1.5 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.transport;

import javax.tv.service.SIChangeType;

/**
 * A <code>BouquetChangeEvent</code> notifies an
 * <code>BouquetChangeListener</code> of changes detected in a
 * <code>BouquetCollection</code>. Specifically, this event signals the
 * addition, removal, or modification of a <code>Bouquet</code>.
 * 
 * @see BouquetCollection
 * @see Bouquet
 */
public class BouquetChangeEvent extends TransportSIChangeEvent {

    /**
     * Constructs a <code>BouquetChangeEvent</code>.
     * 
     * @param collection
     *            The <code>BouquetCollection</code> in which the change
     *            occurred.
     * 
     * @param type
     *            The type of change that occurred.
     * 
     * @param b
     *            The <code>Bouquet</code> that changed.
     */
    public BouquetChangeEvent(BouquetCollection collection, SIChangeType type,
	    Bouquet b) {
	super(collection, type, b);
    }

    /**
     * Reports the <code>BouquetCollection</code> that generated the event. It
     * will be identical to the object returned by the
     * <code>getTransport()</code> method.
     * 
     * @return The <code>BouquetCollection</code> that generated the event.
     */
    public BouquetCollection getBouquetCollection() {
	return (BouquetCollection) getTransport();
    }

    /**
     * Reports the <code>Bouquet</code> that changed. It will be identical to
     * the object returned by the inherited
     * <code>SIChangeEvent.getSIElement</code> method.
     * 
     * @return The <code>Bouquet</code> that changed.
     */
    public Bouquet getBouquet() {
	return (Bouquet) getSIElement();
    }
}
