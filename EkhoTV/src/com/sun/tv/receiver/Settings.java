/*
 * @(#)Settings.java	1.23 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.receiver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import nl.ekholabs.ekhotv.activity.EkhoTVProxyActivity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.sun.tv.si.SIManagerImpl;

public class Settings {
    public static final int REQ_GENERAL = 0;
    public static final int REQ_SERVICE_COMPONENT = 1;
    public static final int REQ_SERVICE_DESCRIPTION = 2;
    public static final int REQ_CURRENT_PROGRAM_EVENT = 3;
    public static final int REQ_FUTURE_PROGRAM_EVENT = 4;
    public static final int REQ_FUTURE_PROGRAM_EVENTS = 5;
    public static final int REQ_PROGRAM_EVENT = 6;
    public static final int REQ_NEXT_PROGRAM_EVENT = 7;
    public static final int REQ_TRANSPORT_STREAM = 8;

    public static long REQUEST_DURATION = 15 * 1000; // 15 seconds

    public static String PreferredLanguage = "eng";

    public static String ReceiverRatingNames[] = new String[2];
    public static String ReceiverRatingLevels[] = new String[2];

    public static String AWTVideoSizeControlClassName = "com.sun.tv.media.AWTVideoSizeControlImpl";

    public static String MediaSelectControlClassName = "com.sun.tv.media.MediaSelectControlImpl";

    public static String RootContainerClassName = "com.sun.tv.RootFrame";

    // public static String SampleClass = "SampleData_01";
    public static String SampleClass = "com.sun.tv.receiver.ReceiverFile";
    public static String SampleFile = "JavaTVSampleFile01.xml";
    public static int SampleInterval = 1000;

    public static String _defaultProperties = "JavaTV.properties";

    public static int ContextServiceLimit = 50;

    static {
	// SIManagerImpl.putFavoriteServices("SERV5",
	// SIEmulator.toStrings("SERV5", ","));
	ReceiverRatingNames[0] = "MPAA";
	ReceiverRatingLevels[0] = "Parental Guidance under 13";

	ReceiverRatingNames[1] = "YAMPAA";
	ReceiverRatingLevels[1] = "Phoney desc for D";

	Load(_defaultProperties);
    }

    public static void Load(String filename) {
	try {
	    EkhoTVProxyActivity proxy = EkhoTVProxyActivity.getInstance();
	    Context context = proxy.getActivity().getApplicationContext();

	    Resources resources = context.getResources();
	    AssetManager assetManager = resources.getAssets();
	    InputStream inputStream = assetManager.open(filename);

	    Properties p = new Properties();

	    p.load(inputStream);

	    SampleClass = p.getProperty("ServiceFileHandler", SampleClass);

	    SampleFile = p.getProperty("ServiceFile", SampleFile);

	    RootContainerClassName = p.getProperty("RootContainerClassName",
		    RootContainerClassName);

	    REQUEST_DURATION = getProperty(p, "RequestDuration",
		    REQUEST_DURATION);

	    PreferredLanguage = p.getProperty("PreferredLanguage",
		    PreferredLanguage);

	    ContextServiceLimit = getProperty(p, "ContextServiceLimit",
		    ContextServiceLimit);

	    loadProperty(p, "FavoriteServicesNames");

	    inputStream.close();

	    Log.i("EkhoTV", "ServiceFile: " + SampleClass);
	} catch (IOException e) {
	    Log.e("EkhoTV", "Could not load properties from file " + filename);
	} catch (Exception e) {
	    Log.e("EkhoTV", "Could not load properties from file ");
	    e.printStackTrace();
	}
    }

    private static long toLong(String str) {
	if (str == null || str.length() == 0) {
	    return 0;
	}

	try {
	    return Long.parseLong(str);
	} catch (Exception e) {
	    ;
	}
	return 0;
    }

    private static long getProperty(Properties p, String key, long defaultValue) {
	String str = p.getProperty(key);
	return (str == null) ? defaultValue : toLong(str);
    }

    private static int getProperty(Properties p, String key, int defaultValue) {
	String str = p.getProperty(key);
	return (str == null) ? defaultValue : (int) toLong(str);
    }

    private static void loadProperty(Properties p, String key) {
	String str = p.getProperty(key);
	if (str == null)
	    return;

	String strs[] = SIEmulator.toStrings(str, ",");
	if (strs == null || strs.length == 0)
	    return;

	for (int i = 0; i < strs.length; i++) {
	    String name = strs[i];
	    String names = p.getProperty("List-" + name);
	    if (names == null)
		continue;

	    SIManagerImpl.putFavoriteServices(name,
		    SIEmulator.toStrings(names, ","));
	}
    }
}
