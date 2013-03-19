/*
 * @(#)Blitter.java	1.3 08/09/15
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.blitter.awt;

import java.nio.Buffer;
import java.nio.IntBuffer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.sun.tv.media.VidData;
import com.sun.tv.media.format.video.IndexColorFormat;
import com.sun.tv.media.format.video.RGBFormat;
import com.sun.tv.media.format.video.VidFormat;
import com.sun.tv.media.renderer.video.RGBData;

/**
 * AWT blitter
 */
public class Blitter implements com.sun.tv.media.Blitter {

    protected View component;
    protected VidFormat inFormat;
    protected VidData inData;

    protected int outWidth = -1;
    protected int outHeight = -1;

    protected VidFormat formatsSupported[];
    protected VidFormat preferredFormat;
    protected boolean componentChanged;

    protected Bitmap sourceImage;
    protected ImageView displayImage;

    private int argbColor;

    public Blitter() {
	formatsSupported = new VidFormat[2];

	formatsSupported[0] = new RGBFormat(0, 0, 0, 4, 0x000000FF, 0x0000FF00,
		0x00FF0000);

	// PseudoColor (dummy values)
	formatsSupported[1] = new IndexColorFormat(0, 0, 0, 1, 256, null);

	preferredFormat = formatsSupported[0];
    }

    /**
     * Sets the Java Component to which we need to blit. Returns true if there
     * were no complaints.
     */
    public boolean setComponent(View comp) {
	component = comp;
	componentChanged = true;
	return true;
    }

    /**
     * Specify the buffer that is going to be used. This method does not have to
     * be called. Its a way of preparing the blitter to use a certain buffer,
     * probably during the prefetch stage, so that we don't take a hit when
     * displaying the first frame. Returns true if there was no problem.
     */
    public boolean setBuffer(VidData data) {
	if (inData != data) {
	    inData = data;
	    // RGB input
	    if (data instanceof RGBData) {
		RGBFormat format = (RGBFormat) data.getFormat();
		int redMask = format.getRedMask();
		int greenMask = format.getGreenMask();
		int blueMask = format.getBlueMask();
		int aes = format.getArrayElSize();
		aes = 8 * aes; // no. of bits
		if (aes != 32) {
		    Log.e("JavaTV", "RGB data has to be 32 bit");
		    return false;
		}

		argbColor = Color.argb(aes, redMask, greenMask, blueMask);

		int width = format.getWidth();
		int height = format.getHeight();

		// sourceImage = Bitmap.createBitmap(new int[] { redMask,
		// greenMask, blueMask }, width, height,
		// Bitmap.Config.ARGB_8888);

		sourceImage = Bitmap.createBitmap(width, height,
			Bitmap.Config.ARGB_8888);

		Buffer buffer = IntBuffer.wrap((int[]) data.getBuffer());
		sourceImage.copyPixelsFromBuffer(buffer);

		if (component != null) {
		    createDisplayImage();
		}
	    } else {
		// System.err.println("Pseudo color AWT blitter not implemented yet");
		return false;
	    }
	}
	return true;
    }

    private void createDisplayImage() {
	// displayImage = (ImageView) EkhoTVActivity.activityInstance
	// .findViewById(R.id.display);
	// EkhoTVActivity.activityInstance.runOnUiThread(new Runnable() {
	// public void run() {
	// displayImage.setImageBitmap(sourceImage);
	// }
	// });
	componentChanged = false;
    }

    /**
     * Sets the size to which the frame is to be scaled when the next frame is
     * blitted. Returns true if the size was acceptable.
     */
    public boolean setOutputSize(int width, int height) {
	if (width > 0 && height > 0) {
	    outWidth = width;
	    outHeight = height;
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Returns instances of the different formats of video frames that this
     * class can take as input.
     */
    public VidFormat[] getFormats() {
	return formatsSupported;
    }

    /**
     * Returns the specific format that this class would prefer. This is
     * probably the format of the display and hence gives the best blit speed.
     */
    public VidFormat getPreferredFormat() {
	return preferredFormat;
    }

    /**
     * Specify the format of the data that is going to be blitted. This is used
     * as a preparatory hint. Does not have to be called. Returns true if the
     * format was acceptable.
     */
    public boolean setFormat(VidFormat format) {
	boolean ret = false;

	if (preferredFormat instanceof RGBFormat && format instanceof RGBFormat) {
	    RGBFormat prefFormat = (RGBFormat) preferredFormat;
	    RGBFormat tryFormat = (RGBFormat) format;
	    if (prefFormat.getRedMask() != tryFormat.getRedMask()
		    || prefFormat.getGreenMask() != tryFormat.getGreenMask()
		    || prefFormat.getBlueMask() != tryFormat.getBlueMask()
		    || prefFormat.getDepth() != tryFormat.getDepth())
		ret = false;
	    else
		ret = true;
	}

	if (ret) {
	    inFormat = format;
	}

	return ret;
    }

    /**
     * Draws the image at the specified coordinate. Returns true if the
     * operation was succesful.
     */
    public boolean draw(VidData data, int x, int y) {
	VidFormat f = (VidFormat) data.getFormat();
	int inWidth = f.getWidth();
	int inHeight = f.getHeight();

	int ow = (outWidth == -1) ? inWidth : outWidth;
	int oh = (outHeight == -1) ? inHeight : outHeight;

	if (component == null) {
	    return false;
	}
	if (data != inData) {
	    setBuffer(data);
	}

	if (argbColor != 0) {
	    if (componentChanged) {
		displayImage.setMaxWidth(ow);
		displayImage.setMinimumHeight(oh);
		createDisplayImage();
	    }

	} else {
	    return false;
	}
	return true;
    }

    public void close() {
    }
}