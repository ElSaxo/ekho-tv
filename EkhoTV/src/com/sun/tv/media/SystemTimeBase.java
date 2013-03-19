/*
 * @(#)SystemTimeBase.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import javax.media.Time;
import javax.media.TimeBase;

/**
 * SystemTimeBase is the implementation of the default <CODE>TimeBase</CODE>
 * that ships with JavaMedia.
 * 
 * @see TimeBasemake doc.
 * 
 * @version 1.11, 98/03/28.
 * 
 */
final public class SystemTimeBase implements TimeBase {

    // Pick some offset (start-up time) so the system time won't be
    // so huge. The huge numbers overflow floating point operations
    // in some cases.
    static long offset = System.currentTimeMillis() * 1000000L;

    public Time getTime() {
	return new Time(getNanoseconds());
    }

    public long getNanoseconds() {
	return (System.currentTimeMillis() * 1000000L) - offset;
    }
}
