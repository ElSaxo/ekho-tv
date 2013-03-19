/*
 * @(#)VideoRenderer.java	1.4 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.renderer.video;

import javax.media.Control;
import javax.media.Controller;
import javax.media.Time;

import nl.ekholabs.ekhotv.activity.EkhoTVProxyActivity;
import nl.ekholabs.ekhotv.awt.Dimension;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import com.sun.tv.media.Blitter;
import com.sun.tv.media.Data;
import com.sun.tv.media.Format;
import com.sun.tv.media.MediaPlayer;
import com.sun.tv.media.MediaRenderer;
import com.sun.tv.media.SizeChangeEvent;
import com.sun.tv.media.VidData;
import com.sun.tv.media.controls.AtomicControlAdapter;
import com.sun.tv.media.controls.BooleanControl;
import com.sun.tv.media.controls.BooleanControlAdapter;
import com.sun.tv.media.controls.ColorControlAdapter;
import com.sun.tv.media.controls.NumericControl;
import com.sun.tv.media.controls.NumericControlAdapter;
import com.sun.tv.media.controls.StringControl;
import com.sun.tv.media.controls.VideoSizingControl;
import com.sun.tv.media.format.video.IndexColorFormat;
import com.sun.tv.media.format.video.RGBFormat;
import com.sun.tv.media.format.video.VidFormat;
import com.sun.tv.media.format.video.YUVFormat;
import com.sun.tv.media.util.DataBufQueue;
import com.sun.tv.media.util.LoopThread;

/**
 * A video renderer that performs color conversion and scaling. Drops frames if
 * the frame rate needs to be controlled.
 */
public class VideoRenderer extends MediaRenderer {

    /*************************************************************************
     * VARIABLES
     *************************************************************************/

    protected final int BRIGHTNESS = 1;
    protected final int CONTRAST = 2;
    protected final int SATURATION = 3;
    protected final int HUE = 4;
    protected final int GRAYSCALE = 5;
    protected final int VIDEOMUTE = 6;

    private long frames; // frame decoded counter.
    private long statTime;
    private float frameRate;
    protected VideoIn videoIn;
    protected VideoTimer vTimer;
    public int offsetX;
    public int offsetY;
    protected int clipWidth;
    protected int clipHeight;
    protected boolean videoMute;

    protected ColorConverter converter;
    protected YUVToRGB yuvConverter;

    // This is the frame that will be blasted to the window
    private VidData displayFrame;
    // The format of the displayFrame
    protected VidFormat displayFormat;

    private long lastMTS;
    private long lastPTS;
    private boolean useScheduling = true;

    // The format of the input video frame
    protected VidFormat inFormat;

    final int MAX_VIDEO_BUF = 640 * 480; // Max video size
    final int maxBuffers = 1;

    // Initial values.
    final protected int DEFAULT_WIDTH = 320;
    final protected int DEFAULT_HEIGHT = 240;
    // Tolerance between old size and new size of buffers.
    final protected int SIZE_DIFF = 64 * 1024;

    protected int width;
    protected int height;
    protected int outWidth;
    protected int outHeight;
    protected int length;
    protected float scale;
    protected Rect clipRect;
    protected long fpsClock;
    public static int screenDepth;

    // Controls
    protected Control[] controls;
    private VideoSizingControl videoSizingControl;
    // private ColorControl colorControl = null;
    private StringControl frameRateControl;

    // private Slider brightnessComp = null;
    // private Slider contrastComp = null;
    // private Slider saturationComp = null;
    // private Slider hueComp = null;
    private boolean colorAvailable = false;
    private boolean flushing = false;
    boolean controllerClosed = false;

    // Buffered image related stuff
    protected boolean usingJava2D;
    protected boolean useJava2D;
    public boolean useFastBlt = true;

    protected Blitter blitter;

    protected VideoComponent visualComponent;
    protected VidData lastFrame;
    protected boolean inputFormatChanged = true;
    protected String[] blitterList;
    protected StringControl videoProps;

    // Note: Create VideoRenderer, call setInputFormat(), then call setSize()

    /*************************************************************************
     * METHODS
     *************************************************************************/

    public VideoRenderer() {

	// deviceFetched = true; // No device to fetch

	scale = (float) 1.0;
	videoIn = new VideoIn(this);
	registerInput("VideoIn", videoIn);
	// just create a dummy buffer
	allocBuffers(10, 1);

	if (MediaPlayer.defaultColorModel == null)
	    MediaPlayer.defaultColorModel = new Color();

	screenDepth = 8;

	// if (MediaPlayer.defaultColorModel instanceof IndexColorModel)
	// screenDepth = 8;
	// else
	// screenDepth = 32;

	videoSizingControl = getVSC();
	// colorControl = getCC();

	// Hard code the blitters for now. Need to get the list from
	// jmf.properties
	blitterList = new String[5];
	blitterList[0] = "com.sun.tv.media.blitter.directx.Blitter"; // for
								     // windows
	blitterList[1] = "com.sun.tv.media.blitter.xil.Blitter"; // for solaris
	blitterList[2] = "com.sun.tv.media.blitter.xlib.Blitter"; // for solaris
	blitterList[3] = "com.sun.tv.media.blitter.gdi.Blitter"; // for windows
	blitterList[4] = "com.sun.tv.media.blitter.awt.Blitter";
	/*
	 * String arch = System.getProperty("os.arch", "none"); if
	 * (arch.indexOf("sparc") >= 0) { blitterList = new String[2];
	 * blitterList[0] = "com.sun.tv.media.blitter.xil.Blitter";
	 * blitterList[1] = "com.sun.tv.media.blitter.awt.Blitter"; } else {
	 * blitterList = new String[4]; blitterList[0] =
	 * "com.sun.tv.media.blitter.directx.Blitter"; blitterList[1] =
	 * "com.sun.tv.media.blitter.xil.Blitter"; // just in case its x86
	 * blitterList[2] = "com.sun.tv.media.blitter.gdi.Blitter";
	 * blitterList[3] = "com.sun.tv.media.blitter.awt.Blitter"; }
	 */
    }

    /*
     * private void colorEnable(boolean enabled) { if (brightnessComp != null)
     * brightnessComp.setEnabled(enabled); if (contrastComp != null)
     * contrastComp.setEnabled(enabled); if (saturationComp != null)
     * saturationComp.setEnabled(enabled); if (hueComp != null)
     * hueComp.setEnabled(enabled); }
     */
    public void reallocateFreeBuffer() {
	VidData free;
	// Assuming there's atmost one buffer

	if ((free = (VidData) removeOldContainer()) != null) {
	    if (inFormat instanceof YUVFormat) {
		free = new YUVData((YUVFormat) inFormat);
	    } else if (inFormat instanceof RGBFormat) {
		free = new RGBData((RGBFormat) inFormat);
	    } else if (inFormat instanceof IndexColorFormat) {
		free = new IndexColorData((IndexColorFormat) inFormat);
	    }
	}
	addNewContainer(free);
	inputFormatChanged = true;
    }

    /**
     * Allocate dummy buffers of VidData type
     */
    public void allocBuffers(int size, int chunks) {
	DataBufQueue q = new DataBufQueue(chunks);
	for (int i = 0; i < chunks; i++) {
	    q.addNewBuffer(new VidData(new VidFormat(1, 1, 1, 1)));
	}
	setBuffers(q);
    }

    /**
     * Kill all its threads.
     */
    protected void doClose() {
	controllerClosed = true;
	blitter = null;
	if (vTimer != null)
	    vTimer.kill();
	dataBuf.close();
    }

    public void setVideoProps(StringControl props) {
	videoProps = props;
    }

    public void setInputFormat(Format format) {
	inFormat = (VidFormat) format;
	width = inFormat.getWidth();
	height = inFormat.getHeight();
	if (videoProps != null)
	    videoProps.setValue(width + " x " + height);
    }

    public void setSize(int w, int h) {
	outWidth = w;
	outHeight = h;
	if (visualComponent != null) {
	    visualComponent.getComponent().setMinimumWidth(w);
	    visualComponent.getComponent().setMinimumHeight(h);
	    synchronized (visualComponent) {
		if (blitter != null)
		    blitter.setOutputSize(w, h);
	    }
	}
    }

    protected void setClipRegion(Rect clip) {
	if (clip == null)
	    clipRect = null;
	else {
	    boolean intersects = clip.intersects(0, 0, width - 1, height - 1);
	    if (intersects) {
		clipRect = new Rect(0, 0, width - 1, height - 1);
	    }
	}
    }

    public void resizeBuffers(int w, int h) {
	width = w;
	height = h;
	reallocateFreeBuffer();
    }

    public Dimension getSize() {
	return new Dimension(width, height);
    }

    private void preGetContainer(Format format) {
	if (format != null) {
	    try {
		VidFormat fmt = (VidFormat) format;
		if (!fmt.equals(inFormat)) {
		    int newWidth = fmt.getWidth();
		    int newHeight = fmt.getHeight();
		    float newScale = (float) 1.0;
		    if (videoProps != null)
			videoProps.setValue(newWidth + " x " + newHeight);
		    // Send a size change event if the size has changed
		    if (newWidth != inFormat.getWidth()
			    || newHeight != inFormat.getHeight()) {
			sendEvent(new SizeChangeEvent(this, newWidth,
				newHeight, newScale));
		    }
		    if (fmt instanceof YUVFormat) {
			YUVFormat yuv = (YUVFormat) format;
			inFormat = (YUVFormat) yuv.clone();
		    } else if (fmt instanceof RGBFormat) {
			RGBFormat rgb = (RGBFormat) format;
			inFormat = (RGBFormat) rgb.clone();
		    } else if (fmt instanceof IndexColorFormat) {
			IndexColorFormat icf = (IndexColorFormat) format;
			inFormat = (IndexColorFormat) icf.clone();
		    } else {
			Log.e("EkhoTV", "Unsupported format: " + format);
		    }
		    resizeBuffers(newWidth, newHeight);
		}
	    } catch (ClassCastException e) {
		System.err.println("Error at VideoRenderer.getContainer:" + e);
	    }
	}
    }

    public synchronized Data getContainer(Format format) {
	preGetContainer(format);
	return super.getContainer(format);
    }

    public synchronized Data tryGetContainer(Format format) {
	preGetContainer(format);
	return super.tryGetContainer(format);
    }

    /**
     * Put back an unused buffer to the node.
     */
    public synchronized void putContainer(Data data) {
	super.putContainer(data);

	VidData vdata = (VidData) data;
	VidFormat vFormat = (VidFormat) vdata.getFormat();
	// If the format is not what it is supposed to be, it must be an old
	// buffer sent upstream before the size/format change request
	if (!vFormat.equals(inFormat))
	    reallocateFreeBuffer();
    }

    protected boolean doRealize() {
	return true;
    }

    protected void abortRealize() {
    }

    protected boolean doPrefetch() {
	return true;
    }

    protected void abortPrefetch() {
	resetDataBufQueue();
    }

    public void doStart() {
	super.doStart();
	// try {
	// JMFSecurity.enablePrivilege.invoke(JMFSecurity.privilegeManager,
	// JMFSecurity.threadArgs);
	// JMFSecurity.enablePrivilege.invoke(JMFSecurity.privilegeManager,
	// JMFSecurity.threadGroupArgs);
	// } catch (Exception e) {
	// }
	if (vTimer == null) {
	    vTimer = new VideoTimer(this);
	    vTimer.setName(vTimer.getName() + ": " + getClass().getName());
	} else {
	    vTimer.restart();
	}
	// colorEnable( true && colorAvailable );
    }

    public void doStop() {
	if (vTimer != null)
	    vTimer.pause();
	// colorEnable( false && colorAvailable );
    }

    public void setVisualComponent(View c, Rect bounds) {
	visualComponent = new VideoComponent(this, c);
    }

    public View getVisualComponent() {
	if (visualComponent == null) {
	    visualComponent = new VideoComponent(this);
	    visualComponent.getComponent().setMinimumWidth(outWidth);
	    visualComponent.getComponent().setMinimumHeight(outHeight);
	}
	return visualComponent.getComponent();
    }

    public void setFrameRateControl(StringControl sc) {
	frameRateControl = sc;
    }

    public float getFrameRate() {
	long now = System.currentTimeMillis();
	long tdiff = now - statTime;

	if (tdiff == 0)
	    return 0;
	frameRate = (float) frames * 1000 / tdiff;
	// Round it off to one decimal pt.
	frameRate = (float) ((int) (frameRate * 10)) / 10;
	statTime = now;
	frames = 0;
	return frameRate;
    }

    // Instruct the renderer to use the PTS to schedule the frame
    // presentations. Turn it off to support the "push" model.
    public void slaveToPTS(boolean b) {
	useScheduling = b;
    }

    public void setMediaTime(Time t) {
	super.setMediaTime(t);
    }

    public void flush() {
	flushing = true;
	// Clean up all the filled buffers.
	super.flush();
    }

    // $$ New semantics. Return true if EOM buffer was received.
    // Dont send EndOfMediaEvent here. Dont call super.stop()
    public boolean processData(Data data) {

	if (!(data instanceof VidData))
	    return false;

	VidData vdata = (VidData) data;

	flushing = false;

	// Check for end of media.
	if (vdata.getLength() == Data.EOM) {

	    // Eventhough we've received EOM, we haven't finished
	    // processing yet. We'll need to sleep till its PT.
	    // Otherwise, for low FR movies, the last frame will
	    // not be presented to its full duration.
	    waitForPT(vdata.getPresentationTime());
	    vTimer.pause();
	    setTargetState(Prefetched);
	    return true;
	}

	// Bogus frame!
	if (vdata.getLength() < 0)
	    return false;

	if (vdata.getPresentationTime() <= 0) {

	    if (!videoMute) {
		convertAndDisplayFrame(vdata);
	    }

	} else {
	    long mt = getMediaNanoseconds();

	    // System.err.println("VR 0: PT = " +
	    // vdata.getPresentationTime() + " MT = " +
	    // mt + " duration = " + vdata.getDuration());

	    // Check if the media has seeked back since.
	    // What this does is tracks the setting of the media time
	    // to a previous location. If so, it will purge the frames
	    // until the new frame matching the new media time has
	    // arrived.
	    if (mt < lastMTS || vdata.getPresentationTime() < lastPTS) {
		if (mt < lastMTS && vdata.getPresentationTime() < lastPTS) {
		    lastMTS = mt;
		    lastPTS = vdata.getPresentationTime();
		} else
		    return false;
	    }

	    // Let's bring the #'s back to milli seconds range.
	    long t = mt / 1000000L - vdata.getPresentationTime() / 1000000L;

	    // System.err.println("VR 1: PT = " +
	    // vdata.getPresentationTime() + " MT = " +
	    // mt + " duration = " + vdata.getDuration());

	    // If delayed beyond the duration of the frame, then we should
	    // drop it.
	    if (t > 2 * vdata.getDuration() / 1000000L && vdata.getDiscard()
		    && useScheduling) {
		// drop the frame.
		// System.err.println("frame dropped");
		return false;
	    } else {

		// Wait if we are ahead of the presentation time.
		if (t < -65 && useScheduling)
		    waitForPT(vdata.getPresentationTime());

		// System.err.println("VR: PT = " +
		// vdata.getPresentationTime() + " MT = " +
		// mt + " duration = " + vdata.getDuration());

		if (!videoMute)
		    convertAndDisplayFrame(vdata);
	    }

	    lastMTS = mt;
	    lastPTS = vdata.getPresentationTime();
	}

	frames++;
	long tmp;
	if ((tmp = System.currentTimeMillis()) - fpsClock >= 1 * 1000) {
	    fpsClock = tmp;
	    // try {
	    // frameRateControl.setValue(getFrameRate()
	    // + JMFI18N.getResource("videorenderer.frames/sec"));
	    // } catch (Exception e) {
	    // Log.e("EkhoTV", e.getMessage());
	    // }
	}
	return false;
    }

    private boolean waitForPT(long pt) {
	long mt = getMediaNanoseconds();
	long t = mt / 1000000L - pt / 1000000L;

	// Check if the media has seeked back since.
	if (mt < lastMTS)
	    return false;

	while (t < -65 && useScheduling && !flushing) {
	    // The interval is scheduled at 1/16 sec earlier.
	    long interval = -1L * t - 65L;

	    // Don't sleep more than 1/4 sec.
	    // We'll wake up and check time again.
	    interval = (interval > 250L ? 250L : interval);

	    // System.err.println("VR: PT = " + pt +
	    // " MT = " + mt + " interval = " + interval);

	    try {
		Thread.currentThread().sleep(interval);
	    } catch (InterruptedException e) {
	    }

	    // Check the time again to see if we need to sleep more.
	    mt = getMediaNanoseconds();

	    // Check if the media has seeked back since.
	    if (mt < lastMTS)
		return false;

	    t = mt / 1000000L - pt / 1000000L;
	}
	return true;
    }

    protected ColorConverter getConverter(VidFormat fin, VidFormat fout) {

	ColorConverter converter = null;
	// YUV to RGB Conversion
	if (fin instanceof YUVFormat) {
	    YUVFormat inFormat = (YUVFormat) fin;
	    int srcWidth = inFormat.getWidth();
	    int srcHeight = inFormat.getHeight();
	    int strideX = srcWidth;
	    int strideY = srcHeight;
	    if ((strideX & 7) != 0)
		strideX += (8 - (strideX & 7));
	    if ((strideY & 1) != 0)
		strideY++;

	    if (fout instanceof RGBFormat) {
		RGBFormat prefFormat = (RGBFormat) fout;
		int dstDepth = prefFormat.getDepth();

		converter = new YUVToRGB(prefFormat.getRedMask(),
			prefFormat.getGreenMask(), prefFormat.getBlueMask(),
			prefFormat.getDepth() * 8);

		displayFormat = new RGBFormat(srcWidth, srcHeight, strideX
			* strideY, 4, prefFormat.getRedMask(),
			prefFormat.getGreenMask(), prefFormat.getBlueMask(),
			dstDepth);
		displayFormat.setStrideX(strideX);
		displayFormat.setStrideY(strideY);

		// if (inFormat.getDecimation() != YUVFormat.YVU9)
		// colorEnable(true);
		yuvConverter = (YUVToRGB) converter;
	    }
	} else if (fin instanceof RGBFormat && fout instanceof RGBFormat) {
	    RGBFormat rgbFormat = (RGBFormat) fin;
	    RGBFormat prefFormat = (RGBFormat) fout;
	    int srcRedMask = rgbFormat.getRedMask();
	    int srcGreenMask = rgbFormat.getGreenMask();
	    int srcBlueMask = rgbFormat.getBlueMask();
	    int srcWidth = rgbFormat.getWidth();
	    int srcHeight = rgbFormat.getHeight();
	    int srcDepth = rgbFormat.getDepth();
	    int dstRedMask = prefFormat.getRedMask();
	    int dstGreenMask = prefFormat.getGreenMask();
	    int dstBlueMask = prefFormat.getBlueMask();
	    int dstDepth = prefFormat.getDepth();
	    int elSize = 4;
	    int strideX = rgbFormat.getStrideX();
	    int strideY = rgbFormat.getStrideY();

	    converter = new RGBToRGB(srcRedMask, srcGreenMask, srcBlueMask,
		    srcDepth, dstRedMask, dstGreenMask, dstBlueMask, dstDepth);
	    displayFormat = new RGBFormat(srcWidth, srcHeight, strideX
		    * strideY, elSize, dstRedMask, dstGreenMask, dstBlueMask,
		    dstDepth);
	    displayFormat.setStrideX(strideX);
	    displayFormat.setStrideY(strideY);
	} else if (fin instanceof IndexColorFormat && fout instanceof RGBFormat) {
	    IndexColorFormat icformat = (IndexColorFormat) fin;
	    int srcWidth = icformat.getWidth();
	    int srcHeight = icformat.getHeight();
	    int strideX = srcWidth;
	    int strideY = srcHeight;
	    RGBFormat prefFormat = (RGBFormat) fout;
	    displayFormat = new RGBFormat(srcWidth, srcHeight, strideX
		    * strideY, 4, prefFormat.getRedMask(),
		    prefFormat.getGreenMask(), prefFormat.getBlueMask(),
		    prefFormat.getDepth());
	    displayFormat.setStrideX(strideX);
	    displayFormat.setStrideY(strideY);

	    converter = new PseudoToRGB(icformat, (RGBFormat) displayFormat);
	}
	return converter;
    }

    protected void selectBlitterAndConverter(VidData frame) {

	// First kill the old blitter and converter
	yuvConverter = null;
	// colorEnable( false );
	if (blitter != null)
	    blitter.close();
	blitter = null;
	converter = null;
	displayFormat = null;
	// Get the format of the video frame
	VidFormat frameFormat = (VidFormat) frame.getFormat();

	// Try each blitter in the blitter list until we find one that we can
	// use
	// or we run out of blitters.
	int index = 0;
	while (blitter == null && index < blitterList.length) {
	    Class blitterClass = null;
	    // Try to get the blitter Class
	    try {
		blitterClass = Class.forName(blitterList[index]);
	    } catch (Exception e) {
	    } catch (UnsatisfiedLinkError ule) {
		blitterClass = null;
	    }

	    if (blitterClass != null) {
		// Try to instantiate the blitter and set the component
		try {
		    blitter = (com.sun.tv.media.Blitter) blitterClass
			    .newInstance();
		    blitter.setComponent(visualComponent.getHeavyComponent());
		} catch (Exception e) {
		    blitter = null;
		} catch (UnsatisfiedLinkError ule) {
		    blitter = null;
		}

		// See if it likes the format
		if (blitter != null) {
		    if (blitter.setFormat(frameFormat)) {
			// set up the display format and frame
			displayFormat = (VidFormat) frameFormat.clone();
			break; // It likes the format!!!
		    } else {
			// We need to try a converter
			VidFormat blitterPrefers = blitter.getPreferredFormat();
			converter = getConverter(frameFormat, blitterPrefers);
			// If we couldn't get a converter, there's no hope
			if (converter == null)
			    blitter = null;
			else {
			    break; // Can convert. Lets use it!!
			}
		    }
		}
	    }
	    // May need to try the next one
	    index++;
	}

	if (blitter == null) {
	    Log.i("EkhoTV", "Oh My! No Blitter could be found!");
	} else {
	    if (displayFormat != null) {
		if (displayFormat instanceof YUVFormat)
		    displayFrame = new YUVData(displayFormat);
		else if (displayFormat instanceof RGBFormat)
		    displayFrame = new RGBData(displayFormat);
		else if (displayFormat instanceof IndexColorFormat)
		    displayFrame = new IndexColorData(displayFormat);
	    } else
		Log.i("EkhoTV",
			"VideoRenderer: displayFormat shouldn't be null!!!");

	    blitter.setBuffer(frame);
	    blitter.setOutputSize(outWidth, outHeight);
	    inputFormatChanged = false;
	}
    }

    protected void convertAndDisplayFrame(VidData frame) {
	VidFormat format = (VidFormat) frame.getFormat();
	if (visualComponent == null)
	    return;
	synchronized (visualComponent) {
	    if (!visualComponent.created)
		return;
	    if (blitter == null || inputFormatChanged) {
		selectBlitterAndConverter(frame);
	    }

	    // Do we need a conversion step?
	    if (converter != null) {
		converter.convert(frame, displayFrame);
		frame = displayFrame;
	    }

	    // Prepare the blitter for the incoming buffer and draw it.
	    if (blitter != null) {
		blitter.setBuffer(frame);
		lastFrame = frame;
		blitter.draw(frame, visualComponent.getX(),
			visualComponent.getY());
	    }
	}
    }

    void repaint() {
	synchronized (visualComponent) {
	    try {
		if (visualComponent == null || !visualComponent.created
			|| lastFrame == null)
		    return;
		if (blitter != null) {
		    blitter.setBuffer(lastFrame);
		    blitter.draw(lastFrame, visualComponent.getX(),
			    visualComponent.getY());
		}
	    } catch (Exception e) {
		Log.e("EkhoTV",
			"Error drawing VideoRenderer Blitter ==> "
				+ e.getMessage());
	    }
	}
    }

    public void windowDestroyed() {
	synchronized (visualComponent) {
	    if (blitter != null) {
		blitter.close();
		blitter = null;
	    }
	    converter = null;
	}
    }

    public void windowResized() {
	synchronized (visualComponent) {
	    if (blitter != null) {
		blitter.setOutputSize(outWidth, outHeight);
	    }
	}
    }

    /**
     * Returns an array of controls that VideoRenderer offers.
     */
    public Control[] getControls() {
	// Create the controls if they are not already created.
	if (controls == null) {
	    controls = new Control[1];
	    controls[0] = videoSizingControl;
	    // controls[1] = colorControl;
	}
	return controls;
    }

    protected VideoSizingControl getVSC() {
	return new VSC(null, false, null);
    }

    /*
     * protected ColorControl getCC() { Color bcolor = Color.white; float []
     * detents = new float[1]; detents[0] = 0.5f; // Brightness brightnessComp =
     * new Slider(detents, bcolor); NumericControl brightness = new
     * GenericColorNCA(0.0f, 1.0f, 0.5f, 0.001f, false, brightnessComp, false,
     * null, BRIGHTNESS); brightnessComp.setControl(brightness);
     * 
     * // Contrast contrastComp = new Slider(detents, bcolor); NumericControl
     * contrast = new GenericColorNCA(0.0f, 1.0f, 0.5f, 0.001f, false,
     * contrastComp, false, null, CONTRAST); contrastComp.setControl(contrast);
     * 
     * // Saturation saturationComp = new Slider(detents, bcolor);
     * NumericControl saturation = new GenericColorNCA(0.0f, 1.0f, 0.5f, 0.001f,
     * false, saturationComp, false, null, SATURATION);
     * saturationComp.setControl(saturation);
     * 
     * // Hue hueComp = new Slider(detents, bcolor); NumericControl hue = new
     * GenericColorNCA(0f, 1.0f, 0.5f, 0.001f, false, hueComp, false, null,
     * HUE); hueComp.setControl(hue);
     * 
     * BooleanControl grayscale = new GenericBCA(null, false, null, GRAYSCALE);
     * 
     * Panel ccPanel = new Panel(); FlowLayout fl; ccPanel.setLayout( fl = new
     * FlowLayout() ); fl.setHgap(1); fl.setVgap(0);
     * ccPanel.add(brightness.getControlComponent());
     * ccPanel.add(contrast.getControlComponent());
     * ccPanel.add(saturation.getControlComponent());
     * ccPanel.add(hue.getControlComponent()); ccPanel.setVisible(true);
     * 
     * return new ColorControlAdapter(brightness, contrast, saturation, hue,
     * grayscale, ccPanel, false, null); }
     */

    /*************************************************************************
     * INNER CLASSES
     *************************************************************************/

    /**
     * GenericColor Numeric Control Adapter for the four color control sliders.
     */
    class GenericColorNCA extends NumericControlAdapter {
	int type;

	// GenericColorNCA::
	public GenericColorNCA(float ll, float ul, float dv, float gran,
		boolean log, View comp, boolean def, Control paren, int type) {
	    super(ll, ul, dv, gran, log, comp, def, paren);
	    this.type = type;
	}

	// GenericColorNCA::
	public float setValue(float val) {
	    float retVal = 0f;
	    if (yuvConverter == null)
		return retVal;
	    switch (type) {
	    case BRIGHTNESS:
		retVal = yuvConverter.setBrightness(val);
		break;
	    case CONTRAST:
		retVal = yuvConverter.setContrast(val);
		break;
	    case SATURATION:
		retVal = yuvConverter.setSaturation(val);
		break;
	    case HUE:
		retVal = yuvConverter.setHue(val);
		break;
	    default:
		return 0.0f;
	    }
	    informListeners();
	    return retVal;
	}

	// GenericColorNCA::
	public float getValue() {
	    if (yuvConverter == null)
		return 0.0f;
	    switch (type) {
	    case BRIGHTNESS:
		return yuvConverter.getBrightness();
	    case CONTRAST:
		return yuvConverter.getContrast();
	    case SATURATION:
		return yuvConverter.getSaturation();
	    case HUE:
		return yuvConverter.getHue();
	    default:
		return 0.0f;
	    }
	}
    }

    /**
     * Color Control group
     */
    class CC extends ColorControlAdapter {
	public CC(NumericControl b, NumericControl c, NumericControl s,
		NumericControl h, BooleanControl g, View comp, boolean def,
		Control parent) {
	    super(b, c, s, h, g, comp, def, parent);
	    ((AtomicControlAdapter) b).setParent(this);
	    ((AtomicControlAdapter) c).setParent(this);
	    ((AtomicControlAdapter) s).setParent(this);
	    ((AtomicControlAdapter) h).setParent(this);
	    ((AtomicControlAdapter) g).setParent(this);
	}
    }

    /**
     * VideoSizingControl
     */
    class VSC extends AtomicControlAdapter implements VideoSizingControl {

	Control[] controls = null;
	BooleanControl vmc = null;

	// VSC::
	public VSC(View c, boolean def, Control parent) {
	    super(c, def, parent);
	    // component = getVideoMute().getControlComponent();
	}

	// VSC::
	public boolean supportsAnyScale() {
	    return true;
	}

	// VSC::
	public Dimension setVideoSize(Dimension size) {
	    setSize(size.width, size.height);
	    return getVideoSize();
	}

	// VSC::
	public Dimension getVideoSize() {
	    return new Dimension(VideoRenderer.this.outWidth,
		    VideoRenderer.this.outHeight);
	}

	// VSC::
	public Dimension getInputVideoSize() {
	    return new Dimension(VideoRenderer.this.width,
		    VideoRenderer.this.height);
	}

	// VSC::
	public boolean supportsZoom() {
	    return false;
	}

	// VSC::
	public float[] getValidZoomFactors() {
	    return new float[0];
	}

	// VSC::
	public NumericControl getZoomControl() {
	    return null;
	}

	// VSC::
	public boolean supportsClipping() {
	    return false;
	}

	// VSC::
	public Rect setClipRegion(Rect clip) {
	    VideoRenderer.this.setClipRegion(clip);
	    return getClipRegion();
	}

	// VSC::
	public Rect getClipRegion() {
	    return new Rect(clipRect);
	}

	// VSC::
	public BooleanControl getVideoMute() {
	    if (vmc == null) {
		vmc = new GenericBCA(null, false, null, VIDEOMUTE);
	    }

	    return vmc;
	}

	// VSC::
	public Control[] getControls() {
	    if (this.controls == null) {
		if (vmc == null) {
		    vmc = getVideoMute();
		}
		controls = new Control[1];
		controls[0] = vmc;
	    }
	    return controls;
	}
    }

    /**
     * Generic (not really) Boolean Control Adapter to be used for both
     * GRAYSCALE and VIDEOMUTE
     */
    class GenericBCA extends BooleanControlAdapter {
	// Which specific control GRAYSCALE/VIDEOMUTE
	int type;

	public GenericBCA(View c, boolean def, Control parent, int type) {
	    super(c, def, parent);
	    this.type = type;
	}

	public boolean setValue(boolean val) {
	    boolean retVal = false;
	    if (type == GRAYSCALE) {
		if (yuvConverter != null)
		    retVal = yuvConverter.setGrayscale(val);
	    } else if (type == VIDEOMUTE) {
		retVal = videoMute = val;
	    } else
		retVal = false;
	    informListeners();
	    return retVal;
	}

	public boolean getValue() {
	    if (type == GRAYSCALE) {
		if (yuvConverter != null)
		    return yuvConverter.getGrayscale();
		else
		    return false;
	    } else if (type == VIDEOMUTE)
		return videoMute;
	    else
		return false;
	}
    }

}

/*************************************************************************
 * SUPPORT CLASSES
 *************************************************************************/

/**
 * A VideoTimer class to schedule the reading of data from the upstream node.
 */
class VideoTimer extends LoopThread {

    VideoRenderer renderer;

    public VideoTimer(VideoRenderer r) {
	super(); // Process right away as soon as we call start().
	setName("VideoRenderer processing loop");
	useVideoPriority();
	renderer = r;
	start();
    }

    public boolean process() {
	if (renderer.controllerClosed)
	    System.err.println("Hey! Controller is closed");
	renderer.processPutData();
	yield();
	return true;
    }

}

/**
 * Target video display area. It uses the default RGB color model for the pixel
 * values: Alpha: 24-31, Red: 16-23; Green: 8-15; Blue: 0-7.
 */
class VideoComponent {

    public boolean created = false;
    private View component;
    private View heavyComponent;
    public int x;
    public int y;
    private int lwOffsetX;
    private int lwOffsetY;
    VideoRenderer vidR;

    public VideoComponent(VideoRenderer vr) {
	vidR = vr;

	EkhoTVProxyActivity proxy = EkhoTVProxyActivity.getInstance();
	Context context = proxy.getActivity().getApplicationContext();

	component = new MyCanvas(context.getApplicationContext(), vidR);
	// component.addComponentListener(new ComponentAdapter() {
	// private int lastWidth = -1;
	// private int lastHeight = -1;
	//
	// public void componentResized(ComponentEvent ce) {
	// if (created) {
	// Dimension csize = ce.getComponent().getSize();
	// if (lastWidth == csize.width && lastHeight == csize.height)
	// return;
	// lastWidth = csize.width;
	// lastHeight = csize.height;
	// vidR.setSize(csize.width, csize.height);
	// vidR.windowResized();
	// }
	// }
	// });

	heavyComponent = component;
	lwOffsetX = 0;
	lwOffsetY = 0;
	x = 0;
	y = 0;
    }

    public void newWindow() {
	vidR.windowDestroyed();
    }

    public VideoComponent(VideoRenderer vr, View c) {
	vidR = vr;
	lwOffsetX = 0;
	lwOffsetY = 0;
	component = c;

	Point p = new Point();
	/*
	 * while (c.getPeer() instanceof java.awt.peer.LightweightPeer) {
	 * p.translate(c.getLocation().x, c.getLocation().y); c = c.getParent();
	 * }
	 */

	heavyComponent = c;
	lwOffsetX = p.x;
	lwOffsetY = p.y;
	x = lwOffsetX;
	y = lwOffsetY;
	created = true;
    }

    public View getComponent() {
	return component;
    }

    public View getHeavyComponent() {
	return heavyComponent;
    }

    public void setSize(int x, int y) {
	component.setMinimumWidth(x);
	component.setMinimumHeight(y);
    }

    public void setOffset(int x, int y) {
	this.x = lwOffsetX + x;
	this.y = lwOffsetY + y;
    }

    public int getX() {
	return x;
    }

    public int getY() {
	return y;
    }

    /*************************************************************************
     * INNER CLASS
     *************************************************************************/

    public class MyCanvas extends View {
	private VideoRenderer vr;

	public MyCanvas(Context context, VideoRenderer vr) {
	    super(context);
	    this.vr = vr;
	    created = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
	    super.onDraw(canvas);
	    // newWindow();

	    if (!created) {
		return;
	    }
	    if (vidR.getState() == Controller.Started) {
		return;
	    } else {
		vidR.repaint();
	    }
	}

	public synchronized Dimension getPreferredSize() {
	    return new Dimension(vr.width, vr.height);
	}

	public Dimension getMinimumSize() {
	    return new Dimension(1, 1);
	}
    }
}