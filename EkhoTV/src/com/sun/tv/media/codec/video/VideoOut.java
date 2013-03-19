/*
 * @(#)VideoOut.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.codec.video;

import com.sun.tv.media.Format;
import com.sun.tv.media.InputConnectable;
import com.sun.tv.media.MediaFilter;
import com.sun.tv.media.MediaProcessor;
import com.sun.tv.media.OutputConnectable;
import com.sun.tv.media.format.video.VideoFormat;

/**
 * VideoOut An output connectable for JPEG and H261 decoder nodes.
 * 
 * @version 1.7 98/03/28
 */
class VideoOut implements OutputConnectable {

    protected MediaFilter decoder;
    protected VideoFormat format;
    protected InputConnectable nextIn;

    public VideoOut(MediaFilter d) {
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

    public InputConnectable connectedTo() {
	return nextIn;
    }

    public MediaProcessor getMediaProcessor() {
	return decoder;
    }

    public void connectTo(InputConnectable port) {
	port.connectTo(this);
	nextIn = port;
    }
}
