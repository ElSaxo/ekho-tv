/*
 * %W% %E%
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.util;

import java.util.Locale;

//A.D.import com.sun.tv.media.BuildInfo;

/**
 * This class is a part of the porting layer implementation for JavaTV. This
 * class specifies the resource bundle class, i.e. class that contains locale
 * specific objects.
 */

public class JMFI18N {
    private static java.util.ResourceBundle bundle = null;

    public static synchronized String getResource(String key) {

	Locale currentLocale = java.util.Locale.getDefault();
	String value;

	if (bundle == null) {
	    try {
		/*
		 * A.D. if (BuildInfo.usePureJava()) { bundle = new
		 * com.sun.tv.media.util.locale.JMFProps(); } else { A.D.
		 */bundle = java.util.ResourceBundle.getBundle(
			"com.sun.tv.media.util.locale.JMFProps", currentLocale);
		// A.D. }
	    } catch (java.util.MissingResourceException e) {
		bundle = new com.sun.tv.media.util.locale.JMFProps();
	    }
	}

	try {
	    value = (String) bundle.getObject(key);
	} catch (java.util.MissingResourceException e) {
	    value = null;
	}

	return value;
    }
}
