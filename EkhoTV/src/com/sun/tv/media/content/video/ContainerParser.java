/*
 * @(#)ContainerParser.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.content.video;

import java.io.IOException;

import javax.media.protocol.PullSourceStream;

import com.sun.tv.media.BadHeaderException;
import com.sun.tv.media.MediaParser;

public abstract class ContainerParser extends MediaParser {

    public static final int BYTE = 8; // 1 Byte = 8 bits

    // This function is to read the header/resource information, and
    // then set the VideoFormat's fields and AudioFormat's fields.
    // Note: The subclass is required to implement readHeader function.
    public abstract void readHeader() throws BadHeaderException, IOException;

    public ContainerParser() {
	super();
    }

    public void setSourceStream(PullSourceStream in) {
	super.setSourceStream(in);
    }

    protected int Swap16(short i) {
	int b1, b2;
	int swapped = 0;

	b1 = i & 0x0FF00;
	b2 = i & 0xFF;

	swapped = ((b1 >>> BYTE) | (b2 << BYTE));

	return swapped;
    }

    protected int Swap32(int i) {
	int b1, b2, b3, b4;
	int swapped = 0;

	b1 = i & 0xFF000000;
	b2 = i & 0xFF0000;
	b3 = i & 0xFF00;
	b4 = i & 0xFF;

	swapped = ((b1 >>> (3 * BYTE)) | (b2 >> BYTE) | (b3 << BYTE) | (b4 << (3 * BYTE)));
	return (int) swapped;
    }

    protected int MulDiv32(int t1, int t2, int bt) {

	float temp;
	long prod;

	if ((t1 == 0) || (t2 == 0) || (bt == 0))
	    return 0;
	if (bt == t1)
	    return t2;
	if (bt == t2)
	    return t1;

	temp = (float) t1 / (float) bt;
	temp *= t2;
	prod = (int) temp;

	return (int) prod;
    }
}
