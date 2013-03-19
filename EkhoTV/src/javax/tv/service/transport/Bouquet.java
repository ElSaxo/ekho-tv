/*
 * @(#)Bouquet.java	1.14 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.transport;

import javax.tv.service.SIElement;

/**
 * This interface represents information about a bouquet.
 * <p>
 * 
 * A <code>Bouquet</code> object may optionally implement the
 * <code>CAIdentification</code> interface. Note that bouquets are not supported
 * in ATSC.
 * 
 * @see javax.tv.service.navigation.CAIdentification
 * 
 * @see <a
 *      href="../../../../overview-summary.html#guidelines-opinterfaces">Optionally
 *      implemented interfaces</a>
 */
public interface Bouquet extends SIElement {

    /**
     * Reports the ID of this bouquet definition.
     * 
     * @return A number identifying this bouquet
     */
    public abstract int getBouquetID();

    /**
     * Reports the name of this bouquet.
     * 
     * @return A string representing the name of this bouquet, or an empty
     *         string if the name is not available.
     */
    public abstract String getName();
}
