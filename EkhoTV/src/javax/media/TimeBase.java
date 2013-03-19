/*
 * @(#)TimeBase.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * A <code>TimeBase</code> is a constantly ticking source of time, much like a
 * crystal.
 * <p>
 * 
 * Unlike a <code>Clock</code>, a <code>TimeBase</code> cannot be temporally
 * transformed, reset, or stopped.
 * 
 * @see Clock
 * @version 1.15, 98/03/28.
 */
public interface TimeBase {

    /**
     * Get the current time of this <code>TimeBase</code>.
     * 
     * @return the current <code>TimeBase</code> time.
     */
    public Time getTime();

    /**
     * Get the current time of the <code>TimeBase</code> specified in
     * nanoseconds.
     * 
     * @return the current <code>TimeBase</code> time in nanoseocnds.
     */
    public long getNanoseconds();

}
