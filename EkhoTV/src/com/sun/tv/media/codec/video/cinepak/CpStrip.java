/*
 * @(#)CpStrip.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.codec.video.cinepak;


public class CpStrip {

    public CpStrip() {

	fSizeOfStrip = 0;

	fx0 = 0;

	fy0 = 0;

	fx1 = 0;

	fy1 = 0;

	fCID = 0;

	Detail = new CodeEntry[256];

	Smooth = new CodeEntry[256];

	for (int i = 0; i < 256; i++) {

	    Detail[i] = new CodeEntry();

	    Smooth[i] = new CodeEntry();

	}

    }

    private int fSizeOfStrip;

    private int fx0;

    private int fy0;

    private int fx1;

    private int fy1;

    private int fCID;

    public CodeEntry[] Detail;

    public CodeEntry[] Smooth;

}
