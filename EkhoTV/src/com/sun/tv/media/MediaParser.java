/*
 * @(#)MediaParser.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import java.io.IOException;

import javax.media.protocol.PullSourceStream;
import javax.media.protocol.Seekable;

import android.util.Log;

public abstract class MediaParser implements Parser {

    public final static int MAX_READ_LIMIT = 2048;
    protected final static int BYTE_LEN = 1;
    protected final static int INT_LEN = 4;
    protected final static int STRING_LEN = 4;
    protected final static int SHORT_LEN = 2;

    protected PullSourceStream pss;
    protected int currentIndex;
    protected int limit;
    protected int curPointer;
    protected byte Buffer[];

    protected Track trackList[];
    protected int numTracks;
    protected int totalBytes;
    protected boolean streamable;
    protected boolean interleaved;
    private boolean isSeekable;

    public MediaParser() {
	flushBuffer();
	currentIndex = 0;
	totalBytes = 0;
	streamable = false;
	interleaved = false;
	trackList = null;
	numTracks = 0;
	isSeekable = false;
    }

    public void setSourceStream(PullSourceStream in) {
	pss = in;
	if (pss instanceof Seekable)
	    isSeekable = true;
    }

    public abstract void readHeader() throws BadHeaderException, IOException;

    public boolean isStreamable() {
	return streamable;
    }

    public boolean isInterleaved() {
	return interleaved;
    }

    public Track[] getTracks() {
	return trackList;
    }

    public void addTrack(Track t) {
	Track newTrackList[] = trackList;
	int i = 0;

	numTracks++;
	trackList = new Track[numTracks];
	for (i = 0; i < (trackList.length - 1); i++) {
	    trackList[i] = newTrackList[i];
	}
	trackList[i] = t;
    }

    public int getLength() {
	return totalBytes;
    }

    //
    // Methods to deal with chunk/index.
    //
    public int getCurrentIndex() {
	return currentIndex;
    }

    //
    // Input Methods.
    // read a chunk of data into a buffer. All read methods
    // defined here is to allow data values to be read from
    // buffer.
    //
    protected void reReadStream() throws IOException {
	/**
	 ** Note: For non-seekable stream, don't use 2K buffer. Just use as much
	 * as it needs. 2K buffer is to reduce parsing time as parsing usually
	 * read only integer/byte/String. By reading 2K buffer, it reduces the
	 * read calls from the client to server. [cania, 12/5/97]
	 **/
	if (isSeekable) {
	    Buffer = new byte[MAX_READ_LIMIT];
	    limit = pss.read(Buffer, 0, Buffer.length);
	    curPointer = 0;
	}
    }

    protected void reReadStream(int min) throws IOException {
	if (isSeekable) {
	    Buffer = new byte[MAX_READ_LIMIT];
	    curPointer = 0;
	    limit = pss.read(Buffer, 0, Buffer.length);
	    while ((limit < min) && (limit != -1)) {
		// $ Log.e("EkhoTV", "min = " + min + " limit = " + limit);
		int byteRead = pss.read(Buffer, limit, (Buffer.length - limit));
		if (byteRead != -1)
		    limit += byteRead;
		else {
		    Log.e("EkhoTV", "byteRead = -1 ");
		    break;
		}
	    }
	}
    }

    private void flushBuffer() {
	limit = 0;
	curPointer = 0;
	Buffer = null;
    }

    private void printVar() {
	Log.e("EkhoTV", "limit = " + limit + " curPointer = " + curPointer);
    }

    protected int byteAvailable() {
	// $ System.out.print("byteAvailable: ");
	// $ printVar();
	if (limit < curPointer) {
	    return 0;
	}
	return (limit - curPointer);
    }

    protected int readInt() throws IOException {
	byte ia[] = new byte[INT_LEN];
	int result = 0;

	if (isSeekable) {
	    int lim;
	    int byteAvail = byteAvailable();
	    // $$ Log.e("EkhoTV", "readInt, byteAvail = " + byteAvail);

	    if (byteAvail > INT_LEN)
		lim = INT_LEN;
	    else
		lim = byteAvail;

	    if (byteAvail > 0) {
		System.arraycopy(Buffer, curPointer, ia, 0, lim);
		curPointer += lim;
	    }
	    if (byteAvail < INT_LEN) {
		reReadStream(INT_LEN - lim);
		System.arraycopy(Buffer, curPointer, ia, lim, INT_LEN - lim);
		curPointer += (INT_LEN - lim);
	    }

	} else {
	    /**
	     ** Note: This section is used by non-seekable stream. "While" loop
	     * is to make sure the buffer, ia, is filled with 4 (INT_LEN) bytes.
	     * [Cania, 12/5/97]
	     **/
	    int readByte = 0, limit = INT_LEN;
	    int totalByte = 0, offset = 0;
	    while (limit > 0) {
		readByte = pss.read(ia, offset, limit);
		if (readByte == -1)
		    break;
		totalByte += readByte;
		offset += readByte;
		limit = INT_LEN - totalByte;
	    }
	}

	// Convert byte array to integer.
	for (int j = 0; j < INT_LEN; j++) {
	    result = result << 8;
	    result |= (ia[j] & 0xFF);
	}
	return result;
    }

    protected byte readByte() throws IOException {

	byte result[] = new byte[BYTE_LEN];
	if (isSeekable) {
	    int byteAvail = byteAvailable();

	    if (byteAvail < BYTE_LEN)
		reReadStream(BYTE_LEN);
	    result[0] = Buffer[curPointer++];
	} else {
	    pss.read(result, 0, BYTE_LEN);
	}
	return result[0];
    }

    protected String readString() throws IOException {
	byte hdr[] = new byte[STRING_LEN];
	String str = null;

	if (isSeekable) {
	    int byteAvail = byteAvailable();
	    int lim;
	    // $$ Log.e("EkhoTV", "readString, byteAvail = " + byteAvail);
	    if (byteAvail > STRING_LEN)
		lim = STRING_LEN;
	    else
		lim = byteAvail;
	    if (byteAvail > 0) {
		System.arraycopy(Buffer, curPointer, hdr, 0, lim);
		curPointer += lim;
	    }

	    if (byteAvail < STRING_LEN) {
		reReadStream(STRING_LEN - lim);
		System.arraycopy(Buffer, curPointer, hdr, lim, STRING_LEN - lim);
		curPointer += (STRING_LEN - lim);
	    }
	} else {
	    /**
	     ** Note: This section is used by non-seekable stream. "While" loop
	     * is to make sure the buffer, hdr, is filled with 4 (STRING_LENG)
	     * bytes. [Cania, 12/5/97]
	     **/
	    int readByte = 0, limit = STRING_LEN;
	    int totalByte = 0, offset = 0;
	    while (limit > 0) {
		readByte = pss.read(hdr, offset, limit);
		if (readByte == -1)
		    break;
		totalByte += readByte;
		offset += readByte;
		limit = STRING_LEN - totalByte;
	    }
	}
	// Convert byte array to String type.
	str = new String(hdr);
	// $ Log.e("EkhoTV", "readString = " + str);
	return str;
    }

    protected int skipBytes(int skip) throws IOException {
	int NeedToSkip = skip;
	int TotalSkip = 0;

	if (isSeekable) {
	    int byteAvail = byteAvailable();
	    // $ Log.e("EkhoTV", "skipBytes: byteAvail = " + byteAvail +
	    // " skip = " + skip);
	    while (NeedToSkip > 0) {
		if (byteAvail <= NeedToSkip) {
		    NeedToSkip -= byteAvail;
		    TotalSkip += byteAvail;

		    long npos, ppos;
		    // $$ TODO: check for ClassCastException
		    ppos = ((Seekable) pss).tell();
		    // ((MediaPullSourceStream) pss).skip(NeedToSkip);
		    if (NeedToSkip != 0)
			((Seekable) pss).seek(ppos + NeedToSkip);
		    npos = ((Seekable) pss).tell();

		    // $$ Sanity Check
		    if ((npos - ppos) != NeedToSkip) {
			/**
			 * System.out.print("%%%%% ERROR in skipBytes");
			 * System.out.print(", NeedToSkip = " + NeedToSkip);
			 * System.out.print(", actual = " + (npos - ppos));
			 * System.out.print(", npos = " + npos); Log.e("EkhoTV",
			 * ", ppos = " + ppos);
			 **/
			throw new IOException("skipBytes error");
		    }
		    TotalSkip += (npos - ppos);
		    flushBuffer();
		    NeedToSkip = 0;
		} else {
		    curPointer += (NeedToSkip);
		    TotalSkip += NeedToSkip;
		    NeedToSkip = 0;
		}
	    }
	} else {
	    /**
	     ** This section is used by non-seekable stream. For non-seekable
	     * stream, skipByte is implemented by reading byte array. It will
	     * keep reading until NeedToSkip is zero or EOF (-1) is return from
	     * the stream. [Cania, 12/5/97]
	     **/
	    byte tBuffer[];
	    int readByte = 0;

	    while (NeedToSkip > 0) {
		if (NeedToSkip > MAX_READ_LIMIT)
		    tBuffer = new byte[MAX_READ_LIMIT];
		else
		    tBuffer = new byte[NeedToSkip];
		readByte = pss.read(tBuffer, 0, tBuffer.length);
		if (readByte == -1)
		    break;
		TotalSkip += readByte;
		NeedToSkip -= readByte;
	    }
	}
	return TotalSkip;
    }

    protected short readShort() throws IOException {
	short result = 0;
	byte buf[] = new byte[SHORT_LEN];

	if (isSeekable) {
	    int lim;
	    int byteAvail = byteAvailable();

	    if (byteAvail < SHORT_LEN)
		lim = byteAvail;
	    else
		lim = SHORT_LEN;

	    if (byteAvail > 0) {
		System.arraycopy(Buffer, curPointer, buf, 0, lim);
		curPointer += lim;
	    }

	    if (byteAvail < SHORT_LEN) {
		reReadStream(SHORT_LEN - lim);
		System.arraycopy(Buffer, curPointer, buf, lim, SHORT_LEN - lim);
		curPointer += (SHORT_LEN - lim);

	    }
	} else {
	    /**
	     ** Note: This section is used by non-seekable stream. "While" loop
	     * is to make sure the buffer, buf, is filled with 2 (SHORT_LEN)
	     * bytes. [Cania, 12/5/97]
	     **/
	    int totalByte = 0, readByte = 0;
	    int offset = 0, limit = SHORT_LEN;
	    while (limit > 0) {
		readByte = pss.read(buf, offset, limit);
		if (readByte == -1)
		    break;
		totalByte += readByte;
		offset += readByte;
		limit = SHORT_LEN - totalByte;
	    }
	}

	// Convert byte array to short type.
	for (int j = 0; j < SHORT_LEN; j++) {
	    result = (short) (result << 8);
	    result |= (buf[j] & 0xFF);
	}

	return result;
    }

    public void dispose() {
	// $ Log.e("EkhoTV", "MediaParser calls dispose(), trackList.length = "
	// + trackList.length);
	if (trackList != null) {
	    for (int i = 0; i < trackList.length; i++) {
		trackList[i].dispose();
	    }
	}
    }
}
