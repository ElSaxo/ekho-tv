/*
 * @(#)SunAudioMixer.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.jmf.audio;

class SunAudioMixer {

    static boolean grabDevice(Object o) {

	if (!sun.audio.AudioPlayer.player.isAlive()) {
	    return false;
	}

	return true;
    }

}
