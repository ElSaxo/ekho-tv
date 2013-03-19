/*
 * @(#)SIRetrievable.java	1.5 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service;

import java.util.Date;

/**
 * This interface is implemented by objects that are retrieved from SI data in
 * the broadcast.
 */
public interface SIRetrievable {

    /**
     * Returns the time when this object was last updated from data in the
     * broadcast.
     * 
     * @return The date of the last update in UTC format, or <code>null</code>
     *         if unknown.
     */
    public abstract Date getUpdateTime();
}
