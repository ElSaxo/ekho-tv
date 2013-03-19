/*
 * @(#)ServiceContextDestroyedEvent.java	1.11 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.selection;

/**
 * <code>ServiceContextDestroyedEvent</code> is generated when a
 * <code>ServiceContext</code> is destroyed via its <code>destroy()</code>
 * method.
 * 
 * @see ServiceContext#destroy
 */

public class ServiceContextDestroyedEvent extends ServiceContextEvent {

    /**
     * Constructs the event.
     * 
     * @param source
     *            The <code>ServiceContext</code> that was destroyed.
     */

    public ServiceContextDestroyedEvent(ServiceContext source) {
	super(source);
    }
}
