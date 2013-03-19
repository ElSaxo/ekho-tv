/*
 * @(#)FavoriteServicesName.java	1.10 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.navigation;

/**
 * This interface represents the name of a preference for a set of favorite
 * services. It can be used to create a collection of <code>Service</code>
 * objects based on a user preference for favorite services.
 * 
 * @see PreferenceFilter
 */
public interface FavoriteServicesName {

    /**
     * Provides a human-readable name for this favorite services preference.
     * 
     * @return The name of the favorite services preference.
     */
    public String getName();
}
