/*
 * @(#)AudioOut.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media.codec.audio;

import com.sun.tv.media.Format;
import com.sun.tv.media.InputConnectable;
import com.sun.tv.media.MediaFilter;
import com.sun.tv.media.MediaProcessor;
import com.sun.tv.media.OutputConnectable;
import com.sun.tv.media.format.audio.AudioFormat;

/**
 * AudioOut
 * 
 * @version 1.4 97/02/07
 */
class AudioOut implements OutputConnectable {

    protected MediaFilter decoder;
    protected AudioFormat format;
    protected InputConnectable nextIn;

    public AudioOut(MediaFilter d) {
	decoder = d;
	format = new AudioFormat();
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
