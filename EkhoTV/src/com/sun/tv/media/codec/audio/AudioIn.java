/*
 * @(#)AudioIn.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media.codec.audio;

import com.sun.tv.media.AudData;
import com.sun.tv.media.Data;
import com.sun.tv.media.Format;
import com.sun.tv.media.InputConnectable;
import com.sun.tv.media.MediaFilter;
import com.sun.tv.media.MediaProcessor;
import com.sun.tv.media.OutputConnectable;
import com.sun.tv.media.format.audio.AudioFormat;

/**
 * AudioIn
 * 
 * @version 1.4 97/02/07
 */
class AudioIn implements InputConnectable {

    protected MediaFilter decoder;
    protected AudioFormat format;
    protected OutputConnectable prevOut;

    AudioIn(MediaFilter d) {
	// $ System.err.println("codec.audio.AudioIn: decoder: " + d);
	decoder = d;
	format = new AudioFormat();
    }

    public Format[] listFormats() {
	Format f[] = new Format[1];
	f[0] = format;
	return f;
    }

    public void setFormat(Format f) {
	/**
	 * DEBUG System.err.println("codec.audio.AudioIn: setFormat: " + f);
	 * System.err.println("codec.audio.AudioIn: " +
	 * decoder.getOutputPort("AudioOut"));
	 * System.err.println("codec.audio.AudioIn: " +
	 * decoder.getOutputPort("AudioOut").connectedTo());
	 **/

	// Propagate the setFormat downstream
	if (decoder != null)
	    (decoder.getOutputPort("AudioOut").connectedTo()).setFormat(f);

    }

    public Format getFormat() {
	return format;
    }

    public OutputConnectable connectedTo() {
	return prevOut;
    }

    public void connectTo(OutputConnectable port) {
	// $ System.err.println("codec.audio.AudioIn: connectTo: " + port);
	prevOut = port;
    }

    public MediaProcessor getMediaProcessor() {
	return decoder;
    }

    public boolean tryPutData(Object obj) {
	return false;
    }

    public void putData(Object obj) {
	decoder.putData((AudData) obj);
    }

    public Object tryGetContainer() {
	return decoder.tryGetContainer(null);
    }

    public Object tryGetContainer(Format f) {
	return decoder.tryGetContainer(f);
    }

    public Object getContainer() {
	return decoder.getContainer(null);
    }

    public Object getContainer(Format f) {
	return decoder.getContainer(f);
    }

    public void putContainer(Object obj) {
	decoder.putContainer((Data) obj);
    }

    public void flush() {
	decoder.flush();
    }
}
