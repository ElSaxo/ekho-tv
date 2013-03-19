/*
 * @(#)AudioPlayer.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package sun.audio;

import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

import android.util.Log;

/**
 * This class provides an interface to play audio streams.
 * 
 * To play an audio stream use:
 * 
 * <pre>
 * AudioPlayer.player.start(audiostream);
 * </pre>
 * 
 * To stop playing an audio stream use:
 * 
 * <pre>
 * AudioPlayer.player.stop(audiostream);
 * </pre>
 * 
 * To play an audio stream from a URL use:
 * 
 * <pre>
 * AudioStream audiostream = new AudioStream(url.openStream());
 * AudioPlayer.player.start(audiostream);
 * </pre>
 * 
 * To play a continuous sound you first have to create an AudioData instance and
 * use it to construct a ContinuousAudioDataStream. For example:
 * 
 * <pre>
 * AudoData data = new AudioStream(url.openStream()).getData();
 * ContinuousAudioDataStream audiostream = new ContinuousAudioDataStream(data);
 * AudioPlayer.player.stop(audiostream);
 * </pre>
 * 
 * @see AudioData
 * @see AudioDataStream
 * @see AudioDevice
 * @see AudioStream
 * @author Arthur van Hoff, Thomas Ball
 * @version 1.37, 08/19/02
 */
public class AudioPlayer extends Thread {
    private AudioDevice devAudio;
    /**
     * The default audio player. This audio player is initialized automatically.
     */
    public static final AudioPlayer player = getAudioPlayer();

    private static ThreadGroup getAudioThreadGroup() {
	ThreadGroup g = currentThread().getThreadGroup();
	while ((g.getParent() != null) && (g.getParent().getParent() != null)) {
	    g = g.getParent();
	}
	return g;
    }

    /**
     * Create an AudioPlayer thread in a privileged block.
     */
    private static AudioPlayer getAudioPlayer() {
	return (AudioPlayer) AccessController
		.doPrivileged(new PrivilegedAction() {
		    public Object run() {
			return new AudioPlayer();
		    }
		});
    }

    /**
     * Construct an AudioPlayer.
     */
    private AudioPlayer() {
	super(getAudioThreadGroup(), "Audio Player");
	devAudio = AudioDevice.device;
	setPriority(MAX_PRIORITY);
	setDaemon(true);
	start();
    }

    /**
     * Start playing a stream. The stream will continue to play until the stream
     * runs out of data, or it is stopped.
     * 
     * @see AudioPlayer#stop
     */
    public synchronized void start(InputStream in) {
	devAudio.openChannel(in);
	notify();
    }

    /**
     * Stop playing a stream. The stream will stop playing, nothing happens if
     * the stream wasn't playing in the first place.
     * 
     * @see AudioPlayer#start
     */
    public synchronized void stop(InputStream in) {
	devAudio.closeChannel(in);
    }

    /**
     * Main mixing loop. This is called automatically when the AudioPlayer is
     * created.
     */
    public void run() {
	devAudio.open();
	devAudio.play();
	devAudio.close();
	Log.e("EkhoTV", "audio player exit");
    }
}
