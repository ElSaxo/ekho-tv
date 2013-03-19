/*
 * @(#)AudioMixer.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.jmf.audio;

import javax.media.TimeBase;

import android.util.Log;

import com.sun.tv.media.SystemTimeBase;
import com.sun.tv.media.renderer.audio.AudioRenderer;

public class AudioMixer {
    private static AudioMixer audioMixer;
    private static TimeBase mixerTimeBase = null;
    private static boolean grabbed = false;

    static {
	audioMixer = new AudioMixer();
    }

    private AudioMixer() {

	if (!AudioRenderer.useJavaSound())
	    mixerTimeBase = new SystemTimeBase();
	else
	    mixerTimeBase = null;// new MixerTimeBase();
    }

    public static TimeBase getMixerTimeBase() {
	// Log.e("EkhoTV", "AudioMixer: getMixerTimeBase is " + mixerTimeBase +
	// ": " + mixerTimeBase.getTime());
	return mixerTimeBase;
    }

    public static synchronized boolean grabDevice(Object o) {

	// If using pure Java, we can't use the JavaSound engine.
	// We'll backup to using sun.audio.

	if (!AudioRenderer.useJavaSound()) {

	    if (!sun.audio.AudioPlayer.player.isAlive()) {
		Log.e("EkhoTV", "Audio device is busy");
		return false;
	    }
	    if (!SunAudioMixer.grabDevice(o)) {
		Log.e("EkhoTV", "Audio device is busy");
		return false;
	    }

	    setGrabbedTrue();
	    return true;
	}

	// Grab JavaSound.

	// Log.e("EkhoTV", "grabDevice: mixer is " + mixer);

	// boolean returnCode =
	// ((MixerTimeBase)mixerTimeBase).grabDevice(o,grabbed);
	// if (returnCode) {
	// setGrabbedTrue();
	// }
	return false;// returnCode;

    }

    public static synchronized void doneUsingAudioDevice(Object o) {

	if (!AudioRenderer.useJavaSound()) {
	    setGrabbedFalse();
	    return;
	}

	// Log.e("EkhoTV", "In doneUsingAudioDevice");
	// Log.e("EkhoTV", "list Of Users before: " + list);

	// boolean grabState =
	// ((MixerTimeBase)mixerTimeBase).doneUsingAudioDevice(o);

	// if (false == grabState) {
	// setGrabbedFalse();
	// }

    }

    public static boolean isGrabbed() {
	return grabbed;
    }

    public long getSamplesPlayed() {
	if (!AudioRenderer.useJavaSound())
	    return 0L;
	else {
	    return 0L;// ((MixerTimeBase)mixerTimeBase).getSamplesPlayed();
	}

    }

    private static void setGrabbedTrue() {
	grabbed = true;
    }

    private static void setGrabbedFalse() {
	grabbed = false;
    }

}
