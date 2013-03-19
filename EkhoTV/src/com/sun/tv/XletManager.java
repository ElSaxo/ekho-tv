/*
 * @(#)XletManager.java	1.21 08/09/15
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 */
package com.sun.tv;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;

import javax.tv.service.selection.ServiceContext;
import javax.tv.xlet.Xlet;
import javax.tv.xlet.XletContext;

/********************* XletManager class ************************************/
public class XletManager implements AppSignalEventListener {

    // ServiceContext, XletManager
    private static Hashtable xletManagers = new Hashtable();
    private Hashtable stateTable = new Hashtable();
    private Hashtable proxyTable = new Hashtable();
    private Hashtable idTable = new Hashtable();

    // The carousel imitation directory to look for all service-tied xlets.
    private String carouselDir = null;

    /**
     * XletManager constructor. Should wait for signal.
     */
    public XletManager() {
	carouselDir = (String) AccessController
		.doPrivileged(new PrivilegedAction() {
		    public Object run() {
			return System.getProperty("javatv.carouseldir", null);
		    }
		});
    }

    public static XletManager createInstance(ServiceContext sc) {
	XletManager manager;
	synchronized (xletManagers) {
	    manager = (XletManager) xletManagers.get(sc);
	    if (manager == null) {
		manager = new XletManager();
		xletManagers.put(sc, manager);
	    }
	}
	return manager;
    }

    /**
     * This is called when XletManager receive a an Xlet related signal. It
     * should look at the signal parameter to determine if the Xlet is
     * 
     * 1. to be started. Check if it's autostart or not. If it's autostart, call
     * prepareXlet(). 2. present. call prepareXlet(). 3. to be paused. Call
     * pauseXlet(). 4. to be destroyed. Call destroyXlet().
     * 
     * Note this is NOT directly calling Xlet's method. Instead private methods
     * are called to handle each case.
     */
    public Xlet signalReceived(AppSignalEvent sigEvent) {

	String id = sigEvent.getApplicationIdentifier();
	Xlet xlet = null;

	switch (sigEvent.getControlCode()) {
	case AppSignalEvent.AUTOSTART:
	    xlet = prepareXlet(sigEvent);
	    if (xlet != null) {
		initXlet(id);
		startXlet(id);
	    }
	    break;
	case AppSignalEvent.PRESENT:
	    xlet = prepareXlet(sigEvent);
	    break;

	case AppSignalEvent.PAUSE:
	    pauseXlet(id);
	    break;

	case AppSignalEvent.DESTROY:
	    destroyXlet(id, false);
	    cleanupXlet(id);
	    break;

	case AppSignalEvent.KILL:
	    destroyXlet(id, true);
	    cleanupXlet(id);
	    break;
	}
	return xlet;
    }

    /**
     * Preparation for starting an xlet, this includes:
     * 
     * 1. create a classloader for this xlet. 2. read and form the xlet
     * class(es) from this loader. 3. create a thread group for the xlet 4.
     * create an XletContext for the xlet 5. start a new thread to perform any
     * xlet lifecycle related actions (start, stop, etc) and wait for such a
     * request. 6. Create reqHolder and resultHolder object for these actions.
     * This is to set up producer/consumer scenario. 7. put all these info in an
     * XletProxy and stick it into the xletProxyTable.
     * 
     * 8. put Xlet's name and Xlet in a name table. Put them all in XletProxy.
     * 
     * @param signal
     *            event
     * @return an instance of Xlet, or null if it already exist in the runtime.
     */
    private Xlet prepareXlet(AppSignalEvent sigEvent) {

	if (sigEvent == null) {
	    return null;
	}
	String id = sigEvent.getApplicationIdentifier();
	Xlet xlet = (Xlet) idTable.get(id);
	if (xlet != null) {
	    System.err.println("xlet instance with the id " + id
		    + " already loaded and instanciated");
	    return xlet;
	}

	try {
	    XletLoader xletLoader = new XletLoader(carouselDir,
		    sigEvent.getBaseDirectories());
	    Class xletClass = xletLoader.loadClass(sigEvent.getClassName()
		    .trim());
	    Object obj = xletClass.newInstance();
	    if (!(obj instanceof Xlet)) {
		System.err.println(sigEvent.getClassName()
			+ " not an instance of javax.tv.xlet.Xlet");
		return null;
	    }

	    Xlet myXletIns = (Xlet) obj;

	    XletContextImpl ctx = new XletContextImpl(this, myXletIns,
		    sigEvent.getServiceContext(), sigEvent.getArgs());

	    ThreadGroup tg = new ThreadGroup("Xlet lifecycle thread for " + id);

	    /*
	     * The next section creates objects needed for the action thread for
	     * each Xlet. The action thread is responsible for watching for
	     * request and executing required actions related to Xlet's
	     * lifecycle.
	     */
	    Holder reqHolder = new Holder();
	    Holder resultHolder = new Holder();
	    XletRunnable xletRun = new XletRunnable(reqHolder, resultHolder);
	    Thread xletActionThread = new Thread(tg, (xletRun));
	    xletActionThread.start();

	    // create a place to hold all of the above Xlet related objects.
	    XletProxy xletProxy = new XletProxy(xletLoader, this, ctx, tg,
		    xletRun, reqHolder, resultHolder, myXletIns);

	    // Put a reference of the Xlet and its related structures
	    // in a hashtable
	    proxyTable.put(myXletIns, xletProxy);

	    idTable.put(sigEvent.getApplicationIdentifier(), myXletIns);

	    XletState myXletState = new XletState();
	    myXletState.setState(XletState.LOADED);
	    stateTable.put(myXletIns, myXletState);

	    return myXletIns;

	} catch (Exception ex) {
	    System.err.println("XletException: " + ex);
	}
	return null;
    }

    /**
     * XletAction
     * 
     * Performe one of the Xlet lifecycle actions through the designated exec
     * thread that's allocated for this Xlet.
     * 
     * @param Xlet
     *            -- used to get the XletProxy, which has other related objects.
     *            This should never be null.
     * @param Request
     *            -- the request whose 'execReq' method contains the real
     *            actions. This should never be null.
     * @return Result of the action -- usually has just the field successful set
     *         to true or false.
     */
    private Result XletAction(Xlet myXlet, Request req) {
	XletProxy myProxy = (XletProxy) proxyTable.get(myXlet);
	Holder reqHolder = myProxy.getReqHolder();
	Holder resultHolder = myProxy.getResultHolder();

	reqHolder.put(req);
	// return (Result)resultHolder.get(); // to implement this property,
	// there has to be more than one thread
	// to pick up the reqHolder's entries.
	// TODO: Introduce Worker thread pool?
	return null;
    }

    /**
     * Initialize Xlet
     * 
     * Call Xlet's init method and update the Xlet's state record. No-op if xlet
     * is in a state anything other than LOADED.
     * 
     * This is done through the action thread by putting a request in a commonly
     * watched holder. The action thread will pick up the request and execute
     * it.
     * 
     * @param xletId
     *            - the identification of the xlet that will be initialized.
     */
    private void initXlet(String xletId) {
	Xlet myXlet = (Xlet) idTable.get(xletId);
	if (myXlet == null) {
	    // this xlet never loaded
	    return;
	}
	XletContext myXletContext = ((XletProxy) proxyTable.get(myXlet))
		.getXletContext();
	int state = ((XletState) stateTable.get(myXlet)).getState();
	if (XletState.LOADED == state) {
	    Request req = new Request(myXlet, myXletContext);
	    Result result = XletAction(myXlet, req);

	    changeXletState(myXlet, XletState.PAUSED);
	}
    }

    /**
     * Start Xlet
     * 
     * Call Xlet's start method and update the Xlet's state record if xlet is in
     * the PAUSED state.
     * 
     * This is done through the action thread by putting a request in a commonly
     * watched holder. The action thread will pick up the request and execute
     * it.
     * 
     * @param xletId
     *            -- identification of the xlet to start.
     */
    private void startXlet(String xletId) {

	Xlet myXlet = (Xlet) idTable.get(xletId);
	if (myXlet == null) {
	    // this xlet never loaded
	    return;
	}
	int state = ((XletState) stateTable.get(myXlet)).getState();
	if (XletState.PAUSED == state) {
	    Request req = new Request(myXlet, Request.START);
	    Result result = XletAction(myXlet, req);

	    changeXletState(myXlet, XletState.ACTIVE);
	}
    }

    /**
     * pause Xlet
     * 
     * Call Xlet's pause method and update the Xlet's state record if xlet is in
     * the ACTIVE state.
     * 
     * This is done through the action thread by putting a request in a commonly
     * watched holder. The action thread will pick up the request and execute
     * it.
     * 
     * @param xletId
     *            -- identification of the xlet to pause.
     */
    private void pauseXlet(String xletId) {

	Xlet myXlet = (Xlet) idTable.get(xletId);
	if (myXlet == null) {
	    // this xlet never loaded
	    return;
	}
	int state = ((XletState) stateTable.get(myXlet)).getState();
	if (XletState.ACTIVE == state) {
	    Request req = new Request(myXlet, Request.PAUSE);
	    Result result = XletAction(myXlet, req);

	    changeXletState(myXlet, XletState.PAUSED);
	}
    }

    /**
     * destroy Xlet
     * 
     * Call Xlet's destroy method and update the Xlet's state record unless the
     * xlet is already in the DESTROYED state.
     * 
     * This is done through the action thread by putting a request in a commonly
     * watched holder. The action thread will pick up the request and execute
     * it.
     * 
     * @param Xlet
     *            -- identification of the xlet to destroy.
     */
    private void destroyXlet(String xletId, boolean unconditional) {

	Xlet myXlet = (Xlet) idTable.get(xletId);
	if (myXlet == null) {
	    // this xlet never loaded
	    return;
	}
	int state = ((XletState) stateTable.get(myXlet)).getState();

	if (XletState.DESTROYED != state) {
	    Request req = new Request(myXlet, unconditional);
	    Result result = XletAction(myXlet, req);

	    /**
	     ** if (result == null) return; // xlet doesn't exist anymore, die
	     * silently.
	     ** 
	     ** // If not successful (=XletChangeException is thrown) if (
	     * !result.getSuccess() ) { // deal with xlet state change exception
	     * if ( !unconditional) { try { // Here we should allow Xlet more
	     * time to recover, then // we should call destroy again.
	     * Thread.sleep(10); } catch (InterruptedException ie) { ; } //
	     * really destroy this time destroyXlet(myXlet, true); } else {
	     * changeXletState(myXlet, XletState.DESTROYED); }
	     ** 
	     ** } else { changeXletState(myXlet, XletState.DESTROYED); }
	     **/
	    changeXletState(myXlet, XletState.DESTROYED);
	}
    }

    /**
     * Cleanup Xlet in XletManager.
     * 
     * This will delete all Xlet related data from various tables in
     * XletManager, therefore xletLoader will be dereferenced and xlet classes
     * unloaded.
     * 
     */
    public void cleanupXlet(final String xletId) {

	final Xlet myXlet = (Xlet) idTable.get(xletId);
	if (myXlet == null) {
	    // this xlet never loaded
	    return;
	}
	int state = ((XletState) stateTable.get(myXlet)).getState();
	if (XletState.DESTROYED != state) {
	    throw new RuntimeException(
		    "Cannot dereference an xlet that is not destroyed");
	}

	// Spawn a new thread that waits for all requests to complete, then to
	// stop the request thread and remove reference of this xlet from all
	// Hashtable.
	new Thread(new Runnable() {
	    public void run() {
		XletProxy myProxy = (XletProxy) proxyTable.get(myXlet);
		Holder reqHolder = myProxy.getReqHolder();

		// If this Xlet exist in our table, remove it and its
		// classloader. This will clear any reference to its
		// classloader.
		proxyTable.remove(myXlet);

		// This method waits until all pending requests are picked up.
		reqHolder.waitTillQueueIsEmpty();

		// Stop the action thread to Xlet.

		myProxy.getActionThread().shouldRun = false;

		// To get rid of myXlet and its key XletID from the idtable
		idTable.remove(xletId);

	    }
	}).start();

    }

    /**
     * Change Xlet state in XletManager
     * 
     * Change the xlet's state in XletManager's state maintainance table.
     */
    public void changeXletState(Xlet xlet, int newState) {
	synchronized (xlet) {
	    if (stateTable.containsKey(xlet)) {

		XletState oldXletState = (XletState) stateTable.get(xlet);
		int oldState = oldXletState.getState();

		if (oldState == XletState.DESTROYED) {
		    return;
		}
		oldXletState.setState(newState);
	    }
	}
    }

    public XletState getXletState(Xlet xlet) {
	return (XletState) stateTable.get(xlet);
    }
}
