/*
 * @(#)NormalContentEvent.java	1.16 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.selection;

/**
 * <code>NormalContentEvent</code> is generated to indicate that the normal
 * content of a service is being presented. This event will be generated in two
 * situations:
 * <ul>
 * 
 * <li>
 * At the end of a successful service selection operation, this event will be
 * generated as long as all of the service components being presented are part
 * of the normal content of the service. The generation of this event in this
 * situation signals a change in state of the service context from the
 * <em>presentation pending</em> state to the <em>presenting</em> state. If even
 * one of the service components being presented is alternative content (as
 * defined in <code>AlternativeContentEvent</code>) then that event will be
 * generated instead.</li>
 * 
 * <li>
 * During the presentation of a service, this event will be generated if
 * alternative content was being presented and all of that alternative content
 * is replaced by content which is a normal part of the service. An example of
 * this is when an end user completes a pay per view dialog the video
 * advertising a service is replaced by the service itself.</li>
 * </ul>
 * 
 * @see AlternativeContentEvent
 */

public class NormalContentEvent extends PresentationChangedEvent {
    /**
     * Constructs the event.
     * 
     * @param source
     *            The <code>ServiceContext</code> that generated the event.
     */
    public NormalContentEvent(ServiceContext source) {
	super(source);
    }
}
