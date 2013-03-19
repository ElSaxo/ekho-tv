/*
 * @(#)ReceiverFile.java	1.40 08/09/15
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 */

package com.sun.tv.receiver;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Vector;

import javax.tv.locator.Locator;
import javax.tv.locator.LocatorFactory;
import javax.tv.service.SIManager;
import javax.tv.service.navigation.DeliverySystemType;
import javax.tv.service.navigation.StreamType;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import nl.ekholabs.ekhotv.activity.EkhoTVProxyActivity;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.sun.tv.LocatorImpl;
import com.sun.tv.si.RatingDimensionImpl;
import com.sun.tv.si.SIManagerImpl;

public class ReceiverFile extends DefaultHandler implements SampleDataInterface {

    private transient static SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
	    "MM/dd/yyyy HH:mm");

    private SIEmulator emulator = null;

    private long last_modified = 0;
    private File sample_file = null;

    private String serviceName;
    private String programName;
    private String programDescription;
    private String sitype;

    private RatingDimensionImpl rating = null;
    private Vector dimensionNames = null;
    private Vector ratingNames = null;
    private long transmitTime = 0;
    private long startTime = 0;
    private long duration = 0;

    // --------------------------------------------------------------------
    public ReceiverFile() {
    }

    // --------------------------------------------------------------------
    public void play(SIEmulator emulator, String args[]) {
	this.emulator = emulator;

	String filename = Settings.SampleFile;
	if (args != null && args.length >= 1 && args[0] != null) {
	    filename = args[0];
	}
	setFile(sample_file = new File(filename));

	last_modified = sample_file.lastModified();

	WatcherThread t = new WatcherThread();

	t.start();
    }

    // --------------------------------------------------------------------
    public void finish() {
	emulator.isCaughtUp();
    }

    // --------------------------------------------------------------------
    public boolean verify() {
	return true;
    }

    // --------------------------------------------------------------------
    private void setFile(File file) {
	if (file != null) {
	    // Document document = createDocument(file);
	    // processDocument(document);
	    try {
		EkhoTVProxyActivity proxy = EkhoTVProxyActivity.getInstance();
		Context context = proxy.getActivity().getApplicationContext();

		Resources resources = context.getResources();
		AssetManager assetManager = resources.getAssets();
		InputStream inputStream = assetManager.open(file.getName());

		SAXParser parser = SAXParserFactory.newInstance()
			.newSAXParser();

		parser.parse(inputStream, this);
	    } catch (Exception e) {
		System.err.println("Parsing failed: " + e + ", file: " + file);
	    }
	}
    }

    // --------------------------------------------------------------------
    private long toDate(String str) {
	if (str == null || str.length() == 0) {
	    return System.currentTimeMillis();
	} else if (str.equalsIgnoreCase("sysdate")) {
	    return System.currentTimeMillis();
	} else if (str.equalsIgnoreCase("current")) {
	    return System.currentTimeMillis();
	} else if (str.startsWith("+")) {
	    return System.currentTimeMillis() + toLong(str.substring(1)) * 60
		    * 1000;
	} else if (str.startsWith("-")) {
	    return System.currentTimeMillis() - toLong(str.substring(1)) * 60
		    * 1000;
	}

	try {
	    return dateTimeFormat.parse(str).getTime();
	} catch (Exception e) {
	    System.err.println("toDate: " + e + ": " + str);
	}
	return 0;
    }

    // --------------------------------------------------------------------
    private long toTransmitTime(String str) {
	if (str == null || str.length() == 0) {
	    return 0;
	}

	if (str.startsWith("+")) {
	    return toLong(str.substring(1));
	} else if (str.startsWith("-")) {
	    return -toLong(str.substring(1));
	}

	/**
	 * try { return dateTimeFormat.parse(str).getTime() / 1000; } catch
	 * (Exception e) { System.err.println("toDate: "+e + ": " + str); }
	 **/
	return 0;
    }

    // --------------------------------------------------------------------
    private long toLong(String str) {
	try {
	    return Long.parseLong(str);
	} catch (Exception e) {
	    System.err.println("toLong: " + e + ": " + str);
	}
	return 0;
    }

    // --------------------------------------------------------------------
    private int toInt(String str) {
	if (str == null || str.length() == 0) {
	    return 0;
	}

	try {
	    return Integer.parseInt(str);
	} catch (Exception e) {
	    System.err.println("toInt: " + e + ": " + str);
	}
	return 0;
    }

    // --------------------------------------------------------------------
    private boolean toBoolean(String str) {
	try {
	    return new Boolean(str).booleanValue();
	} catch (Exception e) {
	    System.err.println("toBoolean: " + e + ": " + str);
	}
	return false;
    }

    // --------------------------------------------------------------------
    public void startDocument() {
    }

    // --------------------------------------------------------------------
    public void endDocument() {
	// perform sanity checks here
    }

    // --------------------------------------------------------------------
    public void startElement(String uri, String localName, String qName,
	    Attributes attributes) {

	if ("TestData".equalsIgnoreCase(qName)) {
	    // Only children there
	    // processTestData(attributes);
	}

	// TestData children
	else if ("DataBundle".equalsIgnoreCase(qName)) {
	    // Only children there
	    // processDataBundle(attributes);
	}

	// DataBundle children
	else if ("TransportStream".equalsIgnoreCase(qName)) {
	    processTransportStream(attributes);

	} else if ("Network".equalsIgnoreCase(qName)) {
	    processNetwork(attributes);

	} else if ("Bouquet".equalsIgnoreCase(qName)) {
	    processBouquet(attributes);

	} else if ("Xlet".equalsIgnoreCase(qName)) {
	    processResidentXlet(attributes);

	} else if ("FavoriteService".equalsIgnoreCase(qName)) {
	    processFavoriteService(attributes);

	} else if ("PreferredLanuage".equalsIgnoreCase(qName)) {
	    processPreferredLanguage(attributes);
	    // language = element; // Not Needed

	} else if ("RatingDimension".equalsIgnoreCase(qName)) {
	    processRatingDimension(attributes);

	} else if ("ServiceTransforms".equalsIgnoreCase(qName)) {
	    processServiceTransforms(attributes);

	} else if ("Service".equalsIgnoreCase(qName)) {
	    processService(attributes);

	} else if ("RemoveList".equalsIgnoreCase(qName)) {
	    // Only children here
	    // processRemoveList(attributes);

	} else if ("RemoveSIDatabase".equalsIgnoreCase(qName)) {
	    RemoveSIDatabase(attributes);
	}

	// RatingDimension children
	else if ("Rating".equalsIgnoreCase(qName)) {
	    processRating(attributes);
	}

	// Service children
	else if ("ServiceDetails".equalsIgnoreCase(qName)) {
	    processServiceDetails(attributes, serviceName, sitype);
	}

	// ServiceDetails children
	else if ("ProgramSchedule".equalsIgnoreCase(qName)) {
	    // Only children there
	    // processProgramSchedule(attributes, serviceName, sitype);

	} else if ("ServiceComponent".equalsIgnoreCase(qName)) {
	    processServiceComponent(attributes, serviceName, null, sitype);

	} else if ("ServiceComponentData".equalsIgnoreCase(qName)) {
	    processServiceComponentData(attributes, serviceName, null, sitype);
	}

	// ProgramSchedule children
	else if ("ProgramEvent".equalsIgnoreCase(qName)) {
	    processProgramEvent(attributes, serviceName, sitype);

	}

	// ProgramEvent children
	else if ("ServiceComponent".equalsIgnoreCase(qName)) {
	    processServiceComponent(attributes, serviceName, programName,
		    sitype);
	} else if ("ContentRatingAdvisory".equalsIgnoreCase(qName)) {
	    processContentRatingAdvisory(attributes);
	}

	// RemoveList children
	else if ("RemoveTransportStream".equalsIgnoreCase(qName)) {
	    int id = toInt((String) attributes.getValue("ID"));
	    emulator.removeTransportStream(0, id);

	} else if ("RemoveNetwork".equalsIgnoreCase(qName)) {
	    int id = toInt((String) attributes.getValue("ID"));
	    emulator.removeNetwork(0, id);

	} else if ("RemoveBouquet".equalsIgnoreCase(qName)) {
	    int id = toInt((String) attributes.getValue("ID"));
	    emulator.removeBouquet(0, id);

	} else if ("RemoveXlet".equalsIgnoreCase(qName)) {
	    String name = (String) attributes.getValue("NAME");
	    emulator.removeXlet(0, name);

	} else if ("RemoveService".equalsIgnoreCase(qName)) {
	    String name = (String) attributes.getValue("NAME");
	    String reason = (String) attributes.getValue("REASON");

	    emulator.removeService(0, name, reason);

	} else if ("RemoveServiceDetails".equalsIgnoreCase(qName)) {
	    String name = (String) attributes.getValue("NAME");
	    emulator.removeServiceDetails(0, name);

	} else if ("RemoveServiceComponent".equalsIgnoreCase(qName)) {
	    String name = (String) attributes.getValue("NAME");
	    emulator.removeServiceComponent(0, name);

	} else if ("RemoveProgramEvent".equalsIgnoreCase(qName)) {
	    programName = (String) attributes.getValue("PROGRAM_NAME");
	    serviceName = (String) attributes.getValue("SERVICE_NAME");
	    emulator.removeProgramEvent(0, serviceName, programName);
	}
    }

    // --------------------------------------------------------------------
    private void processPreferredLanguage(Attributes attributes) {
	String language = (String) attributes.getValue("VALUE");

	SIManager siManager = SIManager.createInstance();
	if (siManager != null) {
	    try {
		siManager.setPreferredLanguage(language);
	    } catch (Exception e) {
		Log.e("EkhoTV", "SetPreferredLanguage failed - " + language);
	    }
	}
    }

    // --------------------------------------------------------------------
    private void processFavoriteService(Attributes attributes) {
	String name = (String) attributes.getValue("NAME");
	String snames = (String) attributes.getValue("SERVICENAMES");

	SIManagerImpl.putFavoriteServices(name,
		SIEmulator.toStrings(snames, ","));
    }

    // --------------------------------------------------------------------
    private void processRatingDimension(Attributes attributes) {
	String ratingName = (String) attributes.getValue("DIMENSIONNAME");

	rating = new RatingDimensionImpl(ratingName);
    }

    // --------------------------------------------------------------------
    private void processServiceTransforms(Attributes attributes) {
	String name = (String) attributes.getValue("NAME");
	String list[] = emulator.toStrings(
		(String) attributes.getValue("LIST"), ",");
	try {
	    LocatorFactory factory = LocatorFactory.getInstance();

	    Locator locator = factory.createLocator(LocatorImpl.ServiceProtocol
		    + name);

	    Vector vector = new Vector();
	    for (int i = 0; i < list.length; i++) {
		Locator loc = factory.createLocator(LocatorImpl.ServiceProtocol
			+ list[i]);
		vector.addElement(loc);
	    }
	    LocatorImpl.setTransforms(locator, vector);
	} catch (Exception e) {
	    Log.e("EkhoTV", "ProcessServiceTransforms error: " + e);
	}
    }

    // --------------------------------------------------------------------
    private void processRating(Attributes attributes) {
	String name = (String) attributes.getValue("NAME");
	String desc = (String) attributes.getValue("DESCRIPTION");
	rating.addRatingLevelDescription(name, desc);
    }

    // --------------------------------------------------------------------
    private void processContentRatingAdvisory(Attributes attributes) {
	String name = (String) attributes.getValue("DIMENSIONNAME");
	String text = (String) attributes.getValue("RATINGNAME");
	// TBD: level is unused.
	String level = (String) attributes.getValue("RATINGLEVEL");

	dimensionNames.addElement(name);
	ratingNames.addElement(text);
    }

    // --------------------------------------------------------------------
    private void processService(Attributes attributes) {
	serviceName = (String) attributes.getValue("NAME");
	String serviceType = (String) attributes.getValue("SERVICETYPE");
	sitype = (String) attributes.getValue("SITYPE");
	String url = (String) attributes.getValue("SIMULATION");
	String alternateURL = (String) attributes.getValue("ALTERNATE");
	int serviceNumber = toInt((String) attributes.getValue("NUMBER"));
	int serviceMinorNumber = toInt((String) attributes
		.getValue("MINORNUMBER"));
	String reason = (String) attributes.getValue("CALIMIT");
	long transmitTime = toTransmitTime((String) attributes
		.getValue("TRANSMITTIME"));

	// String serviceLanguage = (String)language.getAttribute("LANGUAGE");
	// String serviceLocator = (String)attributes.getValue("LOCATOR");

	emulator.putService(transmitTime, serviceName, false, serviceType,
		sitype, serviceNumber, serviceMinorNumber, reason);

	LocatorImpl.setMediaFile("service:/" + serviceName, url);
	if (alternateURL != null && alternateURL.length() > 0) {
	    LocatorImpl.setMediaFile("alternate:/" + serviceName, alternateURL);
	}
    }

    // --------------------------------------------------------------------
    private void processServiceDetails(Attributes attributes,
	    String serviceName, String sitype) {

	String longname = (String) attributes.getValue("LONGNAME");
	String providerName = (String) attributes.getValue("PROVIDERNAME");
	DeliverySystemType deliveryType = SIEmulator
		.toDeliverySystemType((String) attributes
			.getValue("DELIVERYSYSTEMTYPE"));
	String desc = (String) attributes.getValue("DESCRIPTION");
	String caSystemIDs = (String) attributes.getValue("CASYSTEMIDS");
	long transmitTime = toTransmitTime((String) attributes
		.getValue("TRANSMITTIME"));

	emulator.putServiceDetails(transmitTime, serviceName, providerName,
		deliveryType, longname, caSystemIDs);

	emulator.putServiceDescription(transmitTime, serviceName, desc);
    }

    // --------------------------------------------------------------------
    private void processServiceComponent(Attributes attributes,
	    String serviceName, String programName, String sitype) {
	String componentName = (String) attributes.getValue("NAME");
	String language = (String) attributes.getValue("LANGUAGE");
	StreamType streamType = SIEmulator.toStreamType((String) attributes
		.getValue("STREAM_TYPE"));
	String url = (String) attributes.getValue("SIMULATION");
	String alternateURL = (String) attributes.getValue("ALTERNATE");
	long transmitTime = toTransmitTime((String) attributes
		.getValue("TRANSMITTIME"));
	boolean autorun = toBoolean((String) attributes.getValue("AUTORUN"));
	String reason = (String) attributes.getValue("CALIMIT");

	emulator.putServiceComponent(transmitTime, componentName, language,
		streamType, serviceName, programName, sitype, autorun, reason);

	LocatorImpl.setMediaFile("component:/" + componentName + "service:/"
		+ serviceName, url);
	if (alternateURL != null && alternateURL.length() > 0) {
	    LocatorImpl.setMediaFile("alternate:/" + componentName,
		    alternateURL);
	}
    }

    // --------------------------------------------------------------------
    private void processServiceComponentData(Attributes attributes,
	    String serviceName, String programName, String sitype) {

	String xletName = (String) attributes.getValue("NAME");
	String language = (String) attributes.getValue("LANGUAGE");
	StreamType streamType = StreamType.DATA;
	String xletPath = (String) attributes.getValue("PATH");
	String xletArgs = (String) attributes.getValue("ARGS");
	boolean autorun = toBoolean((String) attributes.getValue("AUTORUN"));
	boolean isServiceUnbound = toBoolean((String) attributes
		.getValue("ISSERVICEUNBOUND"));
	long transmitTime = toTransmitTime((String) attributes
		.getValue("TRANSMITTIME"));
	String reason = (String) attributes.getValue("CALIMIT");

	emulator.putServiceComponentData(transmitTime, xletName, language,
		streamType, serviceName, programName, sitype, autorun, reason,
		xletPath, xletArgs, isServiceUnbound);
    }

    // --------------------------------------------------------------------
    private void processProgramEvent(Attributes attributes, String serviceName,
	    String sitype) {
	programName = (String) attributes.getValue("NAME");
	programDescription = (String) attributes.getValue("DESCRIPTION");
	duration = toLong((String) attributes.getValue("DURATION"));
	startTime = toDate((String) attributes.getValue("STARTTIME"));
	// endTime = toDate((String)attributes.getValue("ENDTIME")); // Not
	// Needed
	transmitTime = toTransmitTime((String) attributes
		.getValue("TRANSMITTIME"));

	dimensionNames = new Vector();
	ratingNames = new Vector();
    }

    // --------------------------------------------------------------------
    private void processTransportStream(Attributes attributes) {
	int id = toInt((String) attributes.getValue("ID"));
	String name = (String) attributes.getValue("NAME");
	String sitype = (String) attributes.getValue("SITYPE");
	long transmitTime = toTransmitTime((String) attributes
		.getValue("TRANSMITTIME"));
	int networkID = toInt((String) attributes.getValue("NETWORKID"));

	emulator.putTransportStream(transmitTime, name, id, sitype, networkID);
    }

    // --------------------------------------------------------------------
    private void processNetwork(Attributes attributes) {
	int id = toInt((String) attributes.getValue("ID"));
	String name = (String) attributes.getValue("NAME");
	String sitype = (String) attributes.getValue("SITYPE");
	long transmitTime = toTransmitTime((String) attributes
		.getValue("TRANSMITTIME"));

	emulator.putNetwork(transmitTime, name, id, sitype);
    }

    // --------------------------------------------------------------------
    private void processBouquet(Attributes attributes) {
	int id = toInt((String) attributes.getValue("ID"));
	String name = (String) attributes.getValue("NAME");
	String sitype = (String) attributes.getValue("SITYPE");
	long transmitTime = toTransmitTime((String) attributes
		.getValue("TRANSMITTIME"));

	emulator.putBouquet(transmitTime, name, id, sitype);
    }

    // --------------------------------------------------------------------
    private void processResidentXlet(Attributes attributes) {
	String name = (String) attributes.getValue("NAME");
	String pathStr = (String) attributes.getValue("PATH");
	String argStr = (String) attributes.getValue("ARGS");
	long transmitTime = toTransmitTime((String) attributes
		.getValue("TRANSMITTIME"));

	String paths[] = emulator.toStrings(pathStr, ",");
	String args[] = emulator.toStrings(argStr, ",");

	emulator.putResidentXlet(transmitTime, name, paths, args);
    }

    // --------------------------------------------------------------------
    private void RemoveSIDatabase(Attributes attributes) {
	boolean genEvents = true;
	emulator.RemoveSIDatabase(0, genEvents);
    }

    // --------------------------------------------------------------------
    public void endElement(String uri, String localName, String qName) {
	// insert sanity checks here
	if ("ProgramEvent".equalsIgnoreCase(qName)) {
	    emulator.putProgramEventAbs(transmitTime, programName, serviceName,
		    startTime, duration, sitype, dimensionNames, ratingNames);

	    emulator.putProgramEventDescription(transmitTime, programName,
		    programDescription);
	}
    }

    // --------------------------------------------------------------------
    class WatcherThread extends Thread {
	public void run() {
	    long sf_mod = 0;

	    while (true) {
		try {
		    sleep(10000);
		} catch (InterruptedException e) {
		}

		if (last_modified < (sf_mod = sample_file.lastModified())) {
		    setFile(sample_file);
		    emulator.isCaughtUp();
		    last_modified = sf_mod;
		}
	    }
	}
    }

}
