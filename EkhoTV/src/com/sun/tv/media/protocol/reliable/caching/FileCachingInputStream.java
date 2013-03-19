/*
 * @(#)FileCachingInputStream.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media.protocol.reliable.caching;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.media.protocol.PullSourceStream;

import android.util.Log;

import com.sun.tv.media.MediaInputStream;

public class FileCachingInputStream extends CachingInputStream {

    private RandomAccessFile supplier = null;
    private RandomAccessFile provider = null;
    private File file = null;
    private boolean first = true;

    private native void nativeDelete(String name);

    static {
	// try {
	// JMFSecurity.loadLibrary("jmutil");
	// } catch (Exception e) {
	// System.err.println("Could not load library");
	// }
    }

    public FileCachingInputStream(PullSourceStream p, String path) {
	super(p);
	try {
	    file = new File(path);
	    if (!file.exists()) {
		supplier = new RandomAccessFile(file, "rw");
	    }
	    provider = new RandomAccessFile(file, "r");
	} catch (IOException e) {
	    Log.e("EkhoTV", "FileCaching ctor: IOException is caught: " + e);
	    supplier = null;
	    provider = null;
	}
    }

    public long getStartAvail() {
	return 0;
    }

    public long getEndAvail() {
	long offset = -1L;
	if (supplier != null) {
	    try {
		offset = supplier.getFilePointer();
	    } catch (IOException e) {
		offset = -1L;
	    }
	}
	return offset;
    }

    public void setBufferSize(int lm) {
	bufferSize = -1;
	if (provider != null)
	    bufferSize = lm;
    }

    private int getAvailable() {
	int available = 0;
	try {
	    available = (int) (provider.length() - provider.getFilePointer());
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return available;
    }

    public int download() throws IOException {
	int bytesRead = 0;
	byte buffer[] = new byte[MediaInputStream.MAX_READ_LIMIT];

	if ((bytesRead = sourceStream.read(buffer, 0, buffer.length)) != -1) {
	    try {
		supplier.write(buffer, 0, bytesRead);
	    } catch (IOException e) {
		eosReached = true;
		supplier.close();
		throw new IOException("write error");
	    }
	} else {
	    PRINT_DEBUG_MSG("#### eosReached ####");
	    eosReached = true;
	    supplier.close();
	}
	buffer = null;
	PRINT_DEBUG_MSG("dnload>> " + bytesRead + " bytes");
	return bytesRead;
    }

    // Read Method
    public synchronized int read(byte buffer[], int off, int len)
	    throws IOException {
	int readBytes = len;
	readBytes = provider.read(buffer, off, len);
	PRINT_DEBUG_MSG("read>> " + readBytes + " bytes");
	return readBytes;
    }

    // set the current pointer position
    public long seek(long pos) {
	long cp = -1L;
	if (first) {
	    try {
		while (pos > provider.length())
		    try {
			wait();
		    } catch (Exception e) {
		    }
	    } catch (Exception e) {
	    }
	    first = false;
	}
	try {
	    provider.seek(pos);
	    cp = provider.getFilePointer();
	} catch (IOException e) {
	    Log.e("EkhoTV", "File: seek: IOException is caught: " + e);
	    cp = -1;
	}

	return cp;
    }

    public long getContentLength() {
	int len = 0;
	try {
	    len = (int) provider.length();
	} catch (IOException e) {
	}
	return len;
    }

    public void dispose() {
	PRINT_DEBUG_MSG("FileCachingInputStream dispose");
	try {
	    if (supplier != null)
		supplier.close();
	    if (provider != null)
		provider.close();
	    if (file != null)
		nativeDelete(file.getAbsolutePath());
	} catch (IOException e) {
	}
	supplier = null;
	provider = null;
    }
}
