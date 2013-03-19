/*
 * @(#)RatingDimensionImpl.java	1.9 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.si;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.tv.service.RatingDimension;
import javax.tv.service.SIException;

/**
 * Each Rating Region may support multiple rating dimensions. One dimension in
 * the U.S. rating region, for example, is used to describe the MPAA list. The
 * dimension name for such a case may be defined as "MPAA". Another example of a
 * rating dimension may be an age-based DVB rating.
 */
public class RatingDimensionImpl implements RatingDimension {

    private static Hashtable ratingDimensions = new Hashtable();

    String name = null;
    Vector list = new Vector();

    public RatingDimensionImpl(String name) {
	this.name = name;
	ratingDimensions.put(name, this);
    }

    public void addRatingLevelDescription(String shortName, String fullName) {
	String namePair[] = new String[2];
	namePair[0] = new String(shortName);
	namePair[1] = new String(fullName);

	list.addElement(namePair);
    }

    /**
     * Returns a string which represents the dimension name being described by
     * this object. One dimension in the U.S. rating region, for example, is
     * used to describe the MPAA list. The dimension name for such a case may be
     * defined as "MPAA".
     * 
     * @return A string representing the name of this rating dimension.
     */
    public String getDimensionName() {
	return this.name;
    }

    /**
     * Returns the number of values defined for this particular dimension.
     * 
     * @return The number of values in this dimension.
     */
    public short getNumberOfLevels() {
	return (short) list.size();
    }

    /**
     * Returns a pair of Strings describing the specified Rating Level for this
     * Dimension.
     * 
     * @param ratingLevel
     *            The rating level for which to retrieve the textual
     *            description.
     * 
     * @return A pair of strings representing the rating level names for the
     *         specified rating level. The first string represents the
     *         abbreviated name for one particular rating value. The second
     *         string represents the full name for one particular rating value.
     * 
     * @throws SIException
     *             If <code>ratingLevel</code> is not valid for this
     *             <code>RatingDimensionImpl</code>.
     * 
     * @see javax.tv.service.guide.ContentRatingAdvisory#getRatingLevel
     */
    public String[] getRatingLevelDescription(short ratingLevel)
	    throws SIException {
	if (ratingLevel < 0 || (int) ratingLevel >= list.size()) {
	    throw new SIException(ratingLevel + " < 0 or >= " + list.size());
	}
	return (String[]) list.elementAt((int) ratingLevel);
    }

    /**
     * Provides a list of names of available rating dimensions for the local
     * rating region.
     * 
     * @return An array of Strings representing names of available rating
     *         dimensions for this rating region.
     * 
     * @see RatingDimension
     */
    public static java.lang.String[] getSupportedDimensions() {

	int count = 0;
	Enumeration list = ratingDimensions.keys();
	while (list.hasMoreElements()) {
	    list.nextElement();
	    count++;
	}

	if (count == 0) {
	    // return null;
	    return new String[0];
	}

	String strs[] = new String[count];

	count = 0;
	list = ratingDimensions.keys();
	while (list.hasMoreElements()) {
	    strs[count++] = (String) list.nextElement();
	}

	return strs;
    }

    public short findRatingLevel(String longName) {
	for (int i = 0; i < list.size(); i++) {
	    String pair[] = (String[]) list.elementAt(i);
	    if (longName.equals(pair[1])) {
		return (short) i;
	    }
	}
	return -1;
    }

    /**
     * Returns the Rating Dimension corresponding to the specified Dimension
     * name.
     * 
     * @param name
     *            Name of the requested rating dimension
     * 
     * @return The requested RatingDimension.
     */
    public static RatingDimension getRatingDimension(String name) {
	if (name == null)
	    return null;

	return (RatingDimension) ratingDimensions.get(name);
    }
}
