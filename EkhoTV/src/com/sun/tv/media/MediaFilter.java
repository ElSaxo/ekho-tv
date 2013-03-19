/*
 * @(#)MediaFilter.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import javax.media.Control;

/**
 * MediaFilter Implements a basic processor node that doesn't contain any timing
 * info. e.g. a CODEC node. A filter should have only one input and one output
 * connectable. This simple filter class does not implement any buffering. Hence
 * when there is a putData() call, the node will process the data immediately
 * and the call will be blocked until the processing is completed. There should
 * be a MediaBufferredFilter class to do the more sophisticated work.
 * 
 * @version 1.12, 98/03/28
 */
public abstract class MediaFilter implements MediaProcessor {

    // Remember to initialize these in the subclass!
    // Use registerInput and registerOutput to do that.
    protected InputConnectable input;
    protected OutputConnectable output;
    protected String inputName;
    protected String outputName;

    /**
     * Return a list of processor controls.
     */
    abstract public Control[] getControls();

    /**
     * Invoke as a callback when there is some data to process.
     */
    abstract protected boolean processData(Data data);

    /**
     * How far in advance of presentation does this media node need a frame of
     * data.
     */
    public long getLatency() {
	return MediaProcessor.LATENCY_UNKNOWN;
    }

    /**
     * For each of the inputConnectables to this node, it needs to be registered
     * with this function.
     */
    protected void registerInput(String name, InputConnectable in) {
	input = in;
	inputName = name;
    }

    /**
     * For each of the outputConnectables from this node, it needs to be
     * registered with this function.
     */
    protected void registerOutput(String name, OutputConnectable out) {
	output = out;
	outputName = name;
    }

    /**
     * Return an array of strings containing this media node's input port names.
     */
    public String[] listInputs() {
	String nms[] = new String[1];
	nms[0] = inputName;
	return nms;
    }

    /**
     * Return an array of strings containing this media node's output port
     * names.
     */
    public String[] listOutputs() {
	String nms[] = new String[1];
	nms[0] = outputName;
	return nms;
    }

    /**
     * Set the scaling factor.
     */
    public float setScale(float s) {
	return (float) 1.0;
    }

    /**
     * Get the current scaling factor.
     */
    public float getScale() {
	return (float) 1.0;
    }

    /**
     * Return the max scaling factor the filter can handle.
     */
    public float getMaxScale() {
	return (float) 1.0;
    }

    /**
     * Return the min scaling factor the filter can handle.
     */
    public float getMinScale() {
	return (float) 1.0;
    }

    /**
     * Return the input connectible given the port name.
     */
    public InputConnectable getInputPort(String portName) {
	if (inputName.equals(portName))
	    return input;
	else
	    return null;
    }

    /**
     * Return the output connectible given the port name.
     */
    public OutputConnectable getOutputPort(String portName) {
	if (outputName.equals(portName))
	    return output;
	else
	    return null;
    }

    /**
     * Return a container for the upstream node. This is a blocking call when
     * there is no more container to get. InputConnectable.getContainer() should
     * actually return this.
     */
    public Data getContainer(Format format) {
	InputConnectable ic = output.connectedTo();
	return (Data) ic.getContainer(format);
    }

    /**
     * Return a container for the upstream node. This is a non-blocking call it
     * returns null when the buffers are full.
     * InputConnectable.tryGetContainer() should actually return this.
     */
    public Data tryGetContainer(Format format) {
	InputConnectable ic = output.connectedTo();
	return (Data) ic.tryGetContainer(format);
    }

    /**
     * Put back an unused container to the downstream node.
     */
    public void putContainer(Data data) {
	InputConnectable ic = output.connectedTo();
	ic.putContainer(data);
    }

    /**
     * Put back the data from the upstream node. InputConnectable.putData()
     * should actually invoke this. This is a blocking call.
     */
    public synchronized void putData(Data data) {
	processData(data);
	InputConnectable ic = output.connectedTo();
	ic.putData(data);
    }

    /**
     * Causes a flush to itself and the connected node downstream.
     */
    public void flush() {
	InputConnectable ic = output.connectedTo();
	if (ic != null)
	    ic.flush();
    }

}
