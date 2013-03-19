/*
 * @(#)MediaNode.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import javax.media.Control;
import javax.media.Controller;

import com.sun.tv.media.util.ConnectableRegistry;

/**
 * MediaNode Implements a basic JMF node (i.e. implements Controller and
 * MediaProcessor). It inherits from MediaClock and implements the methods from
 * MediaProcessor.
 * 
 * @version 1.9, 98/03/28
 */
abstract public class MediaNode extends MediaController implements
	MediaProcessor {

    // All input and output connectables of this node should be registered
    // inorder for the super classes to function correctly.
    // Use registerInput() and registerOutput() to do the work.
    protected ConnectableRegistry inputsRegistry;
    protected ConnectableRegistry outputsRegistry;

    public MediaNode() {
	inputsRegistry = new ConnectableRegistry();
	outputsRegistry = new ConnectableRegistry();
    }

    /**
     * Return a list of processor controls.
     */
    abstract public Control[] getControls();

    /**
     * How far in advance of presentation does this media node need a frame of
     * data.
     */
    public long getLatency() {
	return MediaProcessor.LATENCY_UNKNOWN;
    }

    /**
     * Return an array of strings containing this media node's input port names.
     */
    public String[] listInputs() {
	return inputsRegistry.getNames();
    }

    /**
     * Return an array of strings containing this media node's output port
     * names.
     */
    public String[] listOutputs() {
	return outputsRegistry.getNames();
    }

    /**
     * Return the input connectible given the port name.
     */
    public InputConnectable getInputPort(String portName) {
	return (InputConnectable) inputsRegistry.getConnectable(portName);
    }

    /**
     * Return the output connectible given the port name.
     */
    public OutputConnectable getOutputPort(String portName) {
	return (OutputConnectable) outputsRegistry.getConnectable(portName);
    }

    /**
     * For each of the inputConnectables to this node, it needs to be registered
     * with this function.
     */
    protected void registerInput(String name, InputConnectable in) {
	inputsRegistry.register(name, in);
    }

    /**
     * For each of the outputConnectables from this node, it needs to be
     * registered with this function.
     */
    protected void registerOutput(String name, OutputConnectable out) {
	outputsRegistry.register(name, out);
    }

    /**
     * Causes a flush to itself and all the nodes downstream.
     */
    public void flush() {
	if (getState() == Started) {
	    // throw new
	    // ClockStartedError("flush() cannot be used on an already started player");
	    // Instead throwing an error, we'll just make it a no-op for
	    // flush() in the started state.
	    return;
	}

	// propergate the flush downstream.
	for (int i = 0; i < outputsRegistry.size(); i++) {
	    InputConnectable in = ((OutputConnectable) outputsRegistry
		    .getConnectable(i)).connectedTo();
	    if (in != null)
		in.flush();
	}
    }

    /**
     * Implement the getDownStreamController abstract method required by
     * MediaController. To do that, we query the output connectables of this
     * node for the input connectables they are connected to. Then we get the
     * MediaProcessor from the inputConnectables, type-cast them to Controllers
     * if possible, return an array of all these controllers. It looks
     * inefficient. But since the number of outputs per node should be small, 1
     * or 2 normally, this shouldn't be too expensive. The subclass can also
     * re-implement this method to make it more efficient by for example,
     * hardcoding the connectables into this function.
     */
    protected Controller[] getDownStreamControllers() {
	if (outputsRegistry.size() == 0)
	    return null;
	Controller[] controllers = new Controller[outputsRegistry.size()];
	for (int i = 0; i < outputsRegistry.size(); i++) {
	    InputConnectable in = ((OutputConnectable) outputsRegistry
		    .getConnectable(i)).connectedTo();
	    if (in != null) {
		MediaProcessor mp = in.getMediaProcessor();
		if (mp instanceof Controller) {
		    controllers[i] = (Controller) mp;
		}
	    }
	}
	return controllers;
    }

}
