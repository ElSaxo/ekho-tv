/*
 * @(#)Codec.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.codec.video.cvid;

import com.sun.tv.media.Data;
import com.sun.tv.media.Format;
import com.sun.tv.media.MediaFormat;
import com.sun.tv.media.MediaFrameCodec;
import com.sun.tv.media.VidData;
import com.sun.tv.media.codec.video.cinepak.CineStore;
import com.sun.tv.media.format.video.RGBFormat;
import com.sun.tv.media.format.video.VidFormat;
import com.sun.tv.media.renderer.video.PseudoToRGB;

/**
 * Codec Used for decoding Cinepak video data stream based on the
 * JPEGDecoder.java source from Sun Micro Systems The input is VidData with a
 * byte array of compressed image data. The output is VidData with an int array
 * of image data in alpha-R-G-B format.
 * 
 * @version 1.0, 97/03/06
 */

public class Codec extends MediaFrameCodec {

    private CineStore fOurStore;

    private int nativeData;

    private PseudoToRGB colorConverter;

    private int rMask = 0x000000ff;

    private int gMask = 0x0000ff00;

    private int bMask = 0x00ff0000;

    private int bytesPerPixel = 4;

    private int inWidth = 320;

    private int inHeight = 240;

    private VidFormat inputFormat;

    private VidFormat outputFormat;

    public Codec() {
    }

    protected boolean initialize(MediaFormat format) {

	// Log.e("EkhoTV", "Decoder initialize");

	reconfigure();

	// Since we're an interframe codec we can't have dropped frames....

	super.allowDropFrame(false);

	fOurStore = new CineStore();

	if (fOurStore == null) {

	    System.err.println("Error creating storage for Cinepak Data");

	    return false;

	}

	if (colorConverter != null)
	    fOurStore.setColorConverter(colorConverter);

	return true;

    }

    public boolean supports(MediaFormat fmt) {
	return fmt.getCodec().equals("cvid");
    }

    /**
     * 
     * performs final clean up tasks including calling C code to deallocate
     * memory in the native methods
     */

    public void finalize() throws Throwable {

	super.finalize();

    }

    /**
     * 
     * Checks the header of the compressed video packet and detects any format
     * 
     * changes. Does not modify the data in any way. Prepare the decoder to
     * 
     * deal with that format.
     */

    public boolean checkFormat(Data data) {

	VidData vdata = (VidData) data;

	int newWidth = ((VidData) data).getWidth();

	int newHeight = ((VidData) data).getHeight();

	if (inWidth != newWidth || inHeight != newHeight) {

	    inWidth = newWidth;

	    inHeight = newHeight;
	    /*
	     * if (vdata.getFormat() instanceof IndexColorFormat) {
	     * //Log.e("EkhoTV", "Creating a color converter"); colorConverter =
	     * new PseudoToRGB((IndexColorFormat)vdata.getFormat(),
	     * (RGBFormat)outputFormat); if (fOurStore != null) {
	     * fOurStore.setColorConverter(colorConverter); } } else {
	     * colorConverter = null; //Log.e("EkhoTV",
	     * "Color converter unnecessary"); }
	     */
	    reconfigure();

	    return false;

	}

	return true;

    }

    public void reconfigure() {

	// System.err.println("RECONFIGURE sizex="+inWidth+"  sizey="+inHeight);

	inputFormat = new VidFormat(inWidth, inHeight, inWidth * inHeight, 1);

	int w = (inWidth + 3) & 0xfffffffc;
	int h = (inHeight + 3) & 0xfffffffc;

	outputFormat = new RGBFormat(inWidth, inHeight, w * h, 4, rMask, gMask,
		bMask, bytesPerPixel);
	outputFormat.setStrideX(w);
	outputFormat.setStrideY(h);

    }

    /**
     * 
     * This does the real decoding work.
     */

    public boolean decode(Data inData, Data outData) {

	// this is equivalent to BandDecompress

	if (inData.getLength() > 0) {

	    // Log.e("EkhoTV", "Attempting Decode");

	    long start = System.currentTimeMillis();

	    fOurStore.DoFrame((VidData) inData, (VidData) outData, fOurStore);

	    long end = System.currentTimeMillis();

	    long delta = end - start;

	    // Log.e("EkhoTV", delta + "\t" + start);

	} else {

	    if (inData.getLength() == Data.EOM) {

		outData.setLength(Data.EOM);

	    }

	}

	outData.setPresentationTime(inData.getPresentationTime());

	// outData.setPresentationTime(0);

	return true;

    }

    /**
     * 
     * Returns a Format object describing the type of input data expected.
     * 
     * @see #decode
     */

    public Format getInputFormat() {

	return inputFormat;

    }

    /**
     * 
     * Returns a Format object describing the type of output produced by the
     * 
     * decode method.
     * 
     * @see #decode
     */

    public Format getOutputFormat() {

	return outputFormat;

    }

    public void setInputFormat(Format f) {

	if (f instanceof VidFormat) {

	    inputFormat = (VidFormat) ((VidFormat) f).clone();

	}
    }
}
