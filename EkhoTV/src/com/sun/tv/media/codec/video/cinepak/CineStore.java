/*
 * @(#)CineStore.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.codec.video.cinepak;

import com.sun.tv.media.VidData;
import com.sun.tv.media.renderer.video.PseudoToRGB;

public class CineStore {

    public static final int MAXSTRIPS = 5;

    // This table has 48X24 entries = 6 X 8 X 3 X 8 = 128 X 9 = 256 * 4.5 ?

    // looks like 9*8 = 128 entries of 0x00, then 256 entries of real, then 64
    // entries of 0xFF

    public static final int[] BOUNDING24 = {

    0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, // underflow 1

	    0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, // 2

	    0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, // 3

	    0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, // 4

	    0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, // 5

	    0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, // 6

	    0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, // 7

	    0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, // 8

	    0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, // underflow
								    // 1

	    0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, // 2

	    0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, // 3

	    0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, // 4

	    0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, // 5

	    0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, // 6

	    0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, // 7

	    0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, // 8

	    0x000, 0x001, 0x002, 0x003, 0x004, 0x005, 0x006, 0x007, // 9

	    0x008, 0x009, 0x00a, 0x00b, 0x00c, 0x00d, 0x00e, 0x00f, // 10

	    0x010, 0x011, 0x012, 0x013, 0x014, 0x015, 0x016, 0x017, // 11

	    0x018, 0x019, 0x01a, 0x01b, 0x01c, 0x01d, 0x01e, 0x01f, // 12

	    0x020, 0x021, 0x022, 0x023, 0x024, 0x025, 0x026, 0x027, // 13

	    0x028, 0x029, 0x02a, 0x02b, 0x02c, 0x02d, 0x02e, 0x02f, // 14

	    0x030, 0x031, 0x032, 0x033, 0x034, 0x035, 0x036, 0x037, // 15

	    0x038, 0x039, 0x03a, 0x03b, 0x03c, 0x03d, 0x03e, 0x03f, // 16

	    0x040, 0x041, 0x042, 0x043, 0x044, 0x045, 0x046, 0x047, // 17

	    0x048, 0x049, 0x04a, 0x04b, 0x04c, 0x04d, 0x04e, 0x04f, // 18

	    0x050, 0x051, 0x052, 0x053, 0x054, 0x055, 0x056, 0x057, // 19

	    0x058, 0x059, 0x05a, 0x05b, 0x05c, 0x05d, 0x05e, 0x05f, // 20

	    0x060, 0x061, 0x062, 0x063, 0x064, 0x065, 0x066, 0x067, // 21

	    0x068, 0x069, 0x06a, 0x06b, 0x06c, 0x06d, 0x06e, 0x06f, // 22

	    0x070, 0x071, 0x072, 0x073, 0x074, 0x075, 0x076, 0x077, // 23

	    0x078, 0x079, 0x07a, 0x07b, 0x07c, 0x07d, 0x07e, 0x07f, // 24

	    0x080, 0x081, 0x082, 0x083, 0x084, 0x085, 0x086, 0x087, // 25

	    0x088, 0x089, 0x08a, 0x08b, 0x08c, 0x08d, 0x08e, 0x08f, // 26

	    0x090, 0x091, 0x092, 0x093, 0x094, 0x095, 0x096, 0x097, // 27

	    0x098, 0x099, 0x09a, 0x09b, 0x09c, 0x09d, 0x09e, 0x09f, // 28

	    0x0a0, 0x0a1, 0x0a2, 0x0a3, 0x0a4, 0x0a5, 0x0a6, 0x0a7, // 29

	    0x0a8, 0x0a9, 0x0aa, 0x0ab, 0x0ac, 0x0ad, 0x0ae, 0x0af, // 30

	    0x0b0, 0x0b1, 0x0b2, 0x0b3, 0x0b4, 0x0b5, 0x0b6, 0x0b7, // 31

	    0x0b8, 0x0b9, 0x0ba, 0x0bb, 0x0bc, 0x0bd, 0x0be, 0x0bf, // 32

	    0x0c0, 0x0c1, 0x0c2, 0x0c3, 0x0c4, 0x0c5, 0x0c6, 0x0c7, // 33

	    0x0c8, 0x0c9, 0x0ca, 0x0cb, 0x0cc, 0x0cd, 0x0ce, 0x0cf, // 34

	    0x0d0, 0x0d1, 0x0d2, 0x0d3, 0x0d4, 0x0d5, 0x0d6, 0x0d7, // 35

	    0x0d8, 0x0d9, 0x0da, 0x0db, 0x0dc, 0x0dd, 0x0de, 0x0df, // 36

	    0x0e0, 0x0e1, 0x0e2, 0x0e3, 0x0e4, 0x0e5, 0x0e6, 0x0e7, // 37

	    0x0e8, 0x0e9, 0x0ea, 0x0eb, 0x0ec, 0x0ed, 0x0ee, 0x0ef, // 38

	    0x0f0, 0x0f1, 0x0f2, 0x0f3, 0x0f4, 0x0f5, 0x0f6, 0x0f7, // 39

	    0x0f8, 0x0f9, 0x0fa, 0x0fb, 0x0fc, 0x0fd, 0x0fe, 0x0ff, // 40

	    0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, // 41

	    // overflow

	    0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, // 42

	    0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, // 43

	    0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, // 44

	    0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, // 45

	    0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, // 46

	    0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, // 47

	    0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, // 48

	    0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, // 42

	    0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, // 43

	    0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, // 44

	    0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, // 45

	    0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, // 46

	    0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, // 47

	    0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff, 0x0ff // 48

    };

    // nothing constructor

    public CineStore() {

	StripVec = new CpStrip[MAXSTRIPS];

	// Log.e("EkhoTV", "\t\tThenew strip vector is " + StripVec+ "\n" );

	NumStrips = 0;

	ImageSizeX = 0;

	ImageSizeY = 0;

	ImagePosX = 0;

	ImagePosY = 0;

	StripPosX = 0;

	StripPosY = 0;

	StripPosX1 = 0;

	StripPosY1 = 0;

	fOurChunk = new CPChunk();

    }

    public void setColorConverter(PseudoToRGB cc) {
	fOurChunk.setLookup(cc.getCmap());
    }

    public void addStrip() {

	// Log.e("EkhoTV", "\t\tNow adding strip to " + this+ "\n" );

	if (NumStrips < MAXSTRIPS) {

	    CpStrip tempStrip;

	    tempStrip = new CpStrip();

	    // Log.e("EkhoTV", "\t\tnew strip is " + tempStrip + "\n" );

	    StripVec[NumStrips] = tempStrip;

	    NumStrips++;

	}

    }

    public int getNumStrips() {

	return NumStrips;

    }

    // original Cinestore vars

    public CpStrip[] StripVec;

    public int NumStrips;

    public int ImageSizeX;

    public int ImageSizeY;

    public int ImagePosX;

    public int ImagePosY;

    public int StripPosX;

    public int StripPosY;

    public int StripPosX1;

    public int StripPosY1;

    // CineFrame vars

    private boolean finitialCpFlags;

    private int fTopSize;

    private int fXsize;

    private int fYsize;

    private int fNoOfStrips;

    private CineStore fCPStore;

    private byte[] fInBuffer;

    private int fInBufLen;

    private CPChunk fOurChunk;

    public void DoFrame(VidData inData, VidData outData, CineStore myStor) {

	// decode((VidData)inData, (VidData)outData);

	int stripCount;

	byte inByte1;

	// make our local copy of the storage object

	fCPStore = myStor;

	// get the buffer into an array of bytes

	// returns length of data in buffer

	fInBufLen = inData.getLength();

	int bufferLength = inData.getBufferSize();

	int bufferLengthO = outData.getBufferSize();

	// returns actual buffer object... then cast into array of bytes

	fInBuffer = (byte[]) inData.getBuffer();

	// read various header parts

	// calculate check sum

	// int sum = 0;

	// for(int i=0;i<fInBufLen;i++)

	// sum = sum + fInBuffer[i];

	//

	// System.err.println("Checksum = " +sum );

	// print first 10 bytes..

	// for(int i=0;i<15;i++)

	// System.err.print(" " + fInBuffer[i]);

	// System.err.println(" ");

	if ((fInBuffer[0] & 0x01) == 1)

	    finitialCpFlags = false;

	else

	    finitialCpFlags = true;

	// read atom length - 3

	// first convert inbytes to length number

	// inByte1 = inByte1 * 256 + is.read();

	fTopSize = ((fInBuffer[1] & 0xFF) * 256 + (fInBuffer[2] & 0xFF) * 256)
		+ (fInBuffer[3] & 0xFF);

	// read x size - 2

	fXsize = (fInBuffer[4] & 0xFF) * 256 + (fInBuffer[5] & 0xFF);

	// read y size - 2

	fYsize = (fInBuffer[6] & 0xFF) * 256 + (fInBuffer[7] & 0xFF);

	outData.setLength(fXsize * fYsize);

	myStor.ImageSizeX = fXsize;

	myStor.ImageSizeY = fYsize;

	// read strip# - 2

	fNoOfStrips = (fInBuffer[8] & 0xFF) * 256 + (fInBuffer[9] & 0xFF);

	// System.err.println( "NowStartingDecode of Frame" );

	// System.err.println( "JMF is giving us      "+
	// fInBufLen+" bytes to work with" );

	// System.err.println( "Cinepak says frame is "+ fTopSize +" bytes long"
	// );

	// System.err.println( "        The Buffer is "+
	// bufferLength*4+"bytes long ");

	// System.err.println( " The output Buffer is "+
	// bufferLengthO*4+"bytes long ");

	// System.err.println(
	// "There are "+fNoOfStrips+" strips in this "+fXsize+" by "+fYsize+" frame "
	// );

	// check that there are enough strips inited..

	{

	    while (fCPStore.getNumStrips() < fNoOfStrips) {

		// System.err.println( "creating strip" );

		fCPStore.addStrip();

	    }

	}

	// n is fNoOfStrips

	// read 1-n number of strips

	int StripStartInBuffer = 10;

	int StripEndInBuffer = 10;

	for (stripCount = 0; stripCount < fNoOfStrips; stripCount++) {

	    int cid;

	    int y0;

	    int x0;

	    int y1;

	    int x1;

	    int sizeOfStrip;

	    int ByteCount;

	    // System.err.println( "\t StripNumber "+stripCount);

	    // read cid - 2

	    cid = (fInBuffer[StripStartInBuffer] & 0xFF) * 256
		    + (fInBuffer[StripStartInBuffer + 1] & 0xFF);

	    // sizeOfChunk

	    sizeOfStrip = (fInBuffer[StripStartInBuffer + 2] & 0xFF) * 256
		    + (fInBuffer[StripStartInBuffer + 3] & 0xFF);

	    StripEndInBuffer = StripStartInBuffer + sizeOfStrip;

	    // read y0 - 2

	    y0 = (fInBuffer[StripStartInBuffer + 4] & 0xFF) * 256
		    + (fInBuffer[StripStartInBuffer + 5] & 0xFF);

	    // read x0 - 2

	    x0 = (fInBuffer[StripStartInBuffer + 6] & 0xFF) * 256
		    + (fInBuffer[StripStartInBuffer + 7] & 0xFF);

	    if (stripCount == 0) {

		myStor.StripPosX = x0;

		myStor.StripPosY = y0;

	    } else {

		myStor.StripPosX = x0;

		myStor.StripPosY = myStor.StripPosY + myStor.StripPosY1 + y0;

	    }

	    // read y1 - 2

	    y1 = (fInBuffer[StripStartInBuffer + 8] & 0xFF) * 256
		    + (fInBuffer[StripStartInBuffer + 9] & 0xFF);

	    // read x1 - 2

	    x1 = (fInBuffer[StripStartInBuffer + 10] & 0xFF) * 256
		    + (fInBuffer[StripStartInBuffer + 11] & 0xFF);

	    myStor.StripPosX1 = x1;

	    myStor.StripPosY1 = y1;

	    //

	    // System.err.println( "\t strip cid "+cid);

	    // System.err.println( "\t size is   "+sizeOfStrip);

	    // System.err.println( "\t strip x0 "+x0);

	    // System.err.println( "\t strip y0 "+y0);

	    // System.err.println( "\t strip x1 "+x1);

	    // System.err.println( "\t strip y1 "+y1);

	    // now check to see if we should copy the codebook over

	    // if we're the first strip the don't actually do it

	    if (finitialCpFlags) {

		if (cid > 0) {

		    // check to see that we're not the first strip..

		    if (stripCount > 0) {

			// System.err.println(
			// "Copying Strip Codebook over"+cid);

			for (int i = 0; i < 256; i++) {

			    myStor.StripVec[stripCount].Smooth[i] = new CodeEntry(
				    myStor.StripVec[stripCount - 1].Smooth[i]);

			    myStor.StripVec[stripCount].Detail[i] = new CodeEntry(
				    myStor.StripVec[stripCount - 1].Detail[i]);

			}

		    }

		}

	    }

	    // where m = have we reached end of sizeOfChunk;

	    // read 1-m number of chunks

	    // move the "start up

	    int ChunkStartInBuffer = StripStartInBuffer + 12;

	    while (ChunkStartInBuffer < StripEndInBuffer) {

		// System.err.println( "\tRead Chunk at " + ChunkStartInBuffer
		// );

		// System.err.println( "\tChunk type is " +
		// ourChunk.getChunkType());

		fOurChunk.processChunk(fInBuffer, fCPStore, stripCount,
			ChunkStartInBuffer, outData);

		ChunkStartInBuffer = ChunkStartInBuffer
			+ (fInBuffer[ChunkStartInBuffer + 2] & 0xFF) * 256
			+ (fInBuffer[ChunkStartInBuffer + 3] & 0xFF);

	    }

	    //

	    StripStartInBuffer = StripEndInBuffer;

	}

	//

	// System.err.println( "Finished with Frame ");

    }

}
