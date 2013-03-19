/*
 * @(#)MediaSelectEvent.java	1.14 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.media;

import javax.media.Controller;
import javax.tv.locator.Locator;

/**
 * <code>MediaSelectEvent</code> is the base class of events sent to
 * <code>MediaSelectListener</code> instances.
 * 
 * @see MediaSelectListener
 **/
public abstract class MediaSelectEvent extends java.util.EventObject {

    private Locator selection[];

    /**
     * Creates a new <code>MediaSelectEvent</code>.
     * 
     * @param controller
     *            The Controller that generated this event.
     * @param selection
     *            The <code>Locator</code> instances on which selection was
     *            attempted.
     */
    public MediaSelectEvent(Controller controller, Locator[] selection) {
	super(controller);
	this.selection = selection;
    }

    /**
     * Reports the Controller that generated this event.
     * 
     * @return The Controller that generated this event.
     */
    public Controller getController() {
	return (Controller) getSource();
    }

    /**
     * Reports the selection that caused this event.
     * 
     * @return The selection that caused this event.
     */
    public Locator[] getSelection() {
	return selection;
    }
}
