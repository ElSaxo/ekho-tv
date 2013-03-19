/*
 * @(#)XletLoader.java	1.12 08/09/15
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 */

package com.sun.tv;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

/**
 * XletLoader:
 * 
 * Assume we have a carousel string that provides a path to get a carousel. We
 * then read in something of a File style and generate a FileInputStream from
 * this path.
 * 
 * The carousel String might turn to a DSMCCObject later.
 * 
 */

public class XletLoader extends URLClassLoader {
    private String myCarousel = null;

    protected XletLoader(String carousel, String[] paths)
	    throws SecurityException {
	super(getUrls(carousel, paths));

	// more security check, access control go here etc.
	myCarousel = carousel;
    }

    private static URL[] getUrls(String carousel, String[] paths) {
	if (paths == null || paths.length == 0) {
	    return new URL[0];
	}

	Vector v = new Vector();
	for (int i = 0; i < paths.length; i++) {
	    URL url = null;
	    String path = paths[i];
	    // First try creating url from a path string directly
	    try {
		url = new URL(path);
	    } catch (MalformedURLException e) {
	    }

	    // Second, try to convert path into a url format
	    if (url == null) {
		if (carousel != null && carousel.length() > 0) {
		    if (!carousel.endsWith(File.separator)) {
			path = carousel + File.separator + path;
		    } else {
			path = carousel + path;
		    }
		}
		try {
		    url = new File(path).toURL();
		} catch (MalformedURLException e) {
		}
	    }

	    if (url == null) {
		System.err
			.println("XletClassLoader: Warning, failed to create URL from "
				+ path);
	    } else {
		v.add(url);
	    }
	}

	return (URL[]) v.toArray(new URL[v.size()]);
    }
}
