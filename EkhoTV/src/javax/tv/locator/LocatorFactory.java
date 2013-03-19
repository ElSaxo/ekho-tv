/*
 * @(#)LocatorFactory.java	1.19 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.locator;

/**
 * This class defines a factory for the creation of <code>Locator</code>
 * objects.
 * 
 * @see javax.tv.locator.Locator
 */
public abstract class LocatorFactory {

    private static LocatorFactory theLocatorFactory = null;

    /**
     * Creates the <code>LocatorFactory</code> instance.
     */
    protected LocatorFactory() {
    }

    /**
     * Provides an instance of <code>LocatorFactory</code>.
     * 
     * @return A <code>LocatorFactory</code> instance.
     */
    public static LocatorFactory getInstance() {
	if (theLocatorFactory != null) {
	    return theLocatorFactory;
	}

	try {
	    theLocatorFactory = new com.sun.tv.LocatorFactoryImpl();

	} catch (Exception e) {
	    ;
	}

	return theLocatorFactory;
    }

    /**
     * Creates a <code>Locator</code> object from the specified locator string.
     * The format of the locator string may be entirely implementation-specific.
     * 
     * @param locatorString
     *            The string form of the <code>Locator</code> to be created. The
     *            created <code>Locator</code> will have an external form that
     *            is identical to <code>locatorString</code>.
     * 
     * @return A <code>Locator</code> object representing the resource
     *         referenced by the given locator string.
     * 
     * @throws MalformedLocatorException
     *             If an incorrectly formatted locator string is detected.
     * 
     * @see Locator#toExternalForm
     */
    public abstract Locator createLocator(String locatorString)
	    throws MalformedLocatorException;

    /**
     * Transforms a <code>Locator</code> into its respective collection of
     * transport dependent <code>Locator</code> objects. A transformation on a
     * transport dependent <code>Locator</code> results in an identity
     * transformation, i.e. the same locator is returned in a single-element
     * array.
     * 
     * @param source
     *            The <code>Locator</code> to transform.
     * 
     * @return An array of transport dependent <code>Locator</code> objects for
     *         the given <code>Locator</code>.
     * 
     * @throws InvalidLocatorException
     *             If <code>source</code> is not a valid Locator.
     */
    public abstract Locator[] transformLocator(Locator source)
	    throws InvalidLocatorException;
}
