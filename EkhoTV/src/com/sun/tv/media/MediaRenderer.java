/*
 * @(#)MediaRenderer.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import javax.media.EndOfMediaEvent;
import javax.media.StopByRequestEvent;
import javax.media.StopEvent;

import com.sun.tv.media.util.DataBufQueue;

/**
 * MediaRenderer
 * 
 * @version 1.26, 98/03/28
 */
public abstract class MediaRenderer extends MediaNode {

    protected DataBufQueue dataBuf = null;
    protected boolean deviceFetched = false;
    protected Object sync = new Object(); // Used for synchronizing purposes
    protected boolean stopRequested;

    protected void doStart() {
	synchronized (sync) {
	    stopRequested = false;
	}
    }

    public void stop() {
	super.stop();
	synchronized (sync) {
	    stopRequested = true;
	    sendEvent((StopEvent) new StopByRequestEvent(this, Started,
		    Prefetched, getTargetState(), getMediaTime()));
	}
    }

    /**
     * The subclass need to allocate buffers with the
     * javax.media.util.DataBufQueue, for example:
     * 
     * DataBufQueue queue = new DataBufQueue(NumBuffer); for (int i = 0; i <
     * NumBuffer; i++) queue.addNewBuffer(new AudData(BufferSize));
     * setBuffers(queue);
     * 
     * The above code creates a queue with NumBuffer AudData, each with
     * BufferSize bytes in size.
     */
    public abstract void allocBuffers(int size, int chunks);

    /**
     * Set the dataBuf to be the given one.
     */
    public void setBuffers(DataBufQueue buf) {
	dataBuf = buf;
    }

    /**
     * Put all the buffers into the free list
     */
    protected void resetDataBufQueue() {
	if (dataBuf != null) {
	    dataBuf.reset();
	}
    }

    /**
     * Unless there are some shared resources we need to claim, e.g. /dev/audio
     * doPrefetch doesn't need to do anything. Filling of the buffers are taken
     * cared of by the getContainer() and putData() calls. I'm putting empty
     * stub here just to signify that.
     */
    protected boolean doPrefetch() {
	return true;
    }

    /**
     * This is called when the prefetch on the device is done. We also need to
     * check to see if the buffer is already filled before we send the
     * PrefetchCompleteEvent.
     */
    protected synchronized void completePrefetch() {
	if ((dataBuf != null) && dataBuf.allFilled()) {
	    super.completePrefetch();
	}
	deviceFetched = true;
    }

    /**
     * Called when the prefetch() is aborted, i.e. deallocate() was called while
     * prefetching. Release all resources claimed previously by the prefetch
     * call.
     */
    protected abstract void abortPrefetch();

    /**
     * Invoke as a callback when there is some data to process.
     */
    public abstract boolean processData(Data data);

    /**
     * Return a container for the upstream node. This is a blocking call when
     * there is no more container to get. InputConnectable.getContainer() should
     * actually return this.
     */
    public/* synchronized */Data getContainer(Format format) {
	return dataBuf.getFree();
    }

    // Calls the above function with a null parameter
    public Data getContainer() {
	return getContainer(null);
    }

    /**
     * Return a container for the upstream node. This is a non-blocking call it
     * returns null when the buffers are full.
     * InputConnectable.tryGetContainer() should actually return this.
     */
    public/* synchronized */Data tryGetContainer(Format format) {
	return dataBuf.tryGetFree();
    }

    // Calls the above function with a null parameter
    public Data tryGetContainer() {
	return tryGetContainer(null);
    }

    /**
     * Put back an unused buffer to the node.
     */
    public/* synchronized */void putContainer(Data data) {
	dataBuf.putbackFree(data);
    }

    /**
     * Remove a buffer from the list of existing buffers.
     */
    protected Data removeOldContainer() {
	return dataBuf.removeOldBuffer();
    }

    /**
     * Add a new buffer to the buffer list. It cannot exceed the originally
     * allocated number of buffers.
     */
    protected void addNewContainer(Data data) {
	dataBuf.addNewBuffer(data);
    }

    /**
     * Put back the data from the upstream node. InputConnectable.putData()
     * should actually invoke this. This is a non-blocking call unless when the
     * buffer is already full. But that will never happen since if the upstream
     * node were able to get a container, that means the buffer wouldn't be full
     * then!
     */
    public/* synchronized */void putData(Data data) {
	dataBuf.putbackFilled(data);
	if (state == Prefetching) {
	    if (dataBuf.allFilled() && deviceFetched) {
		super.completePrefetch();
	    }
	}
    }

    /**
     * Call this to process the data put by the upstream node. It checks the
     * buffer queue to see if there's data there. If not, it will block and
     * wait. Otherwise, it will call processData(Data) so your implementation of
     * renderer should implement processData() to do the real work. This should
     * be called from a thread. You can set up a thread with an infinite while
     * loop so it will process the incoming data as soon as it arrives: e.g:
     * while (1) processPutData(); You should be able to also call it from a
     * TimerThread and process data only when it wakes up from the timer.
     */
    public/* synchronized */void processPutData() {
	Data data = dataBuf.getFilled();

	if (data == null) // abort condition
	    return;

	if (processData(data)) { // End of Media ?
	    synchronized (sync) {
		if (!stopRequested) {
		    super.stop();
		    sendEvent(new EndOfMediaEvent(this, Started, Prefetched,
			    getTargetState(), getMediaTime()));
		}
	    }
	}
	dataBuf.putbackFree(data);
    }

    public void flush() {
	synchronized (dataBuf) {
	    while (!dataBuf.noFilled())
		dataBuf.putbackFree(dataBuf.getFilled());
	}
	super.flush();
    }

}
