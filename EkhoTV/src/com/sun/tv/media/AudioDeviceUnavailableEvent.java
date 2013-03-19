/*
 * @(#)AudioDeviceUnavailableEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import javax.media.Controller;
import javax.media.ControllerEvent;

/**
 * A <code>AudioDeviceUnavailableEvent</code> indicates that the
 * <code>Controller</code> could not fetch the audio device.
 */

public class AudioDeviceUnavailableEvent extends ControllerEvent {

    public AudioDeviceUnavailableEvent(Controller from) {
	super(from);
    }

}
