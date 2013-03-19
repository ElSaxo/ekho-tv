/*
 * @(#)ServiceContextEvent.java	1.14 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.selection;

/**
 * The parent class for service context events.
 */

public class ServiceContextEvent extends java.util.EventObject {

    /**
     * Constructs the event.
     * 
     * @param source
     *            The <code>ServiceContext</code> that generated the event.
     */
    public ServiceContextEvent(ServiceContext source) {
	super(source);
    }

    /**
     * Reports the <code>ServiceContext</code> that generated the event.
     * 
     * @return The <code>ServiceContext</code> that generated the event.
     */
    public ServiceContext getServiceContext() {
	return (ServiceContext) getSource();
    }
}
