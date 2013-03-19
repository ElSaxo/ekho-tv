/*
 * @(#)AudioRenderer.java	1.6 08/12/01
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media.renderer.audio;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Vector;

import javax.media.Control;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Duration;
import javax.media.EndOfMediaEvent;
import javax.media.StartEvent;
import javax.media.StopEvent;
import javax.media.Time;
import javax.media.TimeBase;

import android.util.Log;

import com.sun.tv.media.AudData;
import com.sun.tv.media.AudioDeviceUnavailableEvent;
import com.sun.tv.media.Data;
import com.sun.tv.media.InputConnectable;
import com.sun.tv.media.MediaProcessor;
import com.sun.tv.media.MediaRenderer;
import com.sun.tv.media.OutputConnectable;
import com.sun.tv.media.SystemTimeBase;
import com.sun.tv.media.format.audio.AudioFormat;
import com.sun.tv.media.jmf.audio.AudioMixer;
import com.sun.tv.media.jmf.audio.AudioPlay;
import com.sun.tv.media.jmf.audio.JMFAudioFormat;
import com.sun.tv.media.util.DataBufQueue;
import com.sun.tv.media.util.LoopThread;

/**
 * AudioRenderer
 * 
 * @version 1.83, 98/06/08
 */

public class AudioRenderer extends MediaRenderer {
    protected final static String inputNames[] = { "AudioIn" };
    protected InputConnectable audioIn;
    protected OutputConnectable prevOut;
    protected Vector observerList;
    protected AudioPlay audio = null;
    protected LoopThread renderThread = null;
    private static int EOM = -1;
    final private int NumBuffer = 1;
    private AudioTimeBase audioTimeBase = null;
    private boolean skipSound = false;
    private boolean flushing = false;

    private boolean haveOldGainState = false;
    private boolean haveOldMuteState = false;
    private boolean oldMute = false;
    private float oldGain = 0.0f;

    protected AudioFormat currentAudioFormat = null;
    private static boolean fUseJavaSound;
    private static boolean fUseGainControl;
    private static boolean useGainControlIndicator;

    static {
	// Force to use all-java impl
	fUseJavaSound = false;// !BuildInfo.usePureJava();

	/*
	 * fUseJavaSound = false; try {
	 * Class.forName("com.sun.tv.media.jmf.audio.HaeMixer"); fUseJavaSound =
	 * true; }
	 * 
	 * catch (ClassNotFoundException e1) { // the class is missing - no
	 * javasound
	 * 
	 * }
	 * 
	 * catch (NoClassDefFoundError e2) { // the class exist but couldn't be
	 * created Log.e("EkhoTV",
	 * "Error laoding javasound - using sun.audio package" ); }
	 * 
	 * catch (UnsatisfiedLinkError e3) { // native code is missing
	 * System.out
	 * .println("Error laoding javasound - using sun.audio package"); }
	 */

	fUseGainControl = true;
	useGainControlIndicator = false;

    }

    public AudioRenderer() {
	audioIn = new AudioIn(this);
	registerInput("AudioIn", audioIn);

	observerList = null;
	allocBuffers(4 * 1024, 1); // Initial allocation
	audioTimeBase = new AudioTimeBase(this);
    }

    public void allocBuffers(int size, int chunk) {
	// $ Sanity Check - size cannot be negative.
	if (size <= 0)
	    return;

	DataBufQueue queue = new DataBufQueue(chunk);

	// $ Log.e("EkhoTV", "In AR allocBuffers: " + size + ": " + chunk);
	for (int i = 0; i < chunk; i++) {
	    Data data = new AudData(new byte[size], size);
	    queue.addNewBuffer(data);
	}
	setBuffers(queue);
    }

    protected void doClose() {
	if (renderThread != null) {
	    // try {
	    // JMFSecurity.enablePrivilege.invoke(JMFSecurity.privilegeManager,
	    // JMFSecurity.threadArgs);
	    // JMFSecurity.enablePrivilege.invoke(JMFSecurity.privilegeManager,
	    // JMFSecurity.threadGroupArgs);
	    // } catch (Exception e) {}
	    renderThread.kill(); // 6778788
	}

	if (audio != null) {
	    audio.stop();
	}

	AudioMixer.doneUsingAudioDevice(this);
    }

    public TimeBase getAudioTimeBase() {
	return audioTimeBase;
    }

    public Control[] getControls() {
	return new Control[0];
    }

    public long getLatency() {
	return 0;
    }

    public String[] listInputs() {
	return inputNames;
    }

    public String[] listOutputs() {
	return null;
    }

    public InputConnectable getInputPort(String portName) {
	if (inputNames[0].equals(portName))
	    return audioIn;
	else
	    return null;
    }

    public OutputConnectable getOutputPort(String portName) {
	return null;
    }

    public Time getStartLatency() {
	return new Time(0);
    }

    public synchronized void doStop() {
	// Guard this from happening in the middle of handling the EOM.
	if (renderThread != null) {
	    pause();
	    renderThread.blockingPause();
	    // $$$$ renderThread.suspend();
	}
    }

    public void setStopTime(Time time) {
	super.setStopTime(time);
    }

    public void setMediaTime(Time now) {
	super.setMediaTime(now);
    }

    public Time getMediaTime() {
	return super.getMediaTime();
    }

    protected float doSetRate(float f) {
	float oldRate = getRate();
	float newRate = super.doSetRate(f);

	if (oldRate == newRate)
	    return newRate;
	if (newRate != 1.0f)
	    skipSound(true);
	else
	    skipSound(false);
	return newRate;
    }

    public void setGain(float g) {
	haveOldGainState = true;
	oldGain = g;
	if (audio != null)
	    audio.setGain(g);
    }

    public void setMute(boolean m) {
	haveOldMuteState = true;
	oldMute = m;
	if (audio != null)
	    audio.setMute(m);
    }

    //
    // Methods to support the Duration interface
    //
    public Time getDuration() {
	return Duration.DURATION_UNKNOWN;
    }

    protected void connectTo(OutputConnectable c) {
	prevOut = c;
    }

    // $$ New semantics. Return true if EOM buffer was received.
    // Dont send EndOfMediaEvent here. Dont call super.stop()
    public boolean processData(Data obj) {

	if (!(obj instanceof AudData))
	    return false;

	AudData data = (AudData) obj;
	int len;
	int off = 0;
	int remain = data.getLength();

	// System.err.println("AudioRenderer.processData(): data.getLength() = "
	// + remain);

	flushing = false;

	// Handling EOM is critical. We'll guard it as such.
	if (remain == EOM) {
	    handleEOM();
	    return true;
	}

	if (skipSound) {

	    // Fake processing the sound by waiting but without really
	    // sending the bytes to JavaSound.
	    processByWaiting(data);

	} else {

	    // Process the sound as it should be.

	    // Code to handle dynamic audio format change
	    AudioFormat fmt = (AudioFormat) data.getFormat();
	    if ((currentAudioFormat != null)
		    && (currentAudioFormat.match(fmt) == null)) {
		// Audio Format has changed
		/**
		 * DEBUG Log.e("EkhoTV",
		 * "AR: processData: audio format changed from " +
		 * currentAudioFormat + ": to " + fmt);
		 **/

		pause();

		if (audio != null) {
		    audio.stop();
		    audio = null;
		    flush();
		}
	    }
	    currentAudioFormat = fmt;

	    if (audio == null) {
		if (!startAudio((AudioFormat) data.getFormat())) {
		    return false;
		}

		if (haveOldGainState)
		    setGain(oldGain);
		if (haveOldMuteState)
		    setMute(oldMute);
	    }

	    try {
		byte[] dataBuffer = (byte[]) data.getBuffer();

		if (audio.needConversion()) {
		    remain = audio.convertData(dataBuffer, off, remain);
		}

		while (remain > 0 && !flushing) {

		    len = audio.write(dataBuffer, off, remain);
		    off += len;
		    remain -= len;
		}
		data.setLength(0); // Buffer has been consumed.
	    } catch (NullPointerException e) {
		return false;
	    }
	}

	return false; // buffer was not EOM buffer
    }

    private synchronized void handleEOM() {
	drain();
	if (renderThread != null) {
	    renderThread.pause();
	} else {
	    // $$ Log.e("EkhoTV", "$$: renderThread is " + renderThread);
	}
	// TODO: $$ Actually EndOfMediaEvent should be sent after
	// audio is drained.
	setTargetState(Prefetched);
    }

    private void processByWaiting(AudData data) {
	AudioFormat format = (AudioFormat) data.getFormat();

	int sampleRate = format.getSampleRate();
	int sampleSize = format.getSampleSize();
	int channels = format.getChannels();
	int timeToWait;

	timeToWait = data.getLength() * 1000
		/ ((sampleSize / 8) * sampleRate * channels);
	timeToWait = (int) ((float) timeToWait / getRate());
	/*
	 * System.err.println("sampleSize = " + sampleSize + " sampleRate = " +
	 * sampleRate + " channels = " + channels + " timeToWait = " +
	 * timeToWait);
	 */
	try {
	    Thread.currentThread().sleep(timeToWait);
	} catch (Exception e) {
	}
    }

    private void skipSound(boolean skip) {
	if (skip) {
	    pause();
	    flush();
	} else {
	    resume();
	}
	skipSound = skip;
	audioTimeBase.useSystemTime(skip);
    }

    protected boolean doPrefetch() {
	if (!AudioMixer.grabDevice(this)) {
	    // Log.e("EkhoTV", "AR: doPrefetch failed: audio device busy");
	    sendEvent(new AudioDeviceUnavailableEvent(this));
	    return false;
	}
	/*
	 * if (haveOldGainState) setGain(oldGain); if (haveOldMuteState)
	 * setMute(oldMute);
	 */
	return true;
    }

    protected void abortPrefetch() {
	/*
	 * --ivg There's no need to kill the thread.
	 * 
	 * if ( renderThread != null) { try {
	 * JMFSecurity.enablePrivilege.invoke(JMFSecurity.privilegeManager,
	 * JMFSecurity.threadArgs);
	 * JMFSecurity.enablePrivilege.invoke(JMFSecurity.privilegeManager,
	 * JMFSecurity.threadGroupArgs); } catch (Exception e) {}
	 * renderThread.stop(); // Log.e("EkhoTV",
	 * "In AR abortPrefetch: setting renderThread to null" ); renderThread =
	 * null; }
	 */
	if (renderThread != null) {
	    renderThread.pause();
	}
	if (audio != null) {
	    // flush();
	    audio.stop();
	    audio = null;
	}
	deviceFetched = false; // $$? Do you need this
	resetDataBufQueue();
	AudioMixer.doneUsingAudioDevice(this);
    }

    protected boolean doRealize() {
	return true;
    }

    protected void abortRealize() {
    }

    public Data getContainer(com.sun.tv.media.Format format) {
	// System.err.println("AudioRenderer.getContainer(): " + format);
	if (format == null)
	    return dataBuf.getFree();

	if (!(format instanceof AudioFormat)) {
	    return null;
	}

	AudData data = (AudData) dataBuf.getFree();
	return checkBuffer(data, (AudioFormat) format);
    }

    public Data tryGetContainer(com.sun.tv.media.Format format) {
	// System.err.println("AudioRenderer.tryGetContainer(): " + format);
	if (format == null)
	    return dataBuf.tryGetFree();

	if (!(format instanceof AudioFormat)) {
	    return null;
	}

	AudData data;
	if ((data = (AudData) dataBuf.tryGetFree()) == null)
	    return null;

	return checkBuffer(data, (AudioFormat) format);
    }

    protected AudData checkBuffer(AudData data, AudioFormat format) {

	// Log.e("EkhoTV", "AR buffersize, framesize: " +
	// data.getBufferSize() + ": " + format.getFrameSize() );
	if (data.getBufferSize() >= format.getFrameSize())
	    return data;

	AudData newData = new AudData(new byte[format.getFrameSize()],
		format.getFrameSize());
	dataBuf.replaceOldBuffer(data, newData);
	return newData;
    }

    public void syncStart(Time tbt) {
	// This is a fix to minimize a lip sync problem when running inside
	// netscape.
	// There is a considerable delay between when syncStart is called and
	// the StartEvent is received. This delay causes the audio time base
	// to respond not faster enough in switching over to using the audio
	// sample count.
	// The following check try to active the audio sample count
	// (deactivate the system time base) the soonest possible.
	// There could still be a problem with a scheduled syncStart.
	// In that case, the following check would pass through and the
	// delay with the thread creation will again contribute to the
	// lip sync problem.
	if (tbt.getNanoseconds() <= audioTimeBase.getNanoseconds()
		&& getRate() == 1.0f) {
	    audioTimeBase.useSystemTime(false);
	}
	super.syncStart(tbt);
    }

    // This method is called by MediaController in a thread (TimedStartThread)
    protected void doStart() {

	if (renderThread == null) {
	    // try {
	    // JMFSecurity.enablePrivilege.invoke(
	    // JMFSecurity.privilegeManager, JMFSecurity.threadArgs);
	    // JMFSecurity.enablePrivilege.invoke(
	    // JMFSecurity.privilegeManager,
	    // JMFSecurity.threadGroupArgs);
	    // } catch (Exception e) {
	    // }
	    renderThread = new RenderThread(this);
	    renderThread.setName(renderThread.getName() + ": "
		    + getClass().getName());
	    renderThread.start();
	    // Thread is started at creation time
	} else {
	    renderThread.restart();
	}
	super.doStart();
	resume();
    }

    private boolean startAudio(AudioFormat format) {
	if (format == null) {
	    System.err.println("AudioRenderer: ERROR: Unknown AudioFormat");
	    return false;
	}

	int flags = format.getFlags();

	int sampleRate = format.getSampleRate();
	int sampleSize = format.getSampleSize();
	int SamplePerUnit;

	// sampleSize is in bits now
	if (sampleSize < 8) {
	    SamplePerUnit = 1;
	} else {
	    SamplePerUnit = 8;
	    sampleSize /= 8;
	}

	int channels = format.getChannels();
	String encoding = format.getEncoding();

	/*
	 * Log.e("EkhoTV", "sampleRate is " + sampleRate); Log.e("EkhoTV",
	 * "SamplePerUnit is " + SamplePerUnit); Log.e("EkhoTV", "channels is "
	 * + channels); Log.e("EkhoTV", "encoding is " + encoding);
	 * Log.e("EkhoTV", "sampleSize is " + sampleSize);
	 */

	boolean bigEndian = (flags & AudioFormat.FLAG_BIGENDIAN) > 0;
	boolean signed = (flags & AudioFormat.FLAG_SIGNED) > 0;

	/**
	 * { Log.e("EkhoTV",
	 * "We don't get signed info yet. Setting signed to true"); signed =
	 * true; }
	 * 
	 * Log.e("EkhoTV", "bigendian is " + bigEndian); Log.e("EkhoTV",
	 * "signed is " + signed);
	 **/

	JMFAudioFormat audioFormat = new JMFAudioFormat(sampleRate, encoding,
		sampleSize, SamplePerUnit, channels, bigEndian, signed);

	// Create AudioPlay based on the format and the current platform.
	createAudioPlay(audioFormat);

	if (!audio.initialize())
	    return false;

	// we need to reset the audio time base since a new stream is used.
	if (audioTimeBase != null)
	    audioTimeBase.reset();

	return true;
    }

    private void createAudioPlay(JMFAudioFormat format) {
	Class aclass, fclass;
	Class cargs[] = new Class[1];
	Object oargs[] = new Object[1];
	Constructor constructor;

	// Use SunAudioPlay for the pure java version.
	// Otherwise, use JavaSound.
	try {
	    if (!useJavaSound())
		aclass = Class
			.forName("com.sun.tv.media.jmf.audio.SunAudioPlay");
	    else
		aclass = Class.forName("com.sun.tv.media.jmf.audio.AudioPlay");
	} catch (ClassNotFoundException e) {
	    audio = null;
	    return;
	}

	try {
	    fclass = Class.forName("com.sun.tv.media.jmf.audio.AudioFormat");
	} catch (ClassNotFoundException e) {
	    audio = null;
	    return;
	}

	cargs[0] = fclass;
	try {
	    constructor = aclass.getConstructor(cargs);
	} catch (Exception e) {
	    System.err.println("AudioRenderer: failed to create AudioPlay: 1 "
		    + e);
	    audio = null;
	    return;
	}

	oargs[0] = format;
	try {
	    audio = (AudioPlay) constructor.newInstance(oargs);
	} catch (Exception e) {
	    System.err.println("AudioRenderer: failed to create AudioPlay: 2 "
		    + e);
	    audio = null;
	}
    }

    public void pause() {
	if (audio != null)
	    audio.pause();
    }

    public void resume() {
	if (skipSound)
	    return;
	if (audio != null)
	    audio.resume();
    }

    public void flush() {
	// Put itself in the flushing state. Then flush the
	// audio device.
	flushing = true;
	if (audio != null)
	    audio.flush();
	// Cleanup all the filled buffers.
	super.flush();
    }

    public void drain() {
	if (audio != null && !skipSound)
	    audio.drain();
    }

    public static boolean useJavaSound() {
	return fUseJavaSound;
    }

    public static boolean useGainControl() {

	if (fUseJavaSound) {
	    return true;
	}

	if (!useGainControlIndicator) {

	    try { // find if the sun.audio package support volume setting
		Class c;

		if (System.getSecurityManager() == null) { // application
		    c = Class.forName("sun.audio.AudioPlayer");
		    Class p[] = new Class[2];
		    p[0] = Integer.TYPE;
		    p[1] = Integer.TYPE;
		    Method m = c.getDeclaredMethod("setVolume", p);
		    fUseGainControl = true;
		} else {
		    fUseGainControl = false;
		}

		/*
		 * Class c =
		 * Class.forName("com.sun.tv.media.renderer.audio.AudioRenderer"
		 * ); ClassLoader cl = c.getClassLoader();
		 * 
		 * if (cl != null ) { // the class was loaded with ClassLoader
		 * fUseGainControl = false; } else { // the class was loaded
		 * locally
		 * 
		 * c = Class.forName("sun.audio.AudioPlayer"); Class p[] = new
		 * Class[2]; p[0] = Integer.TYPE; p[1] = Integer.TYPE; Method m
		 * = c.getDeclaredMethod("setVolume" , p); fUseGainControl =
		 * true;
		 * 
		 * }
		 */

	    } catch (Exception e) { // NoSuchMethodException or
				    // SecurityException
		System.err
			.println("Volume setting is not supported by sun.audio package.");
		fUseGainControl = false;
	    }

	    useGainControlIndicator = true;
	}

	return fUseGainControl;
    }

    // ////////////////////////////////////////////////////////////////////////
    // INNER CLASSES

    class AudioTimeBase implements TimeBase, ControllerListener {
	long origin = 0;
	long offset = 0;
	long time = 0;
	TimeBase systemTimeBase = null;
	AudioRenderer renderer;

	AudioTimeBase(AudioRenderer r) {
	    renderer = r;
	    addControllerListener(this);
	    useSystemTime(true);
	}

	public Time getTime() {
	    return new Time(getNanoseconds());
	}

	public long getNanoseconds() {
	    if (systemTimeBase != null)
		time = origin + systemTimeBase.getNanoseconds() - offset;
	    else if (audio != null)
		time = origin + audio.getTick() - offset;
	    return time;
	}

	/**
	 * Time bases needs to be monotonically increasing with no major quantum
	 * leap. If the audio switches, you'll need to manually call reset() to
	 * continue counting time from previous time.
	 */
	protected void reset() {
	    useSystemTime(false);
	}

	/**
	 * Switch to using system time.
	 */
	protected void useSystemTime(boolean f) {
	    if (f && systemTimeBase == null) {
		systemTimeBase = new SystemTimeBase();
		offset = systemTimeBase.getNanoseconds();
		origin = time;
	    } else if (!f && systemTimeBase != null) {
		systemTimeBase = null;
		offset = (audio != null ? audio.getTick() : 0);
		origin = time;
	    }
	}

	/**
	 * Monitor the events from the controllers.
	 */
	public void controllerUpdate(ControllerEvent evt) {
	    if (evt.getSourceController() != renderer)
		return;

	    // If the renderer is stop, we'll switch to system time.
	    // This will allows for a non-stopping time base.
	    if (evt instanceof EndOfMediaEvent || evt instanceof StopEvent) {
		useSystemTime(true);
	    } else if (evt instanceof StartEvent) {
		if (renderer.getRate() == 1.0f)
		    useSystemTime(false);
	    }
	}
    }

    class RenderThread extends LoopThread {
	AudioRenderer renderer;

	RenderThread(AudioRenderer renderer) {
	    this.renderer = renderer;
	    setName("AudioRenderer LoopThread");
	    useAudioPriority();
	}

	public boolean process() {
	    if (renderer != null) {
		renderer.processPutData();
	    }
	    return true;
	}
    }

    //
    // AudioIn connectable for AudioRenderer.
    //
    class AudioIn implements InputConnectable {

	protected AudioRenderer renderer;
	protected com.sun.tv.media.Format formats[];
	protected AudioFormat format;
	protected OutputConnectable prevOut;

	AudioIn(AudioRenderer renderer) {
	    this.renderer = renderer;
	    formats = new AudioFormat[1];
	    formats[0] = new AudioFormat();
	    format = null;
	}

	public com.sun.tv.media.Format[] listFormats() {
	    return formats;
	}

	public void setFormat(com.sun.tv.media.Format f) {
	    Log.e("EkhoTV", "In AR setFormat: " + f);
	    if (f instanceof AudioFormat) {
		format = (AudioFormat) f;
		Log.e("EkhoTV", "AR setFormat: set to " + f);
	    } else {
		System.err.println("AR setFormat: not an AudioFormat");
		format = null;
	    }
	}

	public com.sun.tv.media.Format getFormat() {
	    return format;
	}

	public OutputConnectable connectedTo() {
	    return prevOut;
	}

	public void connectTo(OutputConnectable port) {
	    renderer.connectTo(port);
	    prevOut = port;
	}

	public MediaProcessor getMediaProcessor() {
	    return renderer;
	}

	public boolean tryPutData(Object obj) {
	    return false;
	}

	public void putData(Object obj) {
	    renderer.putData((Data) obj);
	}

	public Object tryGetContainer() {
	    return tryGetContainer(null);
	}

	public Object getContainer() {
	    return getContainer(null);
	}

	public Object tryGetContainer(com.sun.tv.media.Format f) {
	    // Log.e("EkhoTV", "AR:AIN: tryGetContainer: " + this + ": " +
	    // f);
	    if (f instanceof AudioFormat) {
		format = (AudioFormat) f;
	    } else {
		format = null;
	    }
	    return renderer.tryGetContainer(format);
	}

	public Object getContainer(com.sun.tv.media.Format format) {
	    return renderer.getContainer(format);
	}

	public void putContainer(Object obj) {
	    renderer.putContainer((Data) obj);
	}

	public void flush() {
	    renderer.flush();
	}
    }
}
