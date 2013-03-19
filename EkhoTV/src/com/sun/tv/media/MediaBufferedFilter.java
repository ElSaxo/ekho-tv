/*
 * @(#)MediaBufferedFilter.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import com.sun.tv.media.util.DataBufQueue;

/**
 * MediaBufferedFilter It extends MediaFilter with the functionality to maintain
 * its own buffer. When getContainer() is called, it first invokes
 * getContainer() to the down stream renderer, keeps the container in its
 * requisition list. Then returns its own buffer to the upstream node. For
 * putData(), it gets a container from its container requisition list. processes
 * the incoming data, fill in the result to the container, then put that
 * container back to the down stream renderer.
 * 
 * @version 1.15, 98/03/28
 */
abstract public class MediaBufferedFilter extends MediaFilter {

    // A queue of buffers for incoming compressed data.
    protected DataBufQueue bufQ;
    protected boolean flushing = false;

    public MediaBufferedFilter() {
	// Allocate a queue of one buffer for the incoming compressed data.
	bufQ = allocBuffers();
    }

    /**
     * The subclass need to allocate buffers with the
     * javax.media.util.DataBufQueue, for example:
     * 
     * DataBufQueue queue = new DataBufQueue(NumBuffer); for (int i = 0; i <
     * NumBuffer; i++) queue.addNewBuffer(new AudData(new byte[bufferSize],
     * bufferSize)); return queue;
     * 
     * The above code creates a queue with NumBuffer AudData, each with
     * BufferSize bytes in size.
     */
    abstract protected DataBufQueue allocBuffers();

    /**
     * Returns a free buffer. Blocks until there is a free buffer available.
     */
    public Data getContainer(Format format) {
	return bufQ.getFree();
    }

    /**
     * Return a free buffer if I have one, otherwise return null.
     */
    public Data tryGetContainer(Format format) {
	InputConnectable ic = output.connectedTo();
	Data tempData, rendData;

	if ((tempData = (Data) bufQ.tryGetFree()) != null) {
	    if ((rendData = (Data) ic.tryGetContainer()) != null) {
		ic.putContainer(rendData);
		return tempData;
	    } else {
		bufQ.putbackFree(tempData);
	    }
	}
	return null;
    }

    /**
     * Called from the upstream node.
     */
    public void putData(Data inData) {

	flushing = false;

	InputConnectable ic = output.connectedTo();
	Data outData = (Data) ic.getContainer();

	if (flushing) {
	    ic.putContainer(outData);
	    bufQ.putbackFree(inData);
	    flushing = false;
	    return;
	}

	// process the data
	if (processData(inData, outData)) {
	    // and release my own buffer for use by other purpose.
	    bufQ.putbackFree(inData);
	    // Send the decoded frame to the renderer node.
	    ic.putData(outData);
	} else {
	    bufQ.putbackFree(inData);
	    ic.putContainer(outData);
	}
    }

    /**
     * This should get called only if a frame is being dropped.
     */
    public void putContainer(Data data) {
	// I have my container back. Notify any waiting threads.
	bufQ.putbackFree(data);
    }

    /**
     * Implement this to do the real processing.
     */
    abstract protected boolean processData(Data inData, Data outData);

    /**
     * Override MediaFilter's processData. This shouldn't be used. Implement
     * processData(inData, outData) instead.
     */
    protected boolean processData(Data data) {
	return true;
    }

    public void flush() {
	flushing = true;
	synchronized (bufQ) {
	    while (!bufQ.noFilled())
		bufQ.putbackFree(bufQ.getFilled());
	}
	super.flush();
    }

}
