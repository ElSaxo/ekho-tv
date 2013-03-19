/*
 * @(#)LocatorFactoryImpl.java	1.13 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv;

import javax.tv.locator.InvalidLocatorException;
import javax.tv.locator.Locator;
import javax.tv.locator.LocatorFactory;
import javax.tv.locator.MalformedLocatorException;
import javax.tv.service.SIManager;

/**
 * This class implements a factory for the creation of Java TV locators.
 * 
 * @see javax.tv.locator.Locator
 */
public class LocatorFactoryImpl extends LocatorFactory {

    /**
     * Creates a JavaTV <code>locator</code> from the specified locator string.
     * 
     * @param locatorString
     *            The string format of the locator to be created. The created
     *            Locator will have an external form that is equivalent to
     *            <code>locatorString</code>.
     * 
     * @throws NullPointerException
     *             If <code>locatorString</code> is <code>null</code>.
     * @throws javax.tv.locator.MalformedLocatorException
     *             If an incorrectly formatted locator string is detected.
     * 
     * @see Locator#toExternalForm
     */
    public Locator createLocator(String locatorString)
	    throws MalformedLocatorException {
	if (locatorString == null) {
	    throw new NullPointerException();
	}

	Locator locator = new LocatorImpl(locatorString);
	if (locator == null || locator.toExternalForm() == null) {
	    throw new MalformedLocatorException();
	}

	SIManager.createInstance(); // Get things up and running. (if needed).

	return locator;
    }

    /**
     * Transforms a locator to its respective collection of network dependent
     * locators. A transformation on a network dependent Locator results in an
     * identity transformation.
     * 
     * @param source
     *            The locator to transform.
     * 
     * @return An array of network dependent locators for the given locator.
     * 
     * @throws NullPointerException
     *             If <code>source</code> is <code>null</code>.
     * @throws InvalidLocatorException
     *             If <code>source</code> is not a valid Locator.
     */
    public Locator[] transformLocator(Locator source)
	    throws InvalidLocatorException {

	if (source == null) {
	    throw new NullPointerException();
	}

	LocatorImpl locatorImpl = new LocatorImpl(source.toExternalForm());
	if (locatorImpl.toExternalForm() == null) {
	    throw new InvalidLocatorException(source, source.toExternalForm());
	}

	return LocatorImpl.transformLocator(source);
    }
}
