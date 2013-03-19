/*
 * @(#)ProgramScheduleEvent.java	1.4 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.guide;

import javax.tv.service.SIChangeEvent;
import javax.tv.service.SIChangeType;

/**
 * A <code>ProgramScheduleEvent</code> notifies an
 * <code>ProgramScheduleListener</code> of changes to program events detected in
 * a <code>ProgramSchedule</code>. Specifically, this event signals the
 * addition, removal, or modification of a <code>ProgramEvent</code> in a
 * <code>ProgramSchedule</code>, or a change to the <code>ProgramEvent</code>
 * that is current.
 * <p>
 * 
 * The class <code>ProgramScheduleChangeType</code> defines the kinds of changes
 * reported by <code>ProgramScheduleEvent</code>. A
 * <code>ProgramScheduleChangeType</code> of <code>CURRENT_PROGRAM_EVENT</code>
 * indicates that the current <code>ProgramEvent</code> of a
 * <code>ProgramSchedule</code> has changed in identity.
 * 
 * @see ProgramScheduleListener
 * @see ProgramScheduleChangeType
 */
public class ProgramScheduleEvent extends SIChangeEvent {

    /**
     * Constructs a <code>ProgramScheduleEvent</code>.
     * 
     * @param schedule
     *            The schedule in which the change occurred.
     * 
     * @param type
     *            The type of change that occurred.
     * 
     * @param e
     *            The <code>ProgramEvent</code> that changed.
     */
    public ProgramScheduleEvent(ProgramSchedule schedule, SIChangeType type,
	    ProgramEvent e) {
	super(schedule, type, e);
    }

    /**
     * Reports the <code>ProgramSchedule</code> that generated the event. The
     * object returned will be identical to the object returned by the inherited
     * <code>EventObject.getSource()</code> method.
     * 
     * @return The <code>ProgramSchedule</code> that generated the event.
     * 
     * @see java.util.EventObject#getSource
     **/
    public ProgramSchedule getProgramSchedule() {
	return (ProgramSchedule) getSource();
    }

    /**
     * Reports the <code>ProgramEvent</code> that changed. If the
     * <code>ProgramScheduleChangeType</code> is
     * <code>CURRENT_PROGRAM_EVENT</code>, the <code>ProgramEvent</code> that
     * became current will be returned. The object returned will be identical to
     * the object returned by inherited <code>SIEvent.getSIElement</code>
     * method.
     * 
     * @return The <code>ProgramEvent</code> that changed.
     * 
     * @see javax.tv.service.SIChangeEvent#getSIElement
     */
    public ProgramEvent getProgramEvent() {
	return (ProgramEvent) getSIElement();
    }
}
