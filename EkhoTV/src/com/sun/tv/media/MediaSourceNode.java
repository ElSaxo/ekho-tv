/*
 * @(#)MediaSourceNode.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import javax.media.RestartingEvent;
import javax.media.StartEvent;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullSourceStream;

import android.util.Log;

import com.sun.tv.media.codec.DefaultCodecFactory;
import com.sun.tv.media.format.audio.AudioFormat;
import com.sun.tv.media.format.video.VideoFormat;
import com.sun.tv.media.protocol.reliable.caching.CachingInputStream;
import com.sun.tv.media.protocol.reliable.caching.NewRFCachingInputStream;
import com.sun.tv.media.util.JMFProperties;

/**
 * MediaSourceNode
 * 
 * @version 1.25, 98/03/28
 */
public abstract class MediaSourceNode extends MediaNode {

    String cacheFile = null;
    public final static String CACHE_DISABLE = "CACHE DISABLE";
    public final static String RAM_CACHE_ENABLE = "RAM CACHE ENABLE";
    public final static String FILE_CACHE_ENABLE = "FILE CACHE ENABLE";
    static private int cacheSize;

    /**
     * The stub function to perform the steps to prefetch a SourceNode. This is
     * highly dependent on the implementation of the node. In general the
     * function should do something like this for each InputConnectable it is
     * connected to: AudData buf; while ((buf = ic.tryGetContainer()) != null) {
     * readDataIntoBuf(buf); ic.putData(buf); } The ic.tryGetContainer() will
     * return a free buffer if it's stills has one available. When it returns
     * null, it no longer has any freed buffer and the node should consider the
     * prefetching done.
     */
    protected abstract boolean doPrefetch();

    /**
     * Called when the prefetch() is aborted, i.e. deallocate() was called while
     * prefetching. Release all resources claimed previously by the prefetch
     * call.
     */
    protected abstract void abortPrefetch();

    // NOTE: You should not return less than 'length' bytes unless you
    // encounter end of media. This is not a requirement for audio
    // but certainly for video. Therefore, when using FILE caching,
    // if the read from cache would block, a RestartingEvent is thrown
    // and when you have read 'length' bytes a StartEvent is thrown
    /**
     * read the request number of bytes, length, from the pull source stream.
     */
    protected int readFrame(PullSourceStream pss, Data data, int length)
	    throws IOException {
	int bytesRead = 0, totalRead = 0;
	int offset = 0;
	int request = length;
	boolean restart = false;

	while (totalRead != length) {
	    if (pss instanceof NewRFCachingInputStream) {
		NewRFCachingInputStream cis = (NewRFCachingInputStream) pss;
		if (!cis.canReadFromCache()) {
		    if (getState() == Started) {
			if (!restart) {
			    restart = true;
			    super.stop(); // ADDED
			    sendEvent(new RestartingEvent(this, Started,
				    Prefetching, Started, getMediaTime()));
			}
		    }
		    cis.waitUntilCacheReady();
		}
	    }
	    bytesRead = pss.read((byte[]) data.getBuffer(), offset, request);
	    if (bytesRead == -1) {
		if (totalRead == 0)
		    totalRead = -1;
		break;
	    }

	    totalRead += bytesRead;
	    offset = totalRead;
	    request -= bytesRead;
	}

	if (restart) {
	    sendEvent(new StartEvent(this, Prefetched, Started, Started,
		    getMediaTime(), getTimeBase().getTime()));

	}
	return totalRead;
    }

    // final void seek(PullSourceStream pss, long offset) {
    final protected long trySeek(CachingInputStream pss, long offset) {
	if (!(pss instanceof NewRFCachingInputStream))
	    return -1;
	NewRFCachingInputStream cis = (NewRFCachingInputStream) pss;
	boolean restart = false;

	if (!cis.canSeek(offset)) {
	    if (getState() == Started) {
		restart = true;
		super.stop(); // ADDED
		sendEvent(new RestartingEvent(this, Started, Prefetching,
			Started, getMediaTime()));
	    }
	}
	long where = cis.seek(offset);
	if (restart) {
	    sendEvent(new StartEvent(this, Prefetched, Started, Started,
		    getMediaTime(), getTimeBase().getTime()));

	}
	return where;
    }

    //
    // Generate a new file name by combining actual filename
    // + a random number + extension.
    static public String generateFileName(String infile) {

	String filename, ext = null;
	int sepindex = 0;
	Random generator = new Random();
	int dotindex = infile.lastIndexOf('.');
	int suffix = generator.nextInt();

	//
	// if dotindex is not found, it implies extension
	// doesn't exist. Then set the dotindex to the
	// length of the input file, infile.
	if (dotindex != -1)
	    ext = new String(infile.substring(dotindex));
	else
	    dotindex = infile.length();

	sepindex = infile.lastIndexOf(File.separatorChar);
	// some URL's on Wintel use either slash. So should we.
	sepindex = Math.max(infile.lastIndexOf('/'), sepindex);

	//
	// If sepindex equals to -1, the input file name doesn't
	// have a separator. Copy the filename from 0 up to the
	// the extension.
	filename = infile.substring(sepindex + 1, dotindex);

	return (new String(filename + suffix + ext));
    }

    public static boolean canCreateCacheFile(String file) {
	return com.sun.tv.media.util.RingFile.canCreateCacheFile(file);
    }

    /*
     * Determine what kind of cache mechanism is used.
     */
    public String getCacheMechanism(DataSource ds) {
	String localFile;
	URL url;

	if (ds.getLocator() == null)
	    return CACHE_DISABLE;

	try {
	    url = ds.getLocator().getURL();
	} catch (MalformedURLException e) {
	    // Don't know how to cache a non-url locator.
	    return CACHE_DISABLE;
	}

	// If the protocol is "http", try to open a temporary file
	// and make an copy to the local directory, /tmp. If there
	// are any errors encountered, back up to use reopen http
	// protocol scheme.
	if ((url.getProtocol()).equals("http"))
	    localFile = generateFileName(url.getFile());
	else
	    return CACHE_DISABLE;

	String value = JMFProperties.getProperty("cache.use");
	if (value != null && value.equalsIgnoreCase("n"))
	    return CACHE_DISABLE;
	try {
	    cacheSize = Integer.parseInt(JMFProperties
		    .getProperty("cache.limit"));
	} catch (NumberFormatException e) {
	    /*
	     * robsz: What should be the default behavior? Should it be cache /*
	     * disable, should it be some arbitrary number (e.g. 1MB), or /*
	     * should it be unbounded?
	     */
	    cacheSize = 0;
	    return CACHE_DISABLE;
	}
	String fullName = findLocalPath() + getCacheFile(localFile);
	if (com.sun.tv.media.util.RingFile.canCreateCacheFile(fullName)) {
	    return FILE_CACHE_ENABLE;
	} else {
	    return CACHE_DISABLE;
	}
    }

    protected String getCacheFile(String original) {
	if (cacheFile == null) {
	    if (original != null)
		cacheFile = generateFileName(original);
	    else
		cacheFile = null;
	}
	return cacheFile;
    }

    //
    // Retrieve cache.dir from jmf.properties.
    static public String findLocalPath() {
	String value = JMFProperties.getProperty("cache.dir");

	if (value == null)
	    return null;

	if (!(value.endsWith(File.separator)))
	    value = value.concat(File.separator);

	return value;
    }

    static public int getCacheSize() {
	if (cacheSize != 0)
	    return cacheSize;
	else {
	    try {
		cacheSize = Integer.parseInt(JMFProperties
			.getProperty("cache.limit"));
	    } catch (NumberFormatException e) {
		/*
		 * robsz: What should be the default behavior? Should it be
		 * cache /* disable, should it be some arbitrary number (e.g.
		 * 1MB), or /* should it be unbounded?
		 */
		cacheSize = 0;
	    }
	    return cacheSize;
	}
    }

    /*
     * public void setRegionControl(SliderRegionControl rc) { regionControl =
     * rc; }
     */
    public boolean isSupported(Format f) {
	String codec = null;
	boolean isSupport = false;

	if (f instanceof AudioFormat) {
	    // Hard code this for now. It should inquire JavaSound.
	    AudioFormat af = (AudioFormat) f;
	    codec = af.getCodec();

	    if (codec.equals("ulaw") || codec.equals("g711_ulaw")) {
		if (false /* DEBUG */) {
		    Log.e("EkhoTV", "Channels = " + af.getChannels()
			    + "; Sample size =  " + af.getSampleSize());
		}
		return true;
	    }

	    //
	    // raw, twos, linear, "" all meant PCM linear data.
	    // [cania, 12/5/97]
	    if (codec.equals("raw") || codec.equals("twos")
		    || codec.equals("linear") || codec.equals(""))
		return true;

	    isSupport = DefaultCodecFactory.supports("audio." + codec);
	    return isSupport;
	}

	if (f instanceof VideoFormat) {
	    codec = ((VideoFormat) f).getCodec();
	    isSupport = DefaultCodecFactory.supports("video." + codec);
	    return isSupport;
	}
	return isSupport;
    }
}
