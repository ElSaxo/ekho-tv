/*
 * @(#)ProgramScheduleChangeType.java	1.6 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.guide;

import javax.tv.service.SIChangeType;

/**
 * This class represents types of changes to program schedules.
 * 
 * @see ProgramScheduleEvent
 * @see ProgramSchedule
 */
public class ProgramScheduleChangeType extends SIChangeType {

    /**
     * Creates an <code>ProgramScheduleChangeType</code> object.
     * 
     * @param name
     *            The string name of this type (e.g. "CURRENT_PROGRAM_EVENT").
     */
    protected ProgramScheduleChangeType(String name) {
	super(name);
    }

    /**
     * Provides the string name of the type. For the type objects defined in
     * this class, the string name will be identical to the class variable name.
     * 
     * @return The string name of the type.
     */
    public String toString() {
	return super.toString();
    }

    /**
     * <code>ProgramScheduleChangeType</code> indicating that the current
     * program event has changed.
     */
    public static final ProgramScheduleChangeType CURRENT_PROGRAM_EVENT;

    static {
	CURRENT_PROGRAM_EVENT = new ProgramScheduleChangeType(
		"CURRENT_PROGRAM_EVENT");
    }
}
