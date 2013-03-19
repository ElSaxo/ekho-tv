/*
 * @(#)VideoIn.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.codec.video;

import com.sun.tv.media.Data;
import com.sun.tv.media.Format;
import com.sun.tv.media.InputConnectable;
import com.sun.tv.media.MediaFilter;
import com.sun.tv.media.MediaProcessor;
import com.sun.tv.media.OutputConnectable;
import com.sun.tv.media.VidData;
import com.sun.tv.media.format.video.VideoFormat;

/**
 * VideoIn An input connectable for JPEG and H261 decoder nodes.
 * 
 * @version 1.12 98/03/28
 */
class VideoIn implements InputConnectable {

    protected MediaFilter decoder;
    protected VideoFormat format;
    protected OutputConnectable prevOut;

    VideoIn(MediaFilter d) {
	decoder = d;

	// need to set it to JPEG.
	// [ivg 12/13/96]
	format = new VideoFormat();
    }

    public Format[] listFormats() {
	Format f[] = new Format[1];
	f[0] = format;
	return f;
    }

    public void setFormat(Format f) {
    }

    public Format getFormat() {
	return format;
    }

    public OutputConnectable connectedTo() {
	return prevOut;
    }

    public void connectTo(OutputConnectable port) {
	prevOut = port;
    }

    public MediaProcessor getMediaProcessor() {
	return decoder;
    }

    public boolean tryPutData(Object obj) {
	return false;
    }

    public void putData(Object obj) {
	decoder.putData((VidData) obj);
    }

    public Object tryGetContainer() {
	return tryGetContainer(null);
    }

    public Object tryGetContainer(Format f) {
	return decoder.tryGetContainer(f);
    }

    public Object getContainer() {
	return getContainer(null);
    }

    public Object getContainer(Format f) {
	return decoder.getContainer(f);
    }

    public void putContainer(Object obj) {
	decoder.putContainer((Data) obj);
    }

    // $$$$ Is this a good place? (babu)
    public void flush() {
	// Log.e("EkhoTV", "In flush " + this);
	decoder.flush();
    }
}
