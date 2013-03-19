/*
 * @(#)CPChunk.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.codec.video.cinepak;

import com.sun.tv.media.VidData;

public class CPChunk {

    int fChunkType;

    int fChunkLen;

    int[] lookup;

    static int[] fBounding24;

    static int firstFlag;

    public CPChunk() {

	firstFlag = 1;

    }

    public CPChunk(int[] l) {
	lookup = l;
	firstFlag = 1;
    }

    public void setLookup(int[] l) {
	lookup = l;
    }

    public void processChunk(byte[] inBuffer, CineStore myStor, int whichStrip,
	    int ChunkStart, VidData outData) {

	int inByte1;

	byte[] ChunkArray;

	int numRead;

	fBounding24 = myStor.BOUNDING24;

	fChunkType = (inBuffer[ChunkStart] & 0xFF) * 256
		+ (inBuffer[ChunkStart + 1] & 0xFF);

	fChunkLen = (inBuffer[ChunkStart + 2] & 0xFF) * 256
		+ (inBuffer[ChunkStart + 3] & 0xFF);

	// System.err.println("\t\t Chunk type = " + getChunkType() );

	// System.err.println("\t\t Chunk length = " + fChunkLen );

	switch (fChunkType) {

	case 0x2000:

	    doCFUpdate(inBuffer, ChunkStart + 4,
		    myStor.StripVec[whichStrip].Detail);

	    // doCFDUpdate(inBuffer, ChunkStart +4,myStor,whichStrip,outData);

	    break;

	case 0x2200:

	    doCFUpdate(inBuffer, ChunkStart + 4,
		    myStor.StripVec[whichStrip].Smooth);

	    // doCFSUpdate(inBuffer, ChunkStart +4,myStor,whichStrip,outData);

	    break;

	case 0x2100:

	    doCPUpdate(inBuffer, ChunkStart + 4,
		    myStor.StripVec[whichStrip].Detail);

	    // doCPDUpdate(inBuffer, ChunkStart +4,myStor,whichStrip,outData);

	    break;

	case 0x2300:

	    doCPUpdate(inBuffer, ChunkStart + 4,
		    myStor.StripVec[whichStrip].Smooth);

	    // doCPSUpdate(inBuffer, ChunkStart +4,myStor,whichStrip,outData);

	    break;

	case 0x3000:

	    doFKUpdate(inBuffer, ChunkStart + 4, myStor, whichStrip, outData);

	    break;

	case 0x3200:

	    doFSKUpdate(inBuffer, ChunkStart + 4, myStor, whichStrip, outData);

	    break;

	case 0x3100:

	    doIUpdate(inBuffer, ChunkStart + 4, myStor, whichStrip, outData);

	    break;

	case 0x2400:

	    doGFUpdate(inBuffer, ChunkStart + 4,
		    myStor.StripVec[whichStrip].Detail);

	    // doGFSUpdate(inBuffer, ChunkStart +4,myStor,whichStrip,outData);

	    break;

	case 0x2600:

	    doGFUpdate(inBuffer, ChunkStart + 4,
		    myStor.StripVec[whichStrip].Smooth);

	    // doGFDUpdate(inBuffer, ChunkStart +4,myStor,whichStrip,outData);

	    break;

	case 0x2500:

	    doGPUpdate(inBuffer, ChunkStart + 4,
		    myStor.StripVec[whichStrip].Detail);

	    // doGPSUpdate(inBuffer, ChunkStart +4,myStor,whichStrip,outData);

	    break;

	case 0x2700:

	    doGPUpdate(inBuffer, ChunkStart + 4,
		    myStor.StripVec[whichStrip].Smooth);

	    // doGPDUpdate(inBuffer, ChunkStart +4,myStor,whichStrip,outData);

	    break;

	default:

	}

    }

    public String getChunkType() {

	switch (fChunkType) {

	case 0x2200:

	    return "color full    smooth codebook update";

	case 0x2000:

	    return "color full    detail codebook update";

	case 0x2300:

	    return "color partial smooth codebook update";

	case 0x2100:

	    return "color partial detail codebook update";

	case 0x3000:

	    return "full key frame update";

	case 0x3200:

	    return "full smooth key frame update";

	case 0x3100:

	    return "interframe update";

	case 0x2400:

	    return "greyscale full smooth codebook update";

	case 0x2600:

	    return "greyscale full detail codebook update";

	case 0x2500:

	    return "greyscale partial smooth codebook update";

	case 0x2700:

	    return "greyscale partial detail codebook update";

	default:

	    return "WARNING******* unknown atom chunk type...*******";

	}

    }

    public int getChunkLength() {

	return fChunkLen;

    }

    private void doCFUpdate(byte[] ChunkArray, int ChunkDataStart,
	    CodeEntry[] codebook) {

	int numberOfCodes;

	int i;

	numberOfCodes = ((ChunkArray[ChunkDataStart - 2] & 0xFF) * 256
		+ (ChunkArray[ChunkDataStart - 1] & 0xFF) - 4) / 6;

	for (i = 0; i < numberOfCodes; i++) {

	    int U;

	    int V;

	    int Y0;

	    int Y1;

	    int Y2;

	    int Y3;

	    int delR;

	    int delG;

	    int delB;

	    Y0 = (ChunkArray[ChunkDataStart + i * 6] & 0xFF);

	    Y1 = (ChunkArray[ChunkDataStart + i * 6 + 1] & 0xFF);

	    Y2 = (ChunkArray[ChunkDataStart + i * 6 + 2] & 0xFF);

	    Y3 = (ChunkArray[ChunkDataStart + i * 6 + 3] & 0xFF);

	    // funny thing is U and V are actually SIGNED bytes.... :-) yeah
	    // java works

	    U = ChunkArray[ChunkDataStart + i * 6 + 4];

	    V = ChunkArray[ChunkDataStart + i * 6 + 5];

	    delR = 2 * U + 128;

	    delB = 2 * V + 128;

	    delG = -(U / 2) - V + 128;

	    codebook[i].aRGB0 = (fBounding24[Y0 + delR] << 16)
		    + (fBounding24[Y0 + delG] << 8) + fBounding24[(Y0 + delB)];

	    codebook[i].aRGB1 = (fBounding24[Y1 + delR] << 16)
		    + (fBounding24[Y1 + delG] << 8) + fBounding24[(Y1 + delB)];

	    codebook[i].aRGB2 = (fBounding24[Y2 + delR] << 16)
		    + (fBounding24[Y2 + delG] << 8) + fBounding24[(Y2 + delB)];

	    codebook[i].aRGB3 = (fBounding24[Y3 + delR] << 16)
		    + (fBounding24[Y3 + delG] << 8) + fBounding24[(Y3 + delB)];

	}

    }

    private void doCPUpdate(byte[] ChunkArray, int ChunkDataStart,
	    CodeEntry[] codebook) {

	int ByteCounter;

	int CodeCount;

	ByteCounter = ChunkDataStart;

	CodeCount = 0;

	int len = ((ChunkArray[ChunkDataStart - 2] & 0xFF) * 256
		+ (ChunkArray[ChunkDataStart - 1] & 0xFF) - 4)
		+ ByteCounter;

	while ((ByteCounter < len) && (CodeCount < 256)) {

	    // unload the bit Map.

	    int Map = (ChunkArray[ByteCounter++] & 0xFF);

	    Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

	    Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

	    Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

	    // create mask

	    int Mask = 0x80000000;

	    for (int i = 0; ((i < 32) && (ByteCounter < len) && (CodeCount < 256)); i++) {

		if ((Mask & Map) != 0) {

		    int U;

		    int V;

		    int Y0;

		    int Y1;

		    int Y2;

		    int Y3;

		    int delR;

		    int delG;

		    int delB;

		    Y0 = (ChunkArray[ByteCounter++] & 0xFF);

		    Y1 = (ChunkArray[ByteCounter++] & 0xFF);

		    Y2 = (ChunkArray[ByteCounter++] & 0xFF);

		    Y3 = (ChunkArray[ByteCounter++] & 0xFF);

		    U = ChunkArray[ByteCounter++];

		    V = ChunkArray[ByteCounter++];

		    delR = 2 * U + 128;

		    delB = 2 * V + 128;

		    delG = -(U / 2) - V + 128;

		    codebook[CodeCount].aRGB0 = (fBounding24[Y0 + delR] << 16)
			    + (fBounding24[Y0 + delG] << 8)
			    + fBounding24[(Y0 + delB)];

		    codebook[CodeCount].aRGB1 = (fBounding24[Y1 + delR] << 16)
			    + (fBounding24[Y1 + delG] << 8)
			    + fBounding24[(Y1 + delB)];

		    codebook[CodeCount].aRGB2 = (fBounding24[Y2 + delR] << 16)
			    + (fBounding24[Y2 + delG] << 8)
			    + fBounding24[(Y2 + delB)];

		    codebook[CodeCount].aRGB3 = (fBounding24[Y3 + delR] << 16)
			    + (fBounding24[Y3 + delG] << 8)
			    + fBounding24[(Y3 + delB)];

		}

		Mask = Mask >>> 1;

		CodeCount++;

	    }

	}

    }

    private void doFKUpdate(byte[] ChunkArray, int ChunkDataStart,
	    CineStore myStor, int thisStrip, VidData outData) {

	int ByteCounter;

	int CodeCount;

	int xdraw;

	int ydraw;

	int[] outBuffer = (int[]) outData.getBuffer();

	int outWidth = outData.getWidth();
	;

	CpStrip theStrip = myStor.StripVec[thisStrip];

	CodeEntry[] detailBook = theStrip.Detail;

	CodeEntry[] smoothBook = theStrip.Smooth;

	int len = ((ChunkArray[ChunkDataStart - 2] & 0xFF) * 256
		+ (ChunkArray[ChunkDataStart - 1] & 0xFF) - 4)
		+ ChunkDataStart;

	xdraw = myStor.ImagePosX + myStor.StripPosX;

	ydraw = myStor.ImagePosY + myStor.StripPosY;

	// int blitCount =0;

	// int smoothCount =0;

	// int detailCount =0;

	CodeEntry thisCode;

	ByteCounter = ChunkDataStart;

	CodeCount = 0;

	while ((ByteCounter < len)
		&& (ydraw < myStor.ImagePosY + myStor.ImageSizeY)) {

	    // unload the bit Map.

	    int Map = (ChunkArray[ByteCounter++] & 0xFF);

	    Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

	    Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

	    Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

	    // create mask

	    int color;

	    int startLocation;

	    int location;

	    int Mask = 0x80000000;

	    for (int i = 0; ((i < 32) && (ByteCounter < len) && (ydraw < myStor.ImagePosY
		    + myStor.ImageSizeY)); i++) {

		if ((Mask & Map) != 0) {

		    // do a detail update with next four codes

		    thisCode = detailBook[(ChunkArray[ByteCounter++] & 0xFF)];

		    // draw a small 2X2 square..

		    // Get the color

		    color = thisCode.aRGB0;

		    startLocation = xdraw + outWidth * ydraw;

		    location = startLocation;

		    outBuffer[location] = color;

		    // Get the color

		    color = thisCode.aRGB1;

		    location = startLocation + 1;

		    outBuffer[location] = color;

		    // Get the color

		    color = thisCode.aRGB2;

		    location = startLocation + outWidth;

		    outBuffer[location] = color;

		    // Get the color

		    color = thisCode.aRGB3;

		    location = location + 1;

		    outBuffer[location] = color;

		    thisCode = detailBook[(ChunkArray[ByteCounter++] & 0xFF)];

		    // draw a small 2X2 square..

		    // Get the color

		    color = thisCode.aRGB0;

		    startLocation = xdraw + 2 + outWidth * ydraw;

		    location = startLocation;

		    outBuffer[location] = color;

		    // Get the color

		    color = thisCode.aRGB1;

		    location = startLocation + 1;

		    outBuffer[location] = color;

		    // Get the color

		    color = thisCode.aRGB2;

		    location = startLocation + outWidth;

		    outBuffer[location] = color;

		    // Get the color

		    color = thisCode.aRGB3;

		    location = location + 1;

		    outBuffer[location] = color;

		    thisCode = detailBook[(ChunkArray[ByteCounter++] & 0xFF)];

		    // draw a small 2X2 square..

		    // Get the color

		    color = thisCode.aRGB0;

		    startLocation = xdraw + outWidth * (ydraw + 2);

		    location = startLocation;

		    outBuffer[location] = color;

		    // Get the color

		    color = thisCode.aRGB1;

		    location = startLocation + 1;

		    outBuffer[location] = color;

		    // Get the color

		    color = thisCode.aRGB2;

		    location = startLocation + outWidth;

		    outBuffer[location] = color;

		    // Get the color

		    color = thisCode.aRGB3;

		    location = location + 1;

		    outBuffer[location] = color;

		    thisCode = detailBook[(ChunkArray[ByteCounter++] & 0xFF)];

		    // draw a small 2X2 square..

		    // Get the color

		    color = thisCode.aRGB0;

		    startLocation = xdraw + 2 + outWidth * (ydraw + 2);

		    location = startLocation;

		    outBuffer[location] = color;

		    // Get the color

		    color = thisCode.aRGB1;

		    location = startLocation + 1;

		    outBuffer[location] = color;

		    // Get the color

		    color = thisCode.aRGB2;

		    location = startLocation + outWidth;

		    outBuffer[location] = color;

		    // Get the color

		    color = thisCode.aRGB3;

		    outBuffer[location + 1] = color;

		    // detailCount++;

		} else {

		    // System.err.println("\t\t Counter = " +
		    // (ByteCounter-ChunkDataStart) + "   Len= "
		    // +(len-ChunkDataStart) + " data = " +
		    // ChunkArray[ByteCounter] );

		    // do a smooth update with next single code.

		    thisCode = smoothBook[(ChunkArray[ByteCounter++] & 0xFF)];

		    // Get the color

		    // draw a small 2X2 square..

		    color = thisCode.aRGB0;

		    startLocation = xdraw + outWidth * ydraw;

		    location = startLocation;

		    outBuffer[location] = color;

		    outBuffer[location + 1] = color;

		    location = location + outWidth;

		    outBuffer[location] = color;

		    outBuffer[location + 1] = color;

		    // Get the color

		    color = thisCode.aRGB1;

		    location = startLocation + 2;

		    outBuffer[location] = color;

		    outBuffer[location + 1] = color;

		    location = location + outWidth;

		    outBuffer[location] = color;

		    outBuffer[location + 1] = color;

		    // Get the color

		    color = thisCode.aRGB2;

		    location = startLocation = startLocation + outWidth * 2;

		    outBuffer[location] = color;

		    outBuffer[location + 1] = color;

		    location = location + outWidth;

		    outBuffer[location] = color;

		    outBuffer[location + 1] = color;

		    // Get the color

		    color = thisCode.aRGB3;

		    location = startLocation + 2;

		    outBuffer[location] = color;

		    outBuffer[location + 1] = color;

		    location = location + outWidth;

		    outBuffer[location] = color;

		    outBuffer[location + 1] = color;

		}

		xdraw = xdraw + 4;

		Mask = Mask >>> 1;

		CodeCount++;

		if (xdraw > myStor.ImageSizeX - 4) {

		    xdraw = myStor.ImagePosX + myStor.StripPosX;

		    ydraw = ydraw + 4;

		}

	    }

	}

    }

    private void doFSKUpdate(byte[] ChunkArray, int ChunkDataStart,
	    CineStore myStor, int thisStrip, VidData outData) {

	// in which we actually DRAW on the screen...

	int ByteCounter;

	int xdraw;

	int ydraw;

	int[] outBuffer = (int[]) outData.getBuffer();

	int outWidth = outData.getWidth();
	;

	CpStrip theStrip = myStor.StripVec[thisStrip];

	CodeEntry[] detailBook = theStrip.Detail;

	CodeEntry[] smoothBook = theStrip.Smooth;

	CodeEntry thisCode;

	int len = ((ChunkArray[ChunkDataStart - 2] & 0xFF) * 256
		+ (ChunkArray[ChunkDataStart - 1] & 0xFF) - 4)
		+ ChunkDataStart;

	xdraw = myStor.ImagePosX + myStor.StripPosX;

	ydraw = myStor.ImagePosY + myStor.StripPosY;

	int color;

	int startLocation;

	int location;

	ByteCounter = ChunkDataStart;

	while ((ByteCounter < len)) {

	    // decode each of these codes as a smooth block...

	    thisCode = smoothBook[(ChunkArray[ByteCounter++] & 0xFF)];

	    // Get the color

	    // draw a small 2X2 square..

	    color = thisCode.aRGB0;

	    startLocation = xdraw + outWidth * ydraw;

	    // -ivg
	    // Fixed an ArrayOutOfBoundException when playing
	    // 1984Cine25.mov.
	    if (startLocation >= outBuffer.length)
		break;

	    location = startLocation;

	    // -ivg
	    // This println demonstrates the crash with 1984Cine25.mov.
	    //
	    // if (location >= outBuffer.length) {
	    // System.err.println("outBuffer len = " + outBuffer.length);
	    // System.err.println("ByteCounter = " + ByteCounter + " len = " +
	    // len);
	    // System.err.println("xdraw = " + xdraw + " ydraw = " + ydraw +
	    // " outWidth = " + outWidth + " startLocation = " + startLocation +
	    // " location = " + location);
	    // }

	    outBuffer[location] = color;

	    outBuffer[location + 1] = color;

	    location = location + outWidth;

	    outBuffer[location] = color;

	    outBuffer[location + 1] = color;

	    // Get the color

	    color = thisCode.aRGB1;

	    location = startLocation + 2;

	    outBuffer[location] = color;

	    outBuffer[location + 1] = color;

	    location = location + outWidth;

	    outBuffer[location] = color;

	    outBuffer[location + 1] = color;

	    // Get the color

	    color = thisCode.aRGB2;

	    location = startLocation = startLocation + outWidth * 2;

	    outBuffer[location] = color;

	    outBuffer[location + 1] = color;

	    location = location + outWidth;

	    outBuffer[location] = color;

	    outBuffer[location + 1] = color;

	    // Get the color

	    color = thisCode.aRGB3;

	    location = startLocation + 2;

	    outBuffer[location] = color;

	    outBuffer[location + 1] = color;

	    location = location + outWidth;

	    outBuffer[location] = color;

	    outBuffer[location + 1] = color;

	    // move the x on to the next smooth block sized block

	    xdraw = xdraw + 4;

	    if (xdraw > myStor.ImageSizeX - 4) {

		xdraw = myStor.ImagePosX + myStor.StripPosX;

		ydraw = ydraw + 4;

	    }

	}

    }

    private void doIUpdate(byte[] ChunkArray, int ChunkDataStart,
	    CineStore myStor, int thisStrip, VidData outData) {

	// in which we actually DRAW on the screen...

	int ByteCounter;

	int xdraw;

	int ydraw;

	int[] outBuffer = (int[]) outData.getBuffer();

	int outWidth = outData.getWidth();

	int color;

	int startLocation;

	int location;

	CodeEntry[] detailBook = myStor.StripVec[thisStrip].Detail;

	CodeEntry[] smoothBook = myStor.StripVec[thisStrip].Smooth;

	CodeEntry thisCode;

	int len = ((ChunkArray[ChunkDataStart - 2] & 0xFF) * 256
		+ (ChunkArray[ChunkDataStart - 1] & 0xFF) - 4)
		+ ChunkDataStart;

	xdraw = myStor.ImagePosX + myStor.StripPosX;

	ydraw = myStor.ImagePosY + myStor.StripPosY;

	ByteCounter = ChunkDataStart;

	// Get initial Map and Mask...

	// int MaskCount = 0;

	int Map = 0;

	int Mask = 0x00000000;

	int FinishY = myStor.ImagePosY + myStor.StripPosY + myStor.StripPosY1;

	int code;

	while ((ByteCounter < len) && (ydraw < FinishY)) {

	    // Check to see if we're at the start of the Mask.. if so load the
	    // Map and Mask.

	    Mask = Mask >>> 1;

	    if (Mask == 0x0) {

		// Get new Map

		Map = (ChunkArray[ByteCounter++] & 0xFF);

		Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

		Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

		Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

		// Get new Mask

		Mask = 0x80000000;

	    }

	    // And the Map and Mask, check for 1's this indicates that we need
	    // to draw something...

	    if (((Mask & Map) != 0) && (ByteCounter < len)) {

		// Check to see if we're at the start of the Mask.. if so load
		// the Map and Mask.

		Mask = Mask >>> 1;

		if (Mask == 0x0) {

		    // Get new Map

		    Map = (ChunkArray[ByteCounter++] & 0xFF);

		    Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

		    Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

		    Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

		    // Get new Mask

		    Mask = 0x80000000;

		}

		// now check to see if the NEXT bit is 1 or zero.. 1's indicate
		// a detail block

		if ((Mask & Map) != 0) {

		    // one means detail block; this chunk writes out a detail
		    // block

		    // draw a small 2X2 square..

		    startLocation = xdraw + outWidth * ydraw;

		    thisCode = detailBook[(ChunkArray[ByteCounter++] & 0xFF)];

		    outBuffer[startLocation] = thisCode.aRGB0;

		    outBuffer[startLocation + 1] = thisCode.aRGB1;

		    outBuffer[startLocation + outWidth] = thisCode.aRGB2;

		    outBuffer[startLocation + outWidth + 1] = thisCode.aRGB3;

		    // draw a small 2X2 square..

		    startLocation = xdraw + 2 + outWidth * ydraw;

		    thisCode = detailBook[(ChunkArray[ByteCounter++] & 0xFF)];

		    outBuffer[startLocation] = thisCode.aRGB0;

		    outBuffer[startLocation + 1] = thisCode.aRGB1;

		    outBuffer[startLocation + outWidth] = thisCode.aRGB2;

		    outBuffer[startLocation + outWidth + 1] = thisCode.aRGB3;

		    // draw a small 2X2 square..

		    thisCode = detailBook[(ChunkArray[ByteCounter++] & 0xFF)];

		    startLocation = xdraw + outWidth * (ydraw + 2);

		    outBuffer[startLocation] = thisCode.aRGB0;

		    outBuffer[startLocation + 1] = thisCode.aRGB1;

		    outBuffer[startLocation + outWidth] = thisCode.aRGB2;

		    outBuffer[startLocation + outWidth + 1] = thisCode.aRGB3;

		    // draw a small 2X2 square..

		    thisCode = detailBook[(ChunkArray[ByteCounter++] & 0xFF)];

		    startLocation = xdraw + 2 + outWidth * (ydraw + 2);

		    outBuffer[startLocation] = thisCode.aRGB0;

		    outBuffer[startLocation + 1] = thisCode.aRGB1;

		    outBuffer[startLocation + outWidth] = thisCode.aRGB2;

		    outBuffer[startLocation + outWidth + 1] = thisCode.aRGB3;

		} else {

		    // zero means smooth block; this chunk writes out a smooth
		    // block

		    thisCode = smoothBook[(ChunkArray[ByteCounter++] & 0xFF)];

		    // Get the color

		    // draw a small 2X2 square..

		    color = thisCode.aRGB0;

		    startLocation = xdraw + outWidth * ydraw;

		    location = startLocation;

		    outBuffer[location] = color;

		    outBuffer[location + 1] = color;

		    location = location + outWidth;

		    outBuffer[location] = color;

		    outBuffer[location + 1] = color;

		    // Get the next color

		    color = thisCode.aRGB1;

		    location = startLocation + 2;

		    outBuffer[location] = color;

		    outBuffer[location + 1] = color;

		    location = location + outWidth;

		    outBuffer[location] = color;

		    outBuffer[location + 1] = color;

		    // Get the next color

		    color = thisCode.aRGB2;

		    startLocation = startLocation + outWidth * 2;

		    location = startLocation;

		    outBuffer[location] = color;

		    outBuffer[location + 1] = color;

		    location = location + outWidth;

		    outBuffer[location] = color;

		    outBuffer[location + 1] = color;

		    // Get the next color

		    color = thisCode.aRGB3;

		    location = startLocation + 2;

		    outBuffer[location] = color;

		    outBuffer[location + 1] = color;

		    location = location + outWidth;

		    outBuffer[location] = color;

		    outBuffer[location + 1] = color;

		}

	    } else {

		// just skip this smooth block

	    }

	    // Ok, increment s

	    // Mask = Mask >>> 1;

	    // if(Mask == 0x0) Mask = 0x80000000;

	    xdraw = xdraw + 4;

	    if (xdraw > myStor.ImageSizeX - 4) {

		xdraw = myStor.ImagePosX + myStor.StripPosX;

		ydraw = ydraw + 4;

	    }

	}

    }

    private void doGFUpdate(byte[] ChunkArray, int ChunkDataStart,
	    CodeEntry[] codebook) {

	int ByteCounter;

	int numberOfCodes;

	int i;

	ByteCounter = ChunkDataStart;

	numberOfCodes = ((ChunkArray[ChunkDataStart - 2] & 0xFF) * 256
		+ (ChunkArray[ChunkDataStart - 1] & 0xFF) - 4) / 4;

	int anInt;

	if (lookup == null) {
	    for (i = 0; i < numberOfCodes; i++) {

		if (firstFlag == 1) {

		    anInt = (ChunkArray[ByteCounter++] & 0xFF);

		    codebook[i].aRGB0 = (anInt << 16) + (anInt << 8) + anInt;

		    // System.err.println("\t\t " + i + " code, int = " + anInt
		    // +" first bookentry = " + codebook[i].aRGB0 );

		    anInt = (ChunkArray[ByteCounter++] & 0xFF);

		    codebook[i].aRGB1 = (anInt << 16) + (anInt << 8) + anInt;

		    anInt = (ChunkArray[ByteCounter++] & 0xFF);

		    codebook[i].aRGB2 = (anInt << 16) + (anInt << 8) + anInt;

		    anInt = (ChunkArray[ByteCounter++] & 0xFF);

		    codebook[i].aRGB3 = (anInt << 16) + (anInt << 8) + anInt;

		} else {

		    anInt = (ChunkArray[ByteCounter++] & 0xFF);

		    codebook[i].aRGB0 = (anInt << 16) | (anInt << 8) | anInt;

		    anInt = (ChunkArray[ByteCounter++] & 0xFF);

		    codebook[i].aRGB1 = (anInt << 16) | (anInt << 8) | anInt;

		    anInt = (ChunkArray[ByteCounter++] & 0xFF);

		    codebook[i].aRGB2 = (anInt << 16) | (anInt << 8) | anInt;

		    anInt = (ChunkArray[ByteCounter++] & 0xFF);

		    codebook[i].aRGB3 = (anInt << 16) | (anInt << 8) | anInt;

		}

	    }
	} else {
	    for (i = 0; i < numberOfCodes; i++) {

		if (firstFlag == 1) {

		    anInt = (ChunkArray[ByteCounter++] & 0xFF);

		    codebook[i].aRGB0 = lookup[anInt];

		    // System.err.println("\t\t " + i + " code, int = " + anInt
		    // +" first bookentry = " + codebook[i].aRGB0 );

		    anInt = (ChunkArray[ByteCounter++] & 0xFF);

		    codebook[i].aRGB1 = lookup[anInt];

		    anInt = (ChunkArray[ByteCounter++] & 0xFF);

		    codebook[i].aRGB2 = lookup[anInt];

		    anInt = (ChunkArray[ByteCounter++] & 0xFF);

		    codebook[i].aRGB3 = lookup[anInt];

		} else {

		    anInt = (ChunkArray[ByteCounter++] & 0xFF);

		    codebook[i].aRGB0 = lookup[anInt];

		    anInt = (ChunkArray[ByteCounter++] & 0xFF);

		    codebook[i].aRGB1 = lookup[anInt];

		    anInt = (ChunkArray[ByteCounter++] & 0xFF);

		    codebook[i].aRGB2 = lookup[anInt];

		    anInt = (ChunkArray[ByteCounter++] & 0xFF);

		    codebook[i].aRGB3 = lookup[anInt];

		}

	    }
	}

	firstFlag = 0;

    }

    private void doGPUpdate(byte[] ChunkArray, int ChunkDataStart,
	    CodeEntry[] codebook) {

	int ByteCounter;

	int CodeCount;

	ByteCounter = ChunkDataStart;

	CodeCount = 0;

	int len = ((ChunkArray[ChunkDataStart - 2] & 0xFF) * 256
		+ (ChunkArray[ChunkDataStart - 1] & 0xFF) - 4)
		+ ChunkDataStart;

	if (lookup == null) {
	    while ((ByteCounter < len) && (CodeCount < 256)) {

		// unload the bit Map.

		int Map = (ChunkArray[ByteCounter++] & 0xFF);

		Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

		Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

		Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

		// create mask

		int Mask = 0x80000000;

		int anInt;

		for (int i = 0; ((i < 32) && (ByteCounter < len) && (CodeCount < 256)); i++) {

		    if ((Mask & Map) != 0) {

			anInt = (ChunkArray[ByteCounter++] & 0xFF);

			codebook[i].aRGB0 = (anInt << 16) | (anInt << 8)
				| anInt;

			anInt = (ChunkArray[ByteCounter++] & 0xFF);

			codebook[i].aRGB1 = (anInt << 16) | (anInt << 8)
				| anInt;

			anInt = (ChunkArray[ByteCounter++] & 0xFF);

			codebook[i].aRGB2 = (anInt << 16) | (anInt << 8)
				| anInt;

			anInt = (ChunkArray[ByteCounter++] & 0xFF);

			codebook[i].aRGB3 = (anInt << 16) | (anInt << 8)
				| anInt;

		    }

		    Mask = Mask >>> 1;

		    CodeCount++;

		}

	    }
	} else {
	    while ((ByteCounter < len) && (CodeCount < 256)) {

		// unload the bit Map.

		int Map = (ChunkArray[ByteCounter++] & 0xFF);

		Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

		Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

		Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xFF);

		// create mask

		int Mask = 0x80000000;

		int anInt;

		for (int i = 0; ((i < 32) && (ByteCounter < len) && (CodeCount < 256)); i++) {

		    if ((Mask & Map) != 0) {

			anInt = (ChunkArray[ByteCounter++] & 0xFF);

			codebook[i].aRGB0 = lookup[anInt];

			anInt = (ChunkArray[ByteCounter++] & 0xFF);

			codebook[i].aRGB1 = lookup[anInt];

			anInt = (ChunkArray[ByteCounter++] & 0xFF);

			codebook[i].aRGB2 = lookup[anInt];

			anInt = (ChunkArray[ByteCounter++] & 0xFF);

			codebook[i].aRGB3 = lookup[anInt];

		    }

		    Mask = Mask >>> 1;

		    CodeCount++;

		}

	    }
	}

    }
}
