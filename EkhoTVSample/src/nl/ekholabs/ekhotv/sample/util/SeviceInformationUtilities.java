package nl.ekholabs.ekhotv.sample.util;

import java.util.Properties;

import javax.tv.locator.Locator;
import javax.tv.locator.LocatorFactory;
import javax.tv.service.SIManager;
import javax.tv.service.Service;
import javax.tv.service.navigation.ServiceIterator;
import javax.tv.service.navigation.ServiceList;
import javax.tv.service.selection.ServiceContext;
import javax.tv.service.selection.ServiceContextFactory;

import android.util.Log;

/**
 * @authr Wilder Rodrigues (wilder.rodrigues@ekholabs.nl)
 */
public class SeviceInformationUtilities {

    static {
	String contentPathProp = "java.content.handler.pkgs";
	Properties props = System.getProperties();
	props.put(contentPathProp, "com.sun.media.content");
	System.setProperties(props);
    }

    public ServiceIterator loadServices() {

	Locator locator = null;
	LocatorFactory factory = null;

	try {
	    SIManager manager = SIManager.createInstance();
	    if (manager == null) {
		throw new Exception("SIManager.createInstance() == null");
	    }

	    factory = LocatorFactory.getInstance();
	    if (factory == null) {
		throw new Exception("LocatorFactory.getInstance() == null");
	    }

	    locator = factory.createLocator("service:/SERV1");
	    if (locator == null) {
		throw new Exception("factory.createLocator() == null");
	    }

	    ServiceContextFactory scFactory = ServiceContextFactory
		    .getInstance();
	    if (scFactory == null) {
		throw new Exception("ServiceContextFactory == null");
	    }

	    final ServiceContext context = scFactory.createServiceContext();
	    if (context == null) {
		throw new Exception("createServiceContext == null");
	    }

	    final Service service = manager.getService(locator);
	    if (service == null) {
		throw new Exception("getService == null");
	    }

	    Runnable runSelect = new Runnable() {
	        
	        @Override
	        public void run() {
	            context.select(service);
	        }
	    };
	    
	    new Thread(runSelect).start();

	    ServiceList services = manager.filterServices(null);
	    ServiceIterator iterator = services.createServiceIterator();

	    return iterator;
	} catch (Exception e) {
	    Log.e("JavaTV", e.getMessage());
	}
	return null;
    }
}