/*
 * @(#)RatingDimension.java	1.17 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service;


/**
 * The <code>RatingDimension</code> interface represents an individual content
 * rating scheme against which program events are rated. Each rating region may
 * support multiple rating dimensions. One dimension in the U.S. rating region,
 * for example, is used to describe the MPAA list. The dimension name for such a
 * case may be defined as "MPAA". Another example of a rating dimension may be
 * an age-based DVB rating.
 * 
 * @see javax.tv.service.guide.ProgramEvent
 * @see javax.tv.service.guide.ContentRatingAdvisory
 */
public interface RatingDimension {

    /**
     * Returns a string which represents the dimension name being described by
     * this object. One dimension in the U.S. rating region, for example, is
     * used to describe the MPAA list. The dimension name for such a case may be
     * defined as "MPAA".
     * 
     * @return A string representing the name of this rating dimension.
     */
    public abstract String getDimensionName();

    /**
     * Returns the number of levels defined for this dimension.
     * 
     * @return The number of levels in this dimension.
     */
    public abstract short getNumberOfLevels();

    /**
     * Returns a pair of strings describing the specified rating level for this
     * dimension.
     * 
     * @param ratingLevel
     *            The rating level for which to retrieve the textual
     *            description.
     * 
     * @return A pair of strings representing the names for the specified rating
     *         level. The first string represents the abbreviated name for the
     *         rating level. The second string represents the full name for the
     *         rating level.
     * 
     * @throws SIException
     *             If <code>ratingLevel</code> is not valid for this
     *             <code>RatingDimension</code>.
     * 
     * @see javax.tv.service.guide.ContentRatingAdvisory#getRatingLevel
     */
    public abstract String[] getRatingLevelDescription(short ratingLevel)
	    throws SIException;
}
