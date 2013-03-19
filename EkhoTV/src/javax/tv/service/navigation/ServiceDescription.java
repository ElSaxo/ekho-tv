/*
 * @(#)ServiceDescription.java	1.10 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.navigation;

import javax.tv.service.SIRetrievable;

/**
 * 
 * This interface provides a textual description of a <code>Service</code>. (In
 * ATSC PSIP, this information is obtained from the ETT associated with this
 * service.)
 * 
 */
public interface ServiceDescription extends SIRetrievable {

    /**
     * Provides a textual description of the <code>Service</code>.
     * 
     * @return A textual description of the <code>Service</code>, or an empty
     *         string if no description is available.
     */
    public String getServiceDescription();

}
