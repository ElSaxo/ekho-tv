/*
 * @(#)FavoriteServicesNameImpl.java	1.11 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.si;

import javax.tv.service.navigation.FavoriteServicesName;
import javax.tv.service.navigation.PreferenceFilter;

/**
 * This class represents the name of a preference for a set of favorite
 * services. It can be used to create a collection of Services based on a user
 * preference for favorite services.
 * 
 * @see PreferenceFilter
 */
public class FavoriteServicesNameImpl implements FavoriteServicesName {

    String name = null;

    public FavoriteServicesNameImpl(String name) {
	this.name = name;
    }

    /**
     * Provides a human-readable name for this favorite services preference.
     * 
     * @return The name of the favorite services preference.
     */
    public String getName() {
	return name;
    }
}
