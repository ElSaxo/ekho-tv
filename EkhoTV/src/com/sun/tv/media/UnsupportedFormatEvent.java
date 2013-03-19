/*
 * @(#)UnsupportedFormatEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import javax.media.Controller;
import javax.media.ControllerEvent;

/**
 * Event which indicates that the input format isn't supported by installed
 * decoder.
 */
public class UnsupportedFormatEvent extends ControllerEvent {

    protected Format format;

    public UnsupportedFormatEvent(Controller from, Format f) {
	super(from);
	this.format = f;
    }

    public String toString() {
	String str = null;
	if (format != null) {
	    if (format instanceof MediaFormat) {
		str = new String(((MediaFormat) format).getCodec()
			+ " is not supported.");
	    }
	}
	return str;
    }

    public Format getFormat() {
	return this.format;
    }
}
