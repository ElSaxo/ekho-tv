/*
 * @(#)CodeEntry.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.codec.video.cinepak;

public class CodeEntry {

    // copy constructor

    public CodeEntry(CodeEntry fromCode) {

	// your basic copy constructor

	aRGB0 = fromCode.aRGB0;

	aRGB1 = fromCode.aRGB1;

	aRGB2 = fromCode.aRGB2;

	aRGB3 = fromCode.aRGB3;

    }

    // from nothing constructor

    public CodeEntry() {

	aRGB0 = 0x00a3a3a3;

	aRGB1 = 0x00a3a3a3;

	aRGB2 = 0x00a3a3a3;

	aRGB3 = 0x00a3a3a3;

    }

    int aRGB0;

    int aRGB1;

    int aRGB2;

    int aRGB3;

}
