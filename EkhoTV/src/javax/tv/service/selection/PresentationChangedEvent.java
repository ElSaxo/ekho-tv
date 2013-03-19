/*
 * @(#)PresentationChangedEvent.java	1.16 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.selection;

/**
 * <code>PresentationChangedEvent</code> indicates that the content being
 * presented in the <code>ServiceContext</code> has changed.
 * <code>PresentationChangedEvent</code> is the parent class of events
 * indicating dynamic changes to the presentation of a service due to
 * interaction with the CA system. It is generated when neither
 * <code>AlternativeContentEvent</code> nor <code>NormalContentEvent</code> are
 * applicable.
 * <p>
 * 
 * Applications may determine the nature of the new content by querying the
 * current <code>ServiceContentHandler</code> instances of the
 * <code>ServiceContext</code>.
 * 
 * @see AlternativeContentEvent
 * @see NormalContentEvent
 * @see ServiceContentHandler#getServiceContentLocators
 */
public class PresentationChangedEvent extends ServiceContextEvent {
    /**
     * Constructs the event.
     * 
     * @param source
     *            The <code>ServiceContext</code> that generated the event.
     */
    public PresentationChangedEvent(ServiceContext source) {
	super(source);
    }
}
