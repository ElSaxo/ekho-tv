/*
 * @(#)MediaSelectSucceededEvent.java	1.10 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.media;

import javax.media.Controller;
import javax.tv.locator.Locator;

/**
 * A <code>MediaSelectSucceededEvent</code> notifies a
 * <code>MediaSelectListener</code> that a selection operation succeeded.
 * 
 * @see MediaSelectListener
 **/
public class MediaSelectSucceededEvent extends MediaSelectEvent {

    /**
     * Creates a new <code>MediaSelectSucceededEvent</code>.
     * 
     * @param source
     *            The <code>Controller</code> that generated this event.
     * 
     * @param selection
     *            The <code>Locator</code> instances on which selection
     *            occurred.
     */
    public MediaSelectSucceededEvent(Controller source, Locator[] selection) {
	super(source, selection);
    }
}
