/*
 * @(#)Format.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

/**
 * <b>Tentative</b><br>
 * <i> This interface is currently under development and is not yet recommended
 * for use. It is included here as an indication of direction and solicitation
 * for comment. </i>
 * <p>
 * 
 * Operations to allow format negotiation.
 * 
 * It is common to describe media data as a collection of attributes that can
 * take values from a prescribed set. For example audio data often has a sample
 * rate expressed as a floating point number. It is also common to describe
 * media devices and media processing as capable of operating with media data
 * over a broad range of possible values for particular attributes. Thus an
 * audio renderer may be capable of accepting audio data with any sample rate
 * between the ranges of 8000 and 48000.
 * 
 * The format interface provides methods to facilitate the negotiation between
 * objects that describe media data.
 * 
 * 
 * <h3>Match</h3>
 * 
 * Match returns a Format determined by the object it is called upon. This
 * Format is <i>format compatible</i> with the two objects involved in the
 * match. The returned format is, in some sense, an intersection of the two
 * formats that are provided to match.
 * <p>
 * Match is guaranteed to return a fully specified format. This means that there
 * is no ambiguity in the attributes of the format returned by match.
 * <p>
 * To continue with the audio example, if the sample rate field of an audio
 * format contains the range 8000 to 48000, and the sample rate field of the
 * another audio format contains the range 4000 to 22100, then match could
 * return an object that supports a single sample rate of 16000.
 * <p>
 * 
 * Another example:
 * <p>
 * 
 * Connectables advertise which formats they can use for communication with the
 * <b>listFormats</b> method. Connectables use <b>setFormat</b> to determine
 * which specific format they will use to communicate. The match method is used
 * to obtain a format that two connectables can use to communicate.
 * <p>
 * 
 * If Format A is obtained from the listFormats method of an inputConnectable,
 * and Format B is obtained from the listFormats method of an outputConnectable,
 * and if Format C, obtained from A.match(B), is not null, C can successfully be
 * used as the argument to setFormat on both connectables. Format C specifies
 * the format that the two connectables will use to communicate.
 * <p>
 * 
 * <b>Note:</b> Match is not commutative therefore if A.match(B) fails,
 * B.match(A) should be tried.
 * 
 * @see Connectable
 * @version 1.24, 98/03/28.
 */
public abstract interface Format {

    /**
     * <b>Tentative.</b><br>
     * Return a Format compatible with the argument. If this format cannot find
     * a format compatible object null is returned.
     * <p>
     * 
     * <b>Note:</b> match is not commutative.
     * 
     * @param other
     *            The format to attempt to match to.
     */
    public abstract Format match(Format other);
}
