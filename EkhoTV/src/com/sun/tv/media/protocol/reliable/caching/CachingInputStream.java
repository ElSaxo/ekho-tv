/*
 * @(#)CachingInputStream.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media.protocol.reliable.caching;

import java.io.IOException;

import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullSourceStream;

import android.util.Log;

public abstract class CachingInputStream implements PullSourceStream {

    protected PullSourceStream sourceStream;
    protected ContentDescriptor contentDescriptor;
    protected boolean eosReached;
    protected int bufferSize;

    // Debugging only
    public boolean DEBUG = false;
    public String ID;

    public CachingInputStream(PullSourceStream pss) {
	ID = new String("NONE");
	eosReached = false;
	contentDescriptor = null;
	sourceStream = pss;
	bufferSize = 0;
    }

    public boolean willReadBlock() {
	return true;
    }

    public ContentDescriptor getContentDescriptor() {
	return contentDescriptor;
    }

    public void close() throws IOException {
	// sourceStream.close();
    }

    public boolean endOfStream() {
	return eosReached;
    }

    public int getBufferSize() {
	return bufferSize;
    }

    public abstract void dispose();

    //
    // download method: copy source stream to ring buffer/file
    // return number of bytes are copied.

    public abstract int download() throws IOException;

    // Read Method
    public abstract int read(byte buffer[], int off, int len)
	    throws IOException;

    public abstract void setBufferSize(int lm);

    public abstract long getContentLength();

    public abstract long seek(long l);

    public void newSeekLocation(long offset) {
    }

    /**
     * Return the start offset in the buffer.
     */
    public abstract long getStartAvail();

    /**
     * Return the end offset in the buffer.
     */
    public abstract long getEndAvail();

    /**
     * Returns an zero length array because no controls are supported.
     * 
     * @return a zero length <code>Object</code> array.
     */
    public Object[] getControls() {
	return new Object[0];
    }

    /**
     * Returns <code>null</code> because no controls are implemented.
     * 
     * @return <code>null</code>.
     */
    public Object getControl(String controlName) {
	return null;
    }

    // //////////////////////////////////////////////////////
    // DEBUG tools
    //

    protected void PRINT_DEBUG_MSG(String str) {
	if (DEBUG)
	    Log.e("EkhoTV", str);
    }

    public String getID() {
	return ID;
    }

    public void setID(String id) {
	ID = id;
    }
}
