/*
 * @(#)DataSource.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media.protocol.http;

import java.io.InputStream;
import java.net.URL;

import javax.media.protocol.ContentDescriptor;

import com.sun.tv.media.MediaPullSourceStream;
import com.sun.tv.media.protocol.URLPullSourceStream;

public class DataSource extends com.sun.tv.media.protocol.MediaPullDataSource {

    public MediaPullSourceStream createPullSourceStream(InputStream is,
	    ContentDescriptor type, URL url, long length) {
	return (new URLPullSourceStream(is, type, url, length));
    }

    public Object clone() {
	DataSource dupe = new DataSource();
	dupe.setLocator(getLocator());
	return dupe;
    }

}
