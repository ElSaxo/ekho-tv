package com.sun.tv.media.renderer.video;

import com.sun.tv.media.Format;
import com.sun.tv.media.InputConnectable;
import com.sun.tv.media.MediaProcessor;
import com.sun.tv.media.MediaRenderer;
import com.sun.tv.media.OutputConnectable;
import com.sun.tv.media.VidData;

/*
 * @(#)VideoIn.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
public class VideoIn implements InputConnectable {

    protected MediaRenderer renderer;
    protected OutputConnectable prevOut;

    VideoIn(MediaRenderer r) {
	renderer = r;
    }

    public Format[] listFormats() {
	return null;
    }

    public void setFormat(Format f) {
    }

    public Format getFormat() {
	return null;
    }

    public OutputConnectable connectedTo() {
	return prevOut;
    }

    public void connectTo(OutputConnectable port) {
	prevOut = port;
    }

    public MediaProcessor getMediaProcessor() {
	return renderer;
    }

    public boolean tryPutData(Object obj) {
	return false;
    }

    public void putData(Object obj) {
	renderer.putData((VidData) obj);
    }

    public Object tryGetContainer(Format format) {
	return renderer.tryGetContainer(format);
    }

    public Object tryGetContainer() {
	return tryGetContainer(null);
    }

    public Object getContainer(Format format) {
	return renderer.getContainer(format);
    }

    public Object getContainer() {
	return getContainer(null);
    }

    public void putContainer(Object obj) {
	renderer.putContainer((VidData) obj);
    }

    public void flush() {
	renderer.flush();
    }

}
