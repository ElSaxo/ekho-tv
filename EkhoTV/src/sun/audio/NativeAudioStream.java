/*
 * @(#)NativeAudioStream.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package sun.audio;

import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

/**
 * A Sun-specific AudioStream that supports the .au file format.
 * 
 * @version 1.7, 08/19/02
 */
public class NativeAudioStream extends FilterInputStream {
    private final int SUN_MAGIC = 0x2e736e64;
    private final int DEC_MAGIC = 0x2e736400;
    private final int MINHDRSIZE = 24;
    private final int TYPE_ULAW = 1;
    private int length = 0;

    /**
     * Read header, only sun 8 bit, ulaw encoded, single channel, 8000hz is
     * supported
     */
    public NativeAudioStream(InputStream in) throws IOException {
	super(in);
	DataInputStream data = new DataInputStream(in);
	int magic = data.readInt();
	if (magic != SUN_MAGIC && magic != DEC_MAGIC) {
	    Log.e("EkhoTV", "NativeAudioStream: invalid file type.");
	    throw new InvalidAudioFormatException();
	}
	int hdr_size = data.readInt(); // header size
	if (hdr_size < MINHDRSIZE) {
	    Log.e("EkhoTV", "NativeAudioStream: wrong header size of "
		    + hdr_size + ".");
	    throw new InvalidAudioFormatException();
	}
	length = data.readInt();
	int encoding = data.readInt();
	if (encoding != TYPE_ULAW) {
	    Log.e("EkhoTV", "NativeAudioStream: invalid audio encoding.");
	    throw new InvalidAudioFormatException();
	}
	int sample_rate = data.readInt();
	if ((sample_rate / 1000) != 8) { // allow some slop
	    Log.e("EkhoTV", "NativeAudioStream: invalid sample rate of "
		    + sample_rate + ".");
	    throw new InvalidAudioFormatException();
	}
	int channels = data.readInt();
	if (channels != 1) {
	    Log.e("EkhoTV", "NativeAudioStream: wrong number of channels. "
		    + "(wanted 1, actual " + channels + ")");
	    throw new InvalidAudioFormatException();
	}
	in.skip(hdr_size - MINHDRSIZE);
    }

    public int getLength() {
	return length;
    }
}
