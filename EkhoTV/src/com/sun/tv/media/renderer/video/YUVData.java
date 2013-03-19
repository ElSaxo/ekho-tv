/*
 * @(#)YUVData.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.renderer.video;

import com.sun.tv.media.Format;
import com.sun.tv.media.VidData;
import com.sun.tv.media.format.video.VidFormat;
import com.sun.tv.media.format.video.YUVFormat;

public class YUVData extends VidData {
    /**
     * blocks is a sequence of 5 integer structures. blocks[0] = offset into the
     * frame array blocks[1] = x coordinate of top left pixel of the block
     * blocks[2] = y coordinate blocks[3] = width of the block blocks[4] =
     * height of the block blocks[5] = offset of next block ... and so on
     */
    public int[] blocks;
    public int nBlocks;
    int fullUpdate;

    public YUVData(Format format) {
	YUVFormat fmt = (YUVFormat) format;
	this.format = (VidFormat) fmt.clone();

	allocateBuffer();

	blocks = new int[(fmt.getWidth() * fmt.getHeight() * 5) / 64];
	nBlocks = 0;
	fullUpdate = 0;
    }
}
