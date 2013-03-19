/*
 * @(#)ServiceContextImpl.java	1.58 08/11/18
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 */
package com.sun.tv;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.media.CachingControlEvent;
import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DurationUpdateEvent;
import javax.media.EndOfMediaEvent;
import javax.media.MediaTimeSetEvent;
import javax.media.Player;
import javax.media.PrefetchCompleteEvent;
import javax.media.RateChangeEvent;
import javax.media.RealizeCompleteEvent;
import javax.media.StartEvent;
import javax.media.StopTimeChangeEvent;
import javax.media.Time;
import javax.media.TransitionEvent;
import javax.tv.locator.InvalidLocatorException;
import javax.tv.locator.Locator;
import javax.tv.locator.LocatorFactory;
import javax.tv.locator.MalformedLocatorException;
import javax.tv.service.SIElement;
import javax.tv.service.SIManager;
import javax.tv.service.Service;
import javax.tv.service.navigation.ServiceComponent;
import javax.tv.service.selection.AlternativeContentEvent;
import javax.tv.service.selection.InvalidServiceComponentException;
import javax.tv.service.selection.NormalContentEvent;
import javax.tv.service.selection.PresentationTerminatedEvent;
import javax.tv.service.selection.SelectPermission;
import javax.tv.service.selection.SelectionFailedEvent;
import javax.tv.service.selection.ServiceContentHandler;
import javax.tv.service.selection.ServiceContext;
import javax.tv.service.selection.ServiceContextDestroyedEvent;
import javax.tv.service.selection.ServiceContextEvent;
import javax.tv.service.selection.ServiceContextListener;
import javax.tv.service.selection.ServiceContextPermission;
import javax.tv.xlet.Xlet;

import nl.ekholabs.ekhotv.activity.EkhoTVProxyActivity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.sun.tv.service.selection.InternalEvent;
import com.sun.tv.service.selection.NormalContentCausedInternalEvent;
import com.sun.tv.service.selection.SelectionFailedCausedEvent;
import com.sun.tv.si.ServiceComponentDataImpl;
import com.sun.tv.si.ServiceComponentImpl;
import com.sun.tv.si.ServiceImpl;

/**
 * 
 * A <code>ServiceContext</code> represents an environment in which services are
 * presented in a broadcast receiver. Applications may use
 * <code>ServiceContext</code> objects to select new services to be presented.
 * Content associated with a selected service is presented by one or more
 * <code>ServiceContentHandler</code> objects managed by the
 * <code>ServiceContext</code>.
 * <p>
 * 
 * <code>ServiceContext</code>can exist in four states - <em>presenting</em>,
 * <em>not presenting</em>, <em>presentation
 * pending</em> and <em>destroyed</em>. The initial state is
 * <em>not presenting</em>.
 * <p>
 * 
 * The <code>select</code> method can be called from any state but
 * <em>destroyed</em>. Assuming no exception is thrown, the service context then
 * enters the <em>presentation pending</em> state. No event is generated on this
 * state transition. If a call to <code>select</code> completes successfully,
 * either a <code>NormalContentEvent</code> or an
 * <code>AlternativeContentEvent</code> will be generated and the service
 * context moves into the <em>presenting</em> state. If the service selection
 * fails, a <code>SelectionFailedEvent</code> will be generated. If the state
 * before the <code>select</code> call was <em>not presenting</em>, it will
 * return to that state and a <code>PresentationTerminatedEvent</code>
 * generated. If the state before the <code>select</code>call was
 * <em>presenting</em>, it will attempt to return to that previous state which
 * can result in a <code>NormalContentEvent</code> or
 * <code>AlternativeContentEvent</code> if this is possible or a
 * <code>PresentationTerminatedEvent</code> if it is not possible.
 * <p>
 * 
 * The <em>not presenting</em> state is entered due to service presentation
 * being stopped which is reported by the
 * <code>PresentationTerminatedEvent</code>. The stopping of service
 * presentation can be initiated by an application calling the <code>stop</code>
 * method or because some change in the environment makes continued presentation
 * impossible.
 * <p>
 * 
 * The <em>destroyed</em> state is entered by calling the <code>destroy</code>
 * method, and is signalled by the <code>ServiceContextDestroyedEvent</code>.
 * Once this state is entered, the service context can no longer be used for any
 * purpose. A destroyed service context will be eligible for garbage collection
 * once all references to it by application(s) have been removed.
 * <p>
 * 
 * Note that the ability to select which service is presented does not imply any
 * ownership of the resources used for the presentation.
 * <p>
 * 
 * Applications may also use this interface to register for events associated
 * with service context state changes.
 * <p>
 * 
 * @see javax.tv.service.Service
 * @see ServiceContentHandler
 * 
 * @see NormalContentEvent
 * @see AlternativeContentEvent
 * @see SelectionFailedEvent
 * @see PresentationTerminatedEvent
 * @see ServiceContextDestroyedEvent
 * @see ServiceContextListener
 */
public class ServiceContextImpl implements ServiceContext, ControllerListener {

    private Locator serviceLocator = null;
    private Vector componentLocators = new Vector();
    private Hashtable serviceHandlers = new Hashtable();
    private Vector serviceListeners = new Vector();
    private static final int STATE_PRESENTING = 0;
    private static final int STATE_NOT_PRESENTING = 1;
    private static final int STATE_PRESENTATION_PENDING = 2;
    private static final int STATE_DESTROYED = 3;
    private int state = STATE_NOT_PRESENTING;
    private static final int PLAY_NORMAL = 0;
    private static final int PLAY_ALTERNATE = 1;
    private static final int PLAY_SELECTION_FAILED = 2;

    public ServiceContextImpl() throws Exception {
    }

    private boolean waitTillRealized(Player player) {
	final int MAX_SLEEPS = 100;

	if (player.getState() == player.Unrealized) {
	    return false;
	}
	if (player.getState() >= player.Realized) {
	    return true;
	}
	if (player.getState() != player.Realizing) {
	    return false;
	}
	for (int i = 0; i < MAX_SLEEPS; i++) {
	    try {
		Thread.sleep(100);
	    } catch (Exception e) {
		;
	    }
	    if (player.getState() >= player.Realized) {
		return true;
	    }
	}
	return false;
    }

    private void PlayService(Locator selection) throws InvalidLocatorException,
	    IllegalStateException {

	Handler player = (Handler) serviceHandlers.get(selection
		.toExternalForm());
	if (player == null) {
	    player = new Handler(selection, this);
	}

	if (player == null || player.validHandler() == false) {
	    throw new InvalidLocatorException(selection);
	}

	serviceHandlers.put(selection.toExternalForm(), player);
	player.addControllerListener(this);
	player.start();

	if (waitTillRealized(player) == false) {
	    state = STATE_PRESENTATION_PENDING;
	}
	this.serviceLocator = selection;
    }

    private void PlayServiceComponent(Service service, Locator selection)
	    throws InvalidLocatorException, IllegalStateException {

	if (service == null) {
	    service = SIManager.createInstance().getService(
		    LocatorImpl.transformToService(selection));
	}

	Handler player = (Handler) serviceHandlers.get(service.getLocator()
		.toExternalForm());
	if (player == null || player.validHandler() == false) {
	    throw new InvalidLocatorException(selection);
	}

	CacheManager siCache = (CacheManager) CacheManager.getSICache();
	ServiceComponentImpl sc = (ServiceComponentImpl) siCache.get(selection);
	if (sc instanceof ServiceComponentDataImpl) {
	    PlayServiceComponentData(selection, (ServiceComponentDataImpl) sc);
	}

	componentLocators.removeElement(selection);
	componentLocators.addElement(selection);
    }

    private void PlayServiceComponentData(Locator locator,
	    ServiceComponentDataImpl serviceComponent)
	    throws InvalidLocatorException, IllegalStateException {

	Log.e("EkhoTV", "AutoStarting " + serviceComponent);

	String name = serviceComponent.getName();

	EkhoTVProxyActivity proxy = EkhoTVProxyActivity.getInstance();
	Context context = proxy.getActivity().getApplicationContext();

	DisplayManager dispManager = DisplayManager.createInstance(context);
	XletManager xletManager = XletManager.createInstance(this);

	AppSignalEvent ase = new AppSignalEvent(dispManager.getRootFrame(),
		AppSignalEvent.AUTOSTART, serviceComponent.getXletPaths(),
		name, name, this, serviceComponent.getXletArgs());

	Xlet xlet = xletManager.signalReceived(ase);
    }

    private void StopService(Locator locator) {
	Handler oldPlayer = (Handler) serviceHandlers.get(locator
		.toExternalForm());
	if (oldPlayer == null) {
	    return;
	}

	EkhoTVProxyActivity proxy = EkhoTVProxyActivity.getInstance();
	Context context = proxy.getActivity().getApplicationContext();

	DisplayManager dispManager = DisplayManager.createInstance(context);
	ViewGroup frame = dispManager.getRootFrame();
	// 6344575: update and clear
	frame.invalidate();

	if (waitTillRealized(oldPlayer) == true) {
	    StopVisualComponent(frame, oldPlayer);

	    oldPlayer.stop();
	    oldPlayer.deallocate();
	    oldPlayer.close();

	    serviceHandlers.remove(locator.toExternalForm());
	}
	this.serviceLocator = null;
    }

    private void StopServiceComponent(Locator locator) {
	CacheManager siCache = (CacheManager) CacheManager.getSICache();
	ServiceComponent sc = (ServiceComponent) siCache.get(locator);
	if (sc instanceof ServiceComponentDataImpl) {
	    StopServiceComponentData(locator, (ServiceComponentDataImpl) sc);
	}
	componentLocators.removeElement(locator);
    }

    private void StopServiceComponentData(Locator locator,
	    ServiceComponentDataImpl serviceComponent) {
	String name = serviceComponent.getName();

	EkhoTVProxyActivity proxy = EkhoTVProxyActivity.getInstance();
	Context context = proxy.getActivity().getApplicationContext();

	DisplayManager dispManager = DisplayManager.createInstance(context);
	XletManager xletManager = XletManager.createInstance(this);

	AppSignalEvent ase = new AppSignalEvent(dispManager.getRootFrame(),
		AppSignalEvent.DESTROY, serviceComponent.getXletPaths(), name,
		name, this, serviceComponent.getXletArgs());

	xletManager.signalReceived(ase);
    }

    private void StopVisualComponent(ViewGroup frame, Player player) {
	if (player.getState() < player.Realized) {
	    return;
	}
	try {
	    View comp = player.getVisualComponent();
	    if (comp != null && comp.getParent() == frame) {
		frame.removeView(comp);
	    }
	} catch (Exception e) {
	    ;
	}
    }

    /**
     * Selects a service to be presented in this <code>ServiceContext</code>. If
     * the <code>ServiceContext</code> is already presenting content, the new
     * selection replaces the content being presented. If the
     * <code>ServiceContext</code> is not presenting, successful conclusion of
     * this method results in the <code>ServiceContext</code> entering the
     * <em>presenting</em> state.
     * <p>
     * 
     * This method is asynchronous and successful completion of the selection is
     * notified by either a <code>NormalContentEvent</code> or a
     * <code>AlternativeContentEvent</code>. If an exception is thrown when this
     * method is called, the state of the service context does not change. In
     * such a case, any service being presented before this method was called
     * will continue to be presented.
     * <p>
     * 
     * If the selection process fails after this method returns, a
     * <code>SelectionFailedEvent</code> will be generated. In this case, the
     * system will attempt to return to presenting the original service (if
     * any). If this is not possible (due, for example, to issues such as tuning
     * or CA) the <code>ServiceContext</code> will enter the <em>not
     * presenting</em> state and a <code>PresentationTerminatedEvent</code> will
     * be generated.
     * <p>
     * 
     * If the <code>ServiceContext</code> is currently presenting a service and
     * components of the current service are also to be presented in the newly
     * selected service, these components will continue to be presented and will
     * not be restarted. If the calling application is not a part of the newly
     * selected service and the application lifecycle policy on the receiver
     * dictates that the calling application be terminated, termination of the
     * application will be affected through the application lifecycle API.
     * <p>
     * 
     * If the provided <code>Service</code> is transport-independent, this
     * method will resolve the <code>Service</code> to a transport-dependent
     * <code>Service</code> before performing the selection. The actual
     * <code>Service</code> selected will be reported through the
     * <code>getService()</code> method.
     * <p>
     * 
     * Successful completion of a select operation using this method provides
     * <code>ServiceContentHandler</code> instances for all components that are
     * signaled as "auto-start" in the selected service. Upon entering the
     * <em>presenting</em> state, these <code>ServiceContentHandler</code>
     * instances will have begun presenting their respective content;
     * <code>ServiceMediaHandler</code> instances will be in the
     * <em>started</em> state.
     * 
     * @param selection
     *            The <code>Service</code> the service to be selected.
     * 
     * @throws SecurityException
     *             If the caller owns this <code>ServiceContext</code> but does
     *             not have
     *             <code>SelectPermission(selection.getLocator(), "own")</code>,
     *             or if the caller does not own this
     *             <code>ServiceContext</code> and does not have
     *             <code>SelectPermission(selection.getLocator(), "*")</code>.
     * 
     * @throws IllegalStateException
     *             If the <code>ServiceContext</code> has been destroyed.
     * 
     * @see NormalContentEvent
     * @see AlternativeContentEvent
     * @see SelectionFailedEvent
     * @see PresentationTerminatedEvent
     * @see javax.tv.service.Service
     * @see ServiceContext#getService
     * @see ServiceContentHandler
     * @see ServiceContext#destroy
     **/
    public void select(Service selection) throws SecurityException {

	if (selection == null) {
	    throw new NullPointerException("service selection is null");
	}

	Locator selectionLocator = selection.getLocator();
	if (selectionLocator == null) {
	    throw new NullPointerException("service locator is null");
	}

	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission(new SelectPermission(selectionLocator, "own"));
	}

	if (state == STATE_DESTROYED) {
	    throw new IllegalStateException(
		    "ServiceContext has been destroyed.");
	}

	if (LocatorImpl.isTIService(selectionLocator)) {
	    Locator[] locs = LocatorImpl.transformLocator(selectionLocator);
	    if (locs != null && locs.length > 0) {
		selectionLocator = locs[0];
	    }
	}

	if (selectionLocator.equals(this.serviceLocator)) {
	    notifyListeners(new NormalContentEvent(this));
	    return; // 6355961

	}

	int reason = 0;
	ServiceImpl service;
	try {
	    service = (ServiceImpl) SIManager.createInstance().getService(
		    selectionLocator);
	    reason = service.getSelectionFailedReason();
	} catch (InvalidLocatorException ex) {
	    ex.printStackTrace(); // shouldn't happen
	}
	if (reason != 0) {
	    selectionLocator = LocatorImpl
		    .transformToAlternate(selectionLocator);
	}

	Locator[] newServiceComponents = getServiceComponentLocators(selection);

	select(selectionLocator, newServiceComponents, null);

    }

    /**
     * Selects content by specifying the parts of a service to be presented. If
     * the <code>ServiceContext</code> is already presenting content, the new
     * selection replaces the content being presented. If the
     * <code>ServiceContext</code> is not presenting, successful conclusion of
     * this method results in the <code>ServiceContext</code> entering the
     * <em>presenting</em> state.
     * <p>
     * 
     * This method is asynchronous and successful completion of the selection is
     * notified by either a <code>NormalContentEvent</code> or a
     * <code>AlternativeContentEvent</code>. If failure of the selection can be
     * determined when this method is called, an exception will be generated and
     * the state of the <code>ServiceContext</code> will not change. In this
     * case, any service being presented before this method was called will
     * continue to be presented.
     * <p>
     * 
     * If failure of the selection is determined after this method returns, a
     * <code>SelectionFailedEvent</code> will be generated. In this case, the
     * implementation of the method will try to return to presenting the
     * original service (if any). If this is not possible (due, for example, to
     * issues such as tuning or CA) the <code>ServiceContext</code> will enter
     * the <em>not presenting</em> state and a
     * <code>PresentationTerminatedEvent</code> will be generated.
     * <p>
     * 
     * If the <code>ServiceContext</code> is currently presenting a service and
     * components of the current service are also to be presented in the newly
     * selected content, these components will continue to be presented and will
     * not be restarted. If the calling application is not a part of the newly
     * selected content and the application lifecycle policy on the receiver
     * dictates that the calling application be terminated, termination of the
     * application will be affected through the application lifecycle API.
     * <p>
     * 
     * Successful completion of a select operation using this method provides
     * <code>ServiceContentHandler</code> instances for all components that are
     * indicated in the <code>components</code> parameter. Upon entering the
     * <em>presenting</em> state, these <code>ServiceContentHandler</code>
     * instances will have begun presenting their respective content;
     * <code>ServiceMediaHandler</code> instances will be in the
     * <em>started</em> state. This method will not provide
     * <code>ServiceContentHandler</code> instances for service components for
     * which a locator is not specified.
     * 
     * @param components
     *            An array of <code>Locator</code> instances representing the
     *            parts of this service to be selected. Each array element must
     *            be a locator to either a <code>ServiceComponent</code> or
     *            content within a service component (such as an Xlet).
     * 
     * @throws InvalidLocatorException
     *             If a <code>Locator</code> provided does not reference a
     *             selectable service component or selectable content within a
     *             service component.
     * 
     * @throws InvalidServiceComponentException
     *             If a specified service component must be presented in
     *             conjunction with another service component not contained in
     *             <code>components</code>, if the specified components are not
     *             all members of the same service, or if the specified set of
     *             components cannot be presented as a coherent whole.
     * 
     * @throws SecurityException
     *             If, for any valid <code>i</code>, the caller owns this
     *             <code>ServiceContext</code> but does not have
     *             <code>SelectPermission(components[i], "own")</code>, or if
     *             the caller does not own this <code>ServiceContext</code> and
     *             does not have
     *             <code>SelectPermission(components[i], "*")</code>.
     * 
     * @throws IllegalStateException
     *             If the <code>ServiceContext</code> has been destroyed.
     * 
     * @see NormalContentEvent
     * @see AlternativeContentEvent
     * @see SelectionFailedEvent
     * @see PresentationTerminatedEvent
     * @see ServiceContentHandler
     * @see javax.tv.service.navigation.ServiceComponent
     */
    public void select(Locator[] components) throws InvalidLocatorException,
	    InvalidServiceComponentException, SecurityException,
	    IllegalStateException {

	select(components, (Locator[]) null);
    }

    public void select(Locator[] components, Locator causedComponent)
	    throws InvalidLocatorException, InvalidServiceComponentException,
	    SecurityException, IllegalStateException {

	select(components, new Locator[] { causedComponent });
    }

    public void select(Locator[] components, Locator[] causedComponents)
	    throws InvalidLocatorException, InvalidServiceComponentException,
	    SecurityException, IllegalStateException {

	if (components.length == 0) {
	    throw new IllegalArgumentException(
		    "Locator array length cannot be zero");
	}

	CacheManager siCache = CacheManager.getSICache();
	String serviceName = null;
	for (int i = 0; i < components.length; i++) {
	    if (LocatorImpl.isServiceComponent(components[i]) == false) {
		throw new InvalidLocatorException(components[i],
			"Not a ServiceComponent locator.");
	    }

	    if (!siCache.containsKey(components[i])) {
		throw new InvalidLocatorException(components[i],
			"Can't find a ServiceComponent for this locator.");
	    }

	    serviceName = LocatorImpl.getServiceName(components[i]);

	    if (serviceName == null) {
		throw new InvalidServiceComponentException(components[i]);
	    }

	    if (LocatorImpl.getServiceName(components[i]).equals(serviceName) == false) {
		throw new InvalidServiceComponentException(components[i]);
	    }

	}

	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    for (int i = 0; i < components.length; i++) {
		sm.checkPermission(new SelectPermission(components[i], "own"));
	    }
	}

	if (state == STATE_DESTROYED) {
	    throw new IllegalStateException(
		    "ServiceContext has been destroyed.");
	}

	String locatorStr = LocatorImpl.ServiceProtocol + serviceName;
	Locator newServiceLocator = null;
	try {
	    newServiceLocator = LocatorFactory.getInstance().createLocator(
		    locatorStr);
	} catch (MalformedLocatorException ex) {
	    ex.printStackTrace(); // shouldn't happen
	}

	select(newServiceLocator, components, causedComponents);

    }

    /**
     * Internal method for doing the actual service selection. This method
     * assumes that all parameter validity check, SC state check, permission
     * check, etc, are already performed.
     * 
     * @param serviceLoc
     *            A locator for the new service to be presented.
     * @param componentLocs
     *            An array of service component locators which should be
     *            presented together with the serviceLoc.
     * @param cause
     *            An array of locators which are in the original form that the
     *            application has used to cause this service selection. If the
     *            array is non-null, then the notification event generated
     *            (MediaSelectEvent etc) will contain this original form in it's
     *            'selection' field.
     */
    private synchronized void select(Locator selectionLocator,
	    Locator[] selectionComponentLocators, Locator[] cause) {

	CacheManager siCache = (CacheManager) CacheManager.getSICache();

	// First, check that if any of the new selection's components are
	// the same as the currently presenting locators. If so, those
	// need to be kept running instead of being stopped and restarted.
	Locator[] componentLocatorsToStop = getServiceComponentLocators();
	Locator[] componentLocatorsToStart = selectionComponentLocators;
	Vector locatorsToKeep = new Vector();
	Vector locatorsToReplace = new Vector();

	for (int i = 0; i < componentLocatorsToStop.length; i++) {
	    ServiceComponentImpl sci1 = (ServiceComponentImpl) siCache
		    .get(componentLocatorsToStop[i]);
	    for (int j = 0; j < componentLocatorsToStart.length; j++) {
		ServiceComponentImpl sci2 = (ServiceComponentImpl) siCache
			.get(componentLocatorsToStart[j]);

		if (sci1 == null || sci2 == null) {
		    new Exception("Just for check").printStackTrace();
		}

		if (sci1.getName().equals(sci2.getName())
			&& sci1.getStreamType().equals(sci2.getStreamType())) {

		    // TBD: Match streams other than ServiceComponentData?

		    if (sci1 instanceof ServiceComponentDataImpl
			    && sci2 instanceof ServiceComponentDataImpl) {
			ServiceComponentDataImpl scdi = (ServiceComponentDataImpl) sci1;
			if (scdi.getIsServiceUnbound()) {
			    // Found a service unbound xlet that is also
			    // signaled
			    // in a newly selected service.
			    locatorsToKeep.add(componentLocatorsToStop[i]);
			    locatorsToReplace.add(componentLocatorsToStart[j]);
			}
		    }
		}
	    }
	}

	try {

	    // Check if the current service is different from a new service.
	    boolean keepService = selectionLocator.equals(this.serviceLocator);

	    // first, stop the current service if it's different from the new
	    // one.
	    if (state == STATE_PRESENTING && !keepService) {
		StopService(this.serviceLocator);
	    }

	    // Next, stop all the components that are not identified as
	    // being available in the new service component list to select.
	    for (int i = 0; i < componentLocatorsToStop.length; i++) {
		if (!locatorsToKeep.contains(componentLocatorsToStop[i])) {
		    StopServiceComponent(componentLocatorsToStop[i]);
		}
	    }

	    // Remove the component locators that are being signaled in
	    // the new service from the current locator list.
	    // This allows the component to keep on running.
	    // Once the new service is started, the set of new locators that
	    // points to
	    // these service component are added back to the list.
	    for (int i = 0; i < locatorsToKeep.size(); i++) {
		Locator locator = (Locator) locatorsToKeep.elementAt(i);
		componentLocators.remove(locator);
	    }

	    // now, present a new service, if necessary.
	    if (!keepService) {
		PlayService(selectionLocator);
	    }

	    // And playback all the components that should be started.
	    for (int i = 0; i < componentLocatorsToStart.length; i++) {
		if (!locatorsToReplace.contains(componentLocatorsToStart[i])) {
		    PlayServiceComponent(getService(),
			    componentLocatorsToStart[i]);
		}
	    }

	    // Finally, add the component locators that are being signaled in
	    // both the old and the new service. These components didn't get
	    // replaced during this select(), meanwhile they need to be
	    // identified
	    // by locators that belong to a newly selected service.
	    for (int i = 0; i < locatorsToReplace.size(); i++) {
		Locator locator = (Locator) locatorsToReplace.elementAt(i);
		componentLocators.add(locator);
	    }

	    // 4383401, only notify MediaSelectControlImpl at this point,
	    // as application code needs to get notification only when the
	    // selection
	    // is actually completed in the jmf layer.
	    notifyListeners(new NormalContentCausedInternalEvent(this, cause));

	} catch (Exception e) {
	    SelectionFailedEvent event;
	    int reason = 255; // OTHER
	    if (cause != null) {
		event = new SelectionFailedCausedEvent(this, reason, cause);
	    } else {
		event = new SelectionFailedEvent(this, reason);
	    }
	    notifyListeners(event);
	    this.serviceLocator = null;
	    this.componentLocators.clear();
	}

    }

    /**
     * Causes the <code>ServiceContext</code> to stop presenting content and
     * enter the <em>not presenting</em> state. Resources used in the
     * presentation will be released, associated
     * <code>ServiceContentHandlers</code> will cease presentation (
     * <code>ServiceMediaHandlers</code> will no longer be in the
     * <em>started</em> state), and a <code>PresentationTerminatedEvent</code>
     * will be posted.
     * <p>
     * 
     * This operation completes asynchronously. No action is performed if the
     * <code>ServiceContext</code> is already in the <em>not
     * presenting</em> state.
     * 
     * @throws SecurityException
     *             If the caller owns this <code>ServiceContext</code> but does
     *             not have <code>ServiceContextPermission("stop", "own")</code>
     *             , or if the caller does not own this
     *             <code>ServiceContext</code> and does not have
     *             <code>SelectPermission("stop", "*")</code>.
     * 
     * @throws IllegalStateException
     *             If the <code>ServiceContext</code> has been destroyed.
     */
    public void stop() throws SecurityException, IllegalStateException {
	int reason = PresentationTerminatedEvent.USER_STOP;

	stop0(reason);
    }

    public void stop0(int reason) throws SecurityException,
	    IllegalStateException {
	if (state == STATE_NOT_PRESENTING) {
	    return;
	}

	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission(new ServiceContextPermission("stop", "own"));
	}

	if (state == STATE_DESTROYED) {
	    throw new IllegalStateException(
		    "ServiceContext has been destroyed.");
	}

	StopService(this.serviceLocator);

	Locator[] locators = getServiceComponentLocators();
	for (int i = 0; i < locators.length; i++) {
	    StopServiceComponent(locators[i]);
	}

	state = STATE_NOT_PRESENTING;

	notifyListeners(new PresentationTerminatedEvent(this, reason));
    }

    /**
     * Causes the <code>ServiceContext</code> to release all resources and enter
     * the <em>destroyed</em> state. This method indicates that the the
     * <code>ServiceContext</code> must cease further activity, and that it will
     * no longer be used. After completion of this method,
     * <code>ServiceMediaHandler</code> instances associated with this
     * <code>ServiceContext</code> will have become <em>unrealized</em> or will
     * have been closed.
     * 
     * If the <code>ServiceContext</code> is currently in the
     * <em>presenting</em> or <em>presentation pending</em> state, this method
     * will first stop the <code>ServiceContext</code>, causing a
     * <code>PresentationTerminatedEvent</code> to be posted. After the
     * <code>ServiceContext</code> has moved to the <em>destroyed</em> state, a
     * <code>ServiceContextDestroyedEvent</code> will be posted.
     * <p>
     * 
     * This operation completes asynchronously. No action is performed if the
     * <code>ServiceContext</code> is already in the <em>destroyed</em> state.
     * 
     * @throws SecurityException
     *             If the caller does not have permission to call
     *             <code>stop()</code> on this <code>ServiceContext</code>, or
     *             if the caller owns this <code>ServiceContext</code> but does
     *             not have
     *             <code>ServiceContextPermission("destroy", "own")</code>, or
     *             if the caller does not own this <code>ServiceContext</code>
     *             and does not have
     *             <code>SelectPermission("destroy", "*")</code>.
     * 
     * @see #stop
     */
    public void destroy() throws SecurityException {
	if (state == STATE_DESTROYED) {
	    return;
	}

	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission(new ServiceContextPermission("destroy", "own"));
	}

	if (state == STATE_PRESENTING || state == STATE_PRESENTATION_PENDING) {
	    this.stop();
	}

	state = STATE_DESTROYED;
	notifyListeners(new ServiceContextDestroyedEvent(this));

	serviceHandlers.clear();
	serviceListeners.clear();
	componentLocators.clear();
	serviceLocator = null;
    }

    /**
     * Reports the current collection of ServiceContentHandlers. A zero-length
     * array is returned if the <code>ServiceContext</code> is in in the
     * <em>not presenting</em> or <em>presentation
     * pending</em> states.
     * 
     * @throws SecurityException
     *             If the caller owns this <code>ServiceContext</code> but does
     *             not have
     *             <code>ServiceContextPermission("getServiceContentHandlers",
     * "own")</code>, or if the caller does not own this
     *             <code>ServiceContext</code> and does not have
     *             <code>SelectPermission("getServiceContentHandlers", "*")</code>
     *             .
     * 
     * @return The current <code>ServiceContentHandler</code> instances.
     * 
     * @throws IllegalStateException
     *             If the <code>ServiceContext</code> has been destroyed.
     */
    public ServiceContentHandler[] getServiceContentHandlers()
	    throws SecurityException {

	if (state == STATE_DESTROYED) {
	    throw new IllegalStateException("Already destroyed");
	    // 4822249
	}
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission(new ServiceContextPermission(
		    "getServiceContentHandlers", "own"));
	}

	if (state != STATE_PRESENTING) {
	    return new ServiceContentHandler[0];
	}
	int count = serviceHandlers.size();

	ServiceContentHandler handlers[] = new ServiceContentHandler[count];
	if (handlers == null) {
	    return null;
	}
	int index = 0;
	Enumeration list = serviceHandlers.elements();
	while (list.hasMoreElements()) {
	    handlers[index] = (ServiceContentHandler) list.nextElement();
	    index++;
	}
	return handlers;
    }

    /**
     * 
     * Provides a <code>Locator</code> to the service being presented in this
     * service context. If the service context is currently presenting a
     * service, the locator returned will be a network-dependent locator to the
     * service indicated in the last successful <code>select</code> method call.
     * If the service context is not currently presenting a service or has been
     * destroyed, then <code>null</code> is returned.
     * 
     * @return A locator for the service currently being presented or
     *         <code>null</code> if no service is being presented.
     * 
     * @see javax.tv.service.Service
     */
    public Locator getServiceLocator() {
	if (state == STATE_NOT_PRESENTING || state == STATE_DESTROYED) {
	    return null;
	}

	return this.serviceLocator;
    }

    /**
     * Reports the <code>Service</code> being presented in this
     * <code>ServiceContext</code>. If the <code>ServiceContext</code> is
     * currently presenting a service, the <code>Service</code> returned will be
     * a network-dependent representation of the <code>Service</code> indicated
     * in the last successful <code>select()</code> method call. If the
     * <code>ServiceContext</code> is not in the <em>presenting</em> state then
     * <code>null</code> is returned.
     * 
     * @return The service currently being presented.
     * 
     * @throws IllegalStateException
     *             If the <code>ServiceContext</code> has been destroyed.
     */
    public Service getService() {
	SIManager siManager = (SIManager) SIManager.createInstance();

	if (state == STATE_DESTROYED) {
	    throw new IllegalStateException("ServiceContext has been destroyed");
	}

	try {
	    return siManager.getService(this.serviceLocator);
	} catch (Exception e) {
	    ;
	}
	return null;
    }

    /**
     * 
     * Subscribes a listener to receive events related to this ServiceContext.
     * If the specified listener is currently subscribed then no action is
     * performed.
     * 
     * @param listener
     *            The ServiceContextListener to subscribe.
     * 
     * @throws NullPointerException
     *             If <code>listener</code> is <code>null</code>.
     * 
     * @throws IllegalStateException
     *             If the ServiceContext has been destroyed.
     * 
     * @see ServiceContextEvent
     */
    public void addListener(ServiceContextListener listener)
	    throws IllegalStateException {

	if (listener == null) {
	    throw new NullPointerException();
	}

	if (state == STATE_DESTROYED) {
	    throw new IllegalStateException(
		    "ServiceContext has been destroyed.");
	}

	if (serviceListeners.contains(listener) == false) {
	    serviceListeners.addElement(listener);
	}
    }

    /**
     * Unsubscribes a listener from receiving events related to this
     * ServiceContext. If the specified listener is not currently subscribed
     * then no action is performed.
     * 
     * @param listener
     *            The ServiceContextListener to unsubscribe.
     * 
     * @throws NullPointerException
     *             if <code>listener</code> is <code>null</code>.
     * 
     * @throws IllegalStateException
     *             If the ServiceContext has been destroyed.
     */
    public void removeListener(ServiceContextListener listener)
	    throws IllegalStateException {
	if (listener == null) {
	    throw new NullPointerException();
	}

	if (state == STATE_DESTROYED) {
	    throw new IllegalStateException(
		    "ServiceContext has been destroyed.");
	}

	if (serviceListeners.contains(listener) == true) {
	    serviceListeners.removeElement(listener);
	}
    }

    /**
     * This method notifies all listeners of this service context, of provied
     * service context event.
     */
    private void notifyListeners(ServiceContextEvent event) {
	Vector listeners = (Vector) serviceListeners.clone();

	for (int i = 0; i < listeners.size(); i++) {
	    ServiceContextListener listener = (ServiceContextListener) listeners
		    .elementAt(i);

	    if (listener == null) {
		continue;
	    }

	    boolean isInternalListener = (listener.getClass().getName()
		    .indexOf("MediaSelectControlImpl") != -1);

	    if (event instanceof InternalEvent) {
		if (!isInternalListener) {
		    continue;
		}
	    }

	    notifyAsyncListener(event, listener);
	}
    }

    private void notifyAsyncListener(ServiceContextEvent event,
	    ServiceContextListener listener) {

	if (listener == null || event == null) {
	    return;
	}
	NotifyServiceContextThread thread = new NotifyServiceContextThread(
		event, listener);
	if (thread != null) {
	    thread.start();
	}
    }

    /**
     * Returns the list of cached ServiceComponents that are a part of a given
     * service.
     * 
     * @param service
     *            The service to look components for.
     * @return An array of Locators representing a given service's components.
     */
    public static Locator[] getServiceComponentLocators(Service service) {

	CacheManager siCache = CacheManager.getSICache();

	Vector results = new Vector();

	synchronized (siCache) {
	    Enumeration elements = siCache.elements();
	    while (elements.hasMoreElements()) {
		SIElement element = (SIElement) elements.nextElement();
		if (!(element instanceof ServiceComponentImpl)) {
		    continue;
		}
		Locator serviceLocator = service.getLocator();
		Locator componentLocator = element.getLocator();

		String serviceName1 = LocatorImpl
			.getServiceName(serviceLocator);
		String serviceName2 = LocatorImpl
			.getServiceName(componentLocator);
		if (serviceName1 != null && serviceName1.equals(serviceName2)) {
		    results.add(componentLocator); // belong to the same service
		}
	    }
	}
	return (Locator[]) results.toArray(new Locator[results.size()]);
    }

    /**
     * Reports the current collection of Service Component Locators. A
     * zero-length array is returned if the <code>ServiceContext</code> is in in
     * the <em>not presenting</em> or <em>presentation
     * pending</em> states.
     * 
     * @return The current Service Component <code>Locator</code> instances.
     * 
     * @throws IllegalStateException
     *             If the <code>ServiceContext</code> has been destroyed.
     */
    public Locator[] getServiceComponentLocators() {

	if (state == STATE_DESTROYED) {
	    return null;
	}
	if (componentLocators == null) {
	    return new Locator[0];
	}

	return (Locator[]) componentLocators
		.toArray(new Locator[componentLocators.size()]);
    }

    /**
     * The isDestroyed method is not part of the JavaTV API, it is used to
     * indicate if this context is still valid within the RI. The
     * ServiceContextFactory needs to return a list of all non-destroyed
     * ServiceContexts.
     */
    public boolean isDestroyed() {
	return (state == STATE_DESTROYED);
    }

    /**
     * Handle Controller/Player events for the player that's currently active.
     */
    public synchronized void controllerUpdate(ControllerEvent ce) {
	if (ce == null) {
	    return;
	}
	Controller controller = ce.getSourceController();
	if (controller == null || !(controller instanceof Player)) {
	    return;
	}
	Player player = (Player) controller;
	if (ce instanceof RealizeCompleteEvent) {

	    EkhoTVProxyActivity proxy = EkhoTVProxyActivity.getInstance();
	    Context context = proxy.getActivity().getApplicationContext();

	    DisplayManager dispManager = DisplayManager.createInstance(context);
	    if (dispManager == null) {
		return;
	    }
	    ViewGroup frame = dispManager.getRootFrame();
	    if (frame == null) {
		return;
	    }
	    View comp = player.getVisualComponent();
	    if (comp != null && comp.getParent() == null) {
		frame.addView(comp);
	    }

	    // comp = player.getControlPanelComponent();
	    frame.invalidate();

	} else if (ce instanceof StartEvent) { // 6344273. Notify the listeners
	    // only when the Player is in the
	    // Started state.

	    state = STATE_PRESENTING; // 6342873. Set the state before
	    // delivering ServiceContextEvent.

	    if (LocatorImpl.isAlternate(this.serviceLocator)) {
		notifyListeners(new AlternativeContentEvent(this));

	    } else {
		notifyListeners(new NormalContentEvent(this));
	    }

	} else if (ce instanceof PrefetchCompleteEvent) {
	    ;

	    // EndOfMediaEvent occurs when the media file has played till the
	    // end.
	    // The player is now in the stopped state.
	} else if (ce instanceof EndOfMediaEvent) {
	    player.setMediaTime(new Time(0)); //

	    player.start(); //

	    // If at any point the Player encountered an error - possibly in the
	    // data stream
	    // and it could not recover from the error, it generates a
	    // ControllerErrorEvent
	} else if (ce instanceof ControllerErrorEvent) {
	    Log.i("EkhoTV", "ControllerErrorEvent: " + ce);

	    // Occurs when a player is closed.
	} else if (ce instanceof ControllerClosedEvent) {
	    ;

	    // DurationUpdateEvent occurs when the player's duration changes or
	    // is
	    // updated for the first time
	} else if (ce instanceof DurationUpdateEvent) {
	    Time t = ((DurationUpdateEvent) ce).getDuration();

	    // Caching control.
	} else if (ce instanceof CachingControlEvent) {
	    ;
	} else if (ce instanceof MediaTimeSetEvent) {
	    ;
	} else if (ce instanceof TransitionEvent) {
	    ;
	} else if (ce instanceof RateChangeEvent) {
	    ;
	} else if (ce instanceof StopTimeChangeEvent) {
	    ;
	} else if (ce instanceof DurationUpdateEvent) {
	    Log.i("EkhoTV", "Duration: "
		    + ((DurationUpdateEvent) ce).getDuration().getSeconds());

	} else {
	    // Catch implementation specific events here...

	    // UnsupportedFormatEvent is not a part of the JMF 1.0 spec.
	    // It is generates when the media contains something that is not
	    // supported by the underlying framework. For example, a QuickTime
	    // movie with a VR track, an unsupported codec, etc.

	    // SizeChangeEvent is not a part of the JMF 1.0 spec. It is
	    // generated when the size of the video changes or right at the
	    // beginning of a video clip, to inform listeners about the
	    // dimensions of the video
	}
    }
}

class NotifyServiceContextThread extends Thread {

    ServiceContextListener listener = null;
    ServiceContextEvent event = null;

    public NotifyServiceContextThread(ServiceContextEvent event,
	    ServiceContextListener listener) {

	super("NotifyServiceContextThread");

	this.listener = listener;
	this.event = event;
    }

    public void run() {
	if (this.listener == null || this.event == null) {
	    return;
	}
	listener.receiveServiceContextEvent(event);
    }
}
