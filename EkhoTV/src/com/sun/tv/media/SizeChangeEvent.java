/*
 * @(#)SizeChangeEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import javax.media.Controller;
import javax.media.ControllerEvent;

/**
 * Event which indicates that the input video has changed in size and the video
 * renderer needs to be resized to specified size. Also includes the scale to
 * which the video is going to be zoomed.
 */
public class SizeChangeEvent extends ControllerEvent {

    protected int width;
    protected int height;
    protected float scale;

    public SizeChangeEvent(Controller from, int width, int height, float scale) {
	super(from);
	this.width = width;
	this.height = height;
	this.scale = scale;
    }

    public int getWidth() {
	return width;
    }

    public int getHeight() {
	return height;
    }

    public float getScale() {
	return scale;
    }
}
