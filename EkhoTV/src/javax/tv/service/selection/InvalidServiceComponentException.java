/*
 * @(#)InvalidServiceComponentException.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.selection;

import javax.tv.locator.Locator;

/**
 * This exception is thrown when one or more service components are not valid
 * for usage in a particular context. If multiple service components are
 * simultaneously invalid, this exception reports one of them.
 */
public class InvalidServiceComponentException extends ServiceContextException {

    private Locator component = null;

    /**
     * Constructs an <code>InvalidServiceComponentException</code> with no
     * detail message.
     * 
     * @param component
     *            A locator indicating the offending service component.
     */
    public InvalidServiceComponentException(Locator component) {
	super();
	this.component = component;
    }

    /**
     * 
     * Constructs an <code>InvalidServiceComponentException</code> with the
     * specified detail message.
     * 
     * @param component
     *            A locator indicating the offending service component.
     * 
     * @param reason
     *            The reason why this component is invalid.
     */
    public InvalidServiceComponentException(Locator component, String reason) {
	super(reason);
	this.component = component;
    }

    /**
     * Reports the offending service components.
     * 
     * @return A locator indicating the service component that caused the
     *         exception.
     */
    public Locator getInvalidServiceComponent() {
	return component;
    }
}
