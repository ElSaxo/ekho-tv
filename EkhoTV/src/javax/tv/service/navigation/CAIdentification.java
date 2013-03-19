/*
 * @(#)CAIdentification.java	1.14 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.navigation;

/**
 * This interface associates information related to the conditional access (CA)
 * subsystem with certain SI objects.
 * 
 * @see ServiceDetails
 * @see javax.tv.service.guide.ProgramEvent
 * @see javax.tv.service.transport.Bouquet
 */
public interface CAIdentification {

    /**
     * Returns an array of CA System IDs associated with this object. This
     * information may be obtained from the CAT MPEG message or a system
     * specific conditional access descriptor (such as defined by Simulcrypt or
     * ATSC).
     * 
     * @return An array of CA System IDs. An empty array is returned when no CA
     *         System IDs are available.
     */
    public abstract int[] getCASystemIDs();

    /**
     * Provides information concerning conditional access of this object.
     * 
     * @return <code>true</code> if this Service is not protected by a
     *         conditional access; <code>false</code> if one or more components
     *         is protected by conditional access.
     */
    public abstract boolean isFree();
}
