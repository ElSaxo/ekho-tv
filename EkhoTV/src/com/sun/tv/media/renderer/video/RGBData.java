/*
 * @(#)RGBData.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.renderer.video;

import com.sun.tv.media.Format;
import com.sun.tv.media.VidData;
import com.sun.tv.media.format.video.RGBFormat;
import com.sun.tv.media.format.video.VidFormat;

public class RGBData extends VidData {

    public RGBData(Format f) {
	RGBFormat fmt = (RGBFormat) f;
	this.format = (VidFormat) fmt.clone();

	allocateBuffer();
    }
}
