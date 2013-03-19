/*
 * @(#)URLPullSourceStream.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.protocol;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.media.protocol.ContentDescriptor;

import com.sun.tv.media.MediaPullSourceStream;

public class URLPullSourceStream extends MediaPullSourceStream {

    private long whereAmI; // current stream pointer in bytes

    // Debugging variables
    public boolean DEBUG = false;

    public URLPullSourceStream(InputStream i, ContentDescriptor t, URL u,
	    long length) {
	super(i, t, u, length);
	PRINT_DEBUG_MSG("Create URLPullSourceStream");
	whereAmI = 0L;
    }

    public long seek(long where) {

	PRINT_DEBUG_MSG("$$$$$$$ URLPullSourceStream seek to " + where);
	PRINT_DEBUG_MSG("whereAmI:  " + whereAmI);

	URLConnection UrlC;
	Object object;

	long goTo = where - whereAmI;
	long actual = 0L;

	if (whereAmI == -1)
	    goTo = -1; // End of Stream and force it to reopen url.

	if (goTo == 0L)
	    return whereAmI;

	PRINT_DEBUG_MSG("goTo = " + goTo);

	try {
	    if (goTo < 0) { // backward
		CloserThread ct = new CloserThread(stream);
		ct.start();
		// try {
		// JMFSecurity.enablePrivilege.invoke(
		// JMFSecurity.privilegeManager,
		// JMFSecurity.connectArgs);
		// } catch (Exception e) {}

		UrlC = url.openConnection();
		object = UrlC.getInputStream();

		if (!(object instanceof InputStream)) {
		    return whereAmI;
		}

		// Get the length of the stream if known
		contentLength = UrlC.getContentLength();
		if (contentLength <= 0)
		    contentLength = (int) LENGTH_UNKNOWN;

		stream = (InputStream) object;
		eosReached = false;
		whereAmI = 0L;
		actual = skip(where);
		PRINT_DEBUG_MSG("B: actual skip = " + actual);
	    } else { // forward
		actual = skip(goTo);
		PRINT_DEBUG_MSG("F: actual skip = " + actual);
	    }

	} catch (FileNotFoundException e) {
	    // e.printStackTrace();
	    whereAmI = -2L;
	} catch (IOException e) {
	    // e.printStackTrace();
	    whereAmI = -3L;
	}
	return whereAmI;
    }

    public long tell() {
	return whereAmI;
    }

    public int read(byte[] buffer, int offset, int length) throws IOException {
	int byteRead = 0;

	if (whereAmI == -1)
	    return -1;

	byteRead = super.read(buffer, offset, length);

	if (byteRead != -1)
	    whereAmI += byteRead;
	else
	    whereAmI = byteRead;
	return byteRead;
    }

    public void close() {
	try {
	    if (stream != null)
		stream.close();
	} catch (IOException e) {
	}
    }

    public long skip(long distance) throws IOException {
	int bytesRead = 0;
	int totalRead = 0;
	byte Buffer[];
	int request;
	int totalByteSkip = (int) distance;

	// If the stream is a BufferedInputStream, we can use skip() to do the
	// skipping
	if (stream != null && stream instanceof BufferedInputStream) {
	    // We might need to skip more than once
	    while (totalRead != totalByteSkip) {
		// No. of remaining bytes to skip
		request = totalByteSkip - totalRead;

		// Are we already at end of stream?
		if (whereAmI == -1)
		    bytesRead = -1;
		else
		    bytesRead = (int) ((BufferedInputStream) stream)
			    .skip((long) request);
		// Is this correct if we've hit end of stream?
		totalRead += bytesRead;

		// Update whereAmI and eosReached (if EOS)
		if (bytesRead == -1) {
		    whereAmI = -1;
		    eosReached = true;
		    break;
		} else {
		    whereAmI += bytesRead;
		}
	    }
	} else {
	    if (totalByteSkip > 2048)
		request = 2048;
	    else
		request = totalByteSkip;

	    Buffer = new byte[request];

	    while (totalRead != totalByteSkip) {
		bytesRead = read(Buffer, 0, request);
		totalRead += bytesRead;

		if (bytesRead == -1)
		    break;

		if ((totalByteSkip - totalRead) < 2048)
		    request = totalByteSkip - totalRead;
	    }
	}
	return totalRead;
    }

    //
    // Debugging tools
    //
    public void PRINT_DEBUG_MSG(String str) {
	if (DEBUG)
	    System.err.println(str);
    }

    class CloserThread extends Thread {
	InputStream s;

	CloserThread(InputStream s) {
	    this.s = s;
	}

	public void run() {
	    try {
		s.close();
	    } catch (Exception e) {
	    }
	}
    }

}
