/*
 * @(#)MediaInputStream.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

/**
 * MediaInputStream
 * An MediaInputStream is an abstract base class for 
 * AudioContainerInputStream, VideoContainerInputStream.
 *
 * @Version 1.1 96/11/14
 */

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class MediaInputStream extends FilterInputStream {

    public final static int MAX_READ_LIMIT = 2048;
    protected final static int BYTE_LEN = 1;
    protected final static int INT_LEN = 4;
    protected final static int STRING_LEN = 4;
    protected final static int SHORT_LEN = 2;

    protected int limit;
    protected int curPointer;
    protected BufferedInputStream bis;
    protected byte Buffer[];

    protected long lDataPos; // Position After readAndSetHeader() call
    protected Format f[];
    protected int fcount;

    // protected int openStream;
    // protected int availableBytes;
    // protected int bufferCount;

    // public final static int CLOSED_END = 0;
    // public final static int TWO_END = 1;
    // public final static int STREAM_ERROR = -1;

    public MediaInputStream(InputStream in) {
	super(in);

	// bufferCount = 0;
	limit = 0;
	curPointer = 0;
	lDataPos = 0;
    }

    protected void reReadStream() throws IOException {
	Buffer = new byte[MAX_READ_LIMIT];

	bis = new BufferedInputStream(in, MAX_READ_LIMIT);
	limit = bis.read(Buffer, 0, Buffer.length);
	// bufferCount += limit;
	curPointer = 0;
	lDataPos += limit;
    }

    public abstract Format[] getFormat();

    protected abstract void setFormat(Format theFormat);

    // It returns the amount of data is being read.
    public long getDataPos() {
	return lDataPos;
    }

    /*
     * REMOVE // It is used to read bytes from the input stream and // converts
     * them into an integer array, buffer[]. public synchronized int read(int
     * buffer[], int len) throws IOException {
     * 
     * int num = 0; int numInt = len / 4; int remain = 0;
     * 
     * for (int i = 0; i < numInt; i++) { buffer[i] = 0; for (int k = 0; k < 4;
     * k++) { buffer[i] = (buffer[i] << 8) | in.read(); } num+=4; } remain = len
     * - ( 4 * numInt);
     * 
     * if (remain == 0) return num;
     * 
     * buffer[numInt] = 0; for (int i = 0; i < remain ; i++ ) { buffer[numInt] =
     * (buffer[numInt] << 8) | in.read(); num ++; } return num; }
     */

    // It is used to read bytes from the input stream
    // reads the requested number of bytes from the input stream.
    public synchronized int readFully(byte buffer[], int off, int numBytes)
	    throws IOException {

	// Note: The maximum numByte > 2048, the following code
	// doesn't work. [ cania 1/17/97]
	// num = in.read(buffer, 0, numBytes);
	// Note: Instead of read byte by byte, read the maximum chunks at one
	// time.
	// Continue read until the requested number of bytes are copied into
	// buffer.
	// for (num = 0, i = 0; i < numBytes; i++ ) {
	// buffer[i] = (byte) in.read();
	// num++;
	// }
	int num = off;
	int readByte = 0;
	int readCount = 0;

	if (numBytes >= MAX_READ_LIMIT) {
	    readCount = MAX_READ_LIMIT;
	} else {
	    readCount = numBytes;
	}
	try {
	    while (num < numBytes) {
		readByte = in.read(buffer, num, readCount);
		if (readByte == -1) {
		    if (num == off)
			return -1;
		    else
			break;
		}
		num += readByte;
		if ((numBytes - num) >= MAX_READ_LIMIT) {
		    readCount = MAX_READ_LIMIT;
		} else {
		    readCount = numBytes - num;
		}
	    }
	} catch (IOException e) {
	    num = -1;
	    throw new IOException(e.getMessage());
	}
	return num;
    }

    // Buffer up 1k byte and parse the data later. It will reduce
    // traffic for http protocol.

    protected int byteAvailable() {
	if (limit < curPointer)
	    return 0;
	return (limit - curPointer);
    }

    protected int readInt() throws IOException {
	int i, ia[], j, lim;
	int byteAvail = byteAvailable();

	ia = new int[INT_LEN];
	if (byteAvail > INT_LEN)
	    lim = INT_LEN;
	else
	    lim = byteAvail;

	for (j = 0; j < lim; j++)
	    ia[j] = Buffer[curPointer++] & 0xFF;

	if (byteAvail < INT_LEN) {
	    reReadStream();
	    for (int k = 0; k < (INT_LEN - lim); k++)
		ia[j + k] = Buffer[curPointer++] & 0xFF;
	}
	i = ((ia[0] << 24) | (ia[1] << 16) | (ia[2] << 8) | ia[3]);
	// lDataPos += INT_LEN;
	return i;
    }

    protected byte readByte() throws IOException {
	int byteAvail = byteAvailable();
	byte result;

	if (byteAvail < BYTE_LEN)
	    reReadStream();
	result = Buffer[curPointer++];

	return result;
    }

    protected String readString() throws IOException {
	byte hdr[] = new byte[STRING_LEN];
	String str = null;
	int j, lim;

	int byteAvail = byteAvailable();
	if (byteAvail > STRING_LEN)
	    lim = STRING_LEN;
	else
	    lim = byteAvail;

	for (j = 0; j < lim; j++)
	    hdr[j] = Buffer[curPointer++];

	if (byteAvail < STRING_LEN) {
	    reReadStream();
	    for (int k = 0; k < (STRING_LEN - lim); k++)
		hdr[j + k] = Buffer[curPointer++];

	}
	str = new String(hdr);
	// lDataPos += STRING_LEN;
	return str;
    }

    protected int skipBytes(int skip) throws IOException {
	int NeedToSkip = skip;
	int TotalSkip = 0;
	int byteAvail = byteAvailable();

	while (NeedToSkip > 0) {
	    if (byteAvail <= NeedToSkip) {
		NeedToSkip -= byteAvail;
		TotalSkip += byteAvail;
		reReadStream();
		byteAvail = byteAvailable();
	    } else {
		curPointer += (NeedToSkip);
		TotalSkip += NeedToSkip;
		NeedToSkip = 0;
	    }
	}

	// lDataPos += TotalSkip;
	return TotalSkip;
    }

    protected short readShort() throws IOException {
	short result;
	int lim, j;
	int byteAvail = byteAvailable();
	short s[] = new short[SHORT_LEN];

	if (byteAvail < SHORT_LEN)
	    lim = byteAvail;
	else
	    lim = SHORT_LEN;

	for (j = 0; j < lim; j++)
	    s[j] = Buffer[curPointer++];

	if (byteAvail < SHORT_LEN) {
	    reReadStream();
	    for (int k = 0; k < (SHORT_LEN - lim); k++)
		s[j + k] = Buffer[curPointer++];
	}
	result = (short) ((s[0] << 8) | s[1]);
	// lDataPos += SHORT_LEN;
	return result;
    }

}
