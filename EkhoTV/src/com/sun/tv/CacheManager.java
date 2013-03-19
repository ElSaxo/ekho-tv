/*
 * @(#)CacheManager.java	1.7 08/09/15
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 */
package com.sun.tv;

import javax.tv.locator.Locator;

/**
 * This class manages the SI cache. Currently, the cache is implemented as a
 * hashtable which is keyed using locators external form strings. And the
 * elements are made up of SIElements or derivations thereof.
 */
public class CacheManager extends java.util.Hashtable {

    private static CacheManager siCache = new CacheManager();
    private static CacheManager serviceCache = new CacheManager();

    public CacheManager() {
	super();
    }

    public static CacheManager getSICache() {
	return siCache;
    }

    public static CacheManager getServiceCache() {
	return serviceCache;
    }

    public synchronized void put(Locator locator, Object object) {
	String key = locator.toExternalForm();
	if (containsKey(key)) {
	    throw new IllegalArgumentException("Duplicate key: " + key);
	}

	this.put(key, object);
    }

    public Object get(Locator locator) {
	return this.get(locator.toExternalForm());
    }

    public boolean containsKey(Locator locator) {
	return this.containsKey(locator.toExternalForm());
    }

    public synchronized void remove(Locator locator) {
	this.remove(locator.toExternalForm());
    }
}
