/*
 * @(#)ProgramScheduleListener.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.guide;

import javax.tv.service.SIChangeListener;

/**
 * This interface is implemented by applications wishing to receive notification
 * of changes to <code>ProgramSchedule</code> data.
 */
public interface ProgramScheduleListener extends SIChangeListener {

    /**
     * Notifies the <code>ProgramScheduleListener</code> of a change to a
     * <code>ProgramSchedule</code>.
     * 
     * @param event
     *            A <code>ProgramScheduleEvent</code> describing what changed
     *            and how.
     */
    public abstract void notifyChange(ProgramScheduleEvent event);
}
