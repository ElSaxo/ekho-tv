/*
 * @(#)XletContextImpl.java	1.15 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv;

import javax.tv.service.selection.ServiceContext;
import javax.tv.xlet.Xlet;
import javax.tv.xlet.XletContext;

import nl.ekholabs.ekhotv.activity.EkhoTVProxyActivity;
import android.content.Context;

/**
 * XletContext is used for xlet to communicate back to XletManager. We save a
 * reference to xletManager as a means to get properties, and notify XletManager
 * when calls are made to the lifecycle functions
 * 
 * @version 99/07/08 @(#)XletContextImpl.java 1.6
 */
public class XletContextImpl implements XletContext {

    public static final String CONTAINER = "javax.tv.xlet.container";
    public static final String SERVICE_CONTEXT = "javax.tv.xlet.service_context";
    public static final String ROOT_CONTAINER = "javax.tv.xlet.root_container";

    private XletManager xletManager = null;
    private XletContainer xletContainer = null;
    private Xlet xlet = null;
    private String[] args = null;
    private ServiceContext context = null;

    public XletContextImpl(XletManager xletMgr, Xlet xlet,
	    ServiceContext context, String args[]) {
	this.xletManager = xletMgr;
	this.xlet = xlet;
	this.args = args;
	this.context = context;
    }

    /*
     * Accessor methods to populate properties database
     */
    private DisplayManager getDisplayMgr() {
	EkhoTVProxyActivity proxy = EkhoTVProxyActivity.getInstance();
	Context context = proxy.getActivity().getApplicationContext();

	return DisplayManager.createInstance(context);
    }

    /**
     * Provides an Xlet with a mechanism to retrieve named properties from the
     * XletContext.
     * 
     * @parameter key The name of the property
     * @return A reference to an object representing the property.
     *         <code>null</code> is returned if no value is available for key.
     */

    public Object getXletProperty(String prop) {
	if (prop == null) {
	    throw new NullPointerException("String is null");
	}

	if (ARGS.equals(prop)) {
	    return args;
	} else if (CONTAINER.equals(prop)) {
	    if (xlet != null && xletManager != null) {
		XletState state = xletManager.getXletState(xlet);
		if (state.getState() == XletState.DESTROYED)
		    return null;
	    }

	    if (xletContainer == null) {
		xletContainer = getDisplayMgr().createXletContainer(null);
	    }
	    return xletContainer;

	} else if (ROOT_CONTAINER.equals(prop)) {
	    return getDisplayMgr().getRootFrame();

	} else if (SERVICE_CONTEXT.equals(prop)) {
	    if (xlet != null && xletManager != null) {
		XletState state = xletManager.getXletState(xlet);
		if (state.getState() == XletState.DESTROYED)
		    return null;
	    }
	    return context;
	}
	return null;
    }

    /**
     * Notifies the manager that the Xlet does not want to be active and has
     * entered the <i>Paused</i> state. Invoking this method will have no effect
     * if the Xlet is destroyed, or if it has not yet been started.
     * <p>
     * 
     * If an Xlet calls <code>notifyPaused()</code>, in the future it may
     * receive an <i>Xlet.startXlet()</i> call to request it to become active,
     * or an <i>Xlet.destroyXlet()</i> call to request it to destroy itself.
     * 
     */

    public void notifyPaused() {
	xletManager.changeXletState(xlet, XletState.PAUSED);
    }

    /**
     * Provides the Xlet with a mechanism to indicate that it is interested in
     * entering the <i>Active</i> state. Calls to this method can be used by an
     * application manager to determine which Xlets to move to <i>Active</i>
     * state.
     */

    public void resumeRequest() {
	xletManager.changeXletState(xlet, XletState.ACTIVE);
    }

    /**
     * 
     * Used by an application to notify its manager that it has entered into the
     * <i>Destroyed</i> state. The application manager will not call the Xlet's
     * <code>destroy</code> method, and all resources held by the Xlet will be
     * considered eligible for reclamation. Before calling this method, the Xlet
     * must have performed the same operations (clean up, releasing of resources
     * etc.) it would have if the <code>Xlet.destroyXlet()</code> had been
     * called.
     */

    public void notifyDestroyed() {
	xletManager.changeXletState(xlet, XletState.DESTROYED);

	getDisplayMgr().destroyXletContainer(xletContainer);

	try {
	    if (context != null) {
		context.destroy();
	    }
	} catch (Exception e) {
	    ;
	}
	context = null;
    }
}
