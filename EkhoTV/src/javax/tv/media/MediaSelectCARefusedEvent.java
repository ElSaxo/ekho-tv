/*
 * @(#)MediaSelectCARefusedEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.media;

import javax.media.Controller;
import javax.tv.locator.Locator;

/**
 * <code>MediaSelectCARefusedEvent</code> is generated when a media select
 * operation fails due to lack of CA authorization.
 **/
public class MediaSelectCARefusedEvent extends MediaSelectFailedEvent {

    /**
     * Constructs the <code>MediaSelectCARefusedEvent</code>.
     * 
     * @param source
     *            The <code>Controller</code> that generated this event.
     * 
     * @param selection
     *            The <code>Locator</code> instances on which selection failed.
     **/
    public MediaSelectCARefusedEvent(Controller source, Locator[] selection) {
	super(source, selection);
    }
}
