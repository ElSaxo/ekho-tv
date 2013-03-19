/*
 * @(#)IMA4.java	1.2 98/11/25
 *
 *  Licensed Materials - Property of IBM
 *  "Restricted Materials of IBM"
 *  5648-B81
 *  (c) Copyright IBM Corporation 1997,1998 All Rights Reserved
 *  US Government Users Restricted Rights - Use, duplication or
 *  disclosure restricted by GSA ADP Schedule Contract with
 *  IBM Corporation.
 *
 */

package com.sun.tv.media.codec.audio.ima4;

/**
 * IMA4 decoder
 * 
 * @author Shay Ben-David
 */

public class IMA4 {

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997,1998.";
    /* Intel ADPCM step variation table */
    public static int indexTable[] = { -1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1,
	    -1, 2, 4, 6, 8, };

    public static int stepsizeTable[] = { 7, 8, 9, 10, 11, 12, 13, 14, 16, 17,
	    19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 50, 55, 60, 66, 73, 80, 88,
	    97, 107, 118, 130, 143, 157, 173, 190, 209, 230, 253, 279, 307,
	    337, 371, 408, 449, 494, 544, 598, 658, 724, 796, 876, 963, 1060,
	    1166, 1282, 1411, 1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024,
	    3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630,
	    9493, 10442, 11487, 12635, 13899, 15289, 16818, 18500, 20350,
	    22385, 24623, 27086, 29794, 32767 };

    public static int[] diffLUT = new int[89 * 16];

    static {
	for (int delta = 0; delta < 16; delta++) {
	    for (int lastIndex = 0; lastIndex <= 88; lastIndex++) {

		int sign = delta & 8;
		int step = stepsizeTable[lastIndex];

		int vpdiff = step >> 3;
		if ((delta & 4) != 0)
		    vpdiff += step;
		if ((delta & 2) != 0)
		    vpdiff += step >> 1;
		if ((delta & 1) != 0)
		    vpdiff += step >> 2;

		if (sign != 0)
		    vpdiff = -vpdiff;
		diffLUT[(lastIndex << 4) + delta] = vpdiff;

	    }
	}

    }

    // IMA4 encoder
    /*
     * static void adpcm_coder(short[] indata,byte[] outdata,int len,IMA4State
     * state) { short[] inp; // Input buffer pointer byte[] outp; // output
     * buffer pointer int val; // Current input sample value int sign; //
     * Current adpcm sign bit int delta; // Current adpcm output value int diff;
     * // Difference between val and valprev int step; // Stepsize int valpred;
     * // Predicted output value int vpdiff; // Current change to valpred int
     * index; // Current step change index int outputbuffer=0;// place to keep
     * previous 4-bit value boolean bufferstep; // toggle between
     * outputbuffer/output
     * 
     * int inOffset=0; int outOffset=0;
     * 
     * 
     * outp = outdata; inp = indata;
     * 
     * valpred = state.valprev; index = state.index; step =
     * stepsizeTable[index];
     * 
     * bufferstep = true;
     * 
     * for ( ; len > 0 ; len-- ) { val = inp[inOffset++];
     * 
     * // Step 1 - compute difference with previous value diff = val - valpred;
     * sign = (diff < 0) ? 8 : 0; if ( sign !=0) diff = (-diff);
     * 
     * // Step 2 - Divide and clamp // Note: // This code *approximately*
     * computes: // delta = diff*4/step; // vpdiff = (delta+0.5)*step/4; // but
     * in shift step bits are dropped. The net result of this is // that even if
     * you have fast mul/div hardware you cannot put it to // good use since the
     * fixup would be too expensive. // delta = 0; vpdiff = (step >> 3);
     * 
     * if ( diff >= step ) { delta = 4; diff -= step; vpdiff += step; } step >>=
     * 1; if ( diff >= step ) { delta |= 2; diff -= step; vpdiff += step; } step
     * >>= 1; if ( diff >= step ) { delta |= 1; vpdiff += step; }
     * 
     * // Step 3 - Update previous value if ( sign !=0 ) valpred -= vpdiff; else
     * valpred += vpdiff;
     * 
     * // Step 4 - Clamp previous value to 16 bits if ( valpred > 32767 )
     * valpred = 32767; else if ( valpred < -32768 ) valpred = -32768;
     * 
     * // Step 5 - Assemble value, update index and step values delta |= sign;
     * 
     * index += indexTable[delta]; if ( index < 0 ) index = 0; if ( index > 88 )
     * index = 88; step = stepsizeTable[index];
     * 
     * // Step 6 - Output value if ( bufferstep ) { outputbuffer = (delta << 4)
     * & 0xf0; } else { outp[outOffset++] = (byte)((delta & 0x0f) |
     * outputbuffer); } bufferstep = !bufferstep; }
     * 
     * // Output last step, if needed if ( !bufferstep ) outp[outOffset++] =
     * (byte)outputbuffer;
     * 
     * state.valprev = valpred; state.index = index; }
     */

    public static void decode(byte[] indata, int inOffset, byte[] outdata,
	    int outOffset, int len, IMA4State state, int stride) {
	byte[] inp; // encoded bitstream buffer
	byte[] outp; // decoded buffer
	int delta; // adpcm encoded nibble
	int valpred; // predicted sample value
	int index; // current step change index
	int lastIndex; // previous step change index
	int inputbuffer = 0; // place to keep next encoded nibble
	boolean bufferstep = false;// toggle between hi-lo nibble

	outp = outdata;
	inp = indata;

	valpred = state.valprev;
	index = state.index;
	lastIndex = index;

	for (; len > 0; len--) {

	    // Step 1 - get the delta value //DVI endianess
	    // if ( bufferstep ) {
	    // delta = inputbuffer & 0xf;
	    // } else {
	    // inputbuffer = inp[inOffset++];
	    // delta = (inputbuffer >> 4) & 0xf;
	    // }

	    if (bufferstep) { // IMA 4 nibble endianess
		delta = (inputbuffer >> 4) & 0xf;
	    } else {
		inputbuffer = inp[inOffset++];
		delta = inputbuffer & 0xf;
	    }
	    bufferstep = !bufferstep;

	    // Step 2 - Find new index value (for later)
	    index += indexTable[delta];
	    if (index < 0)
		index = 0;
	    else if (index > 88)
		index = 88;

	    // Steps 3 & 4 calcluate output value
	    valpred += diffLUT[(lastIndex << 4) + delta];

	    // Step 5 - clamp output value
	    if (valpred > 32767)
		valpred = 32767;
	    else if (valpred < -32768)
		valpred = -32768;

	    // Step 6 - Update step value
	    lastIndex = index; // keep index for next sample (instead of step)

	    // Step 7 - Output value
	    // le outp[outOffset++]=(byte)valpred;
	    // le outp[outOffset++]=(byte)(valpred>>8);

	    outp[outOffset++] = (byte) (valpred >> 8); // big Endian store
	    outp[outOffset++] = (byte) valpred;

	    outOffset += stride; // stride = 0-mono,2-stereo

	}

	state.valprev = valpred;
	state.index = index;
    }

}
