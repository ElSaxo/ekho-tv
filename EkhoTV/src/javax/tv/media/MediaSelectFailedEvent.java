/*
 * @(#)MediaSelectFailedEvent.java	1.14 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.media;

import javax.media.Controller;
import javax.tv.locator.Locator;

/**
 * <code>MediaSelectFailedEvent</code> notifies a
 * <code>MediaSelectListener</code> that a selection operation failed.
 * 
 * @see MediaSelectListener
 **/
public class MediaSelectFailedEvent extends MediaSelectEvent {

    /**
     * Creates a new <code>MediaSelectFailedEvent</code>.
     * 
     * @param source
     *            The Controller that generated this event.
     * 
     * @param selection
     *            The <code>Locator</code>instances on which selection was
     *            attempted.
     */
    public MediaSelectFailedEvent(Controller source, Locator[] selection) {
	super(source, selection);
    }
}
