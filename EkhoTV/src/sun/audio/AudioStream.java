/*
 * @(#)AudioStream.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package sun.audio;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Convert an InputStream to an AudioStream.
 * 
 * @version 1.16, 08/19/02
 */
public class AudioStream extends FilterInputStream {
    NativeAudioStream audioIn;

    public AudioStream(InputStream in) throws IOException {
	super(in);
	try {
	    audioIn = (new NativeAudioStream(in));
	} catch (InvalidAudioFormatException e) {
	    // Not a native audio stream -- use a translator (if available).
	    // If not, let the exception bubble up.
	    audioIn = (new AudioTranslatorStream(in));
	}
	this.in = audioIn;
    }

    /**
     * A blocking read.
     */
    public int read(byte buf[], int pos, int len) throws IOException {
	int count = 0;
	while (count < len) {
	    int n = super.read(buf, pos + count, len - count);
	    if (n < 0) {
		return count;
	    }
	    count += n;
	    Thread.currentThread().yield();
	}
	return count;
    }

    /**
     * Get the data.
     */
    public AudioData getData() throws IOException {
	byte buffer[] = new byte[audioIn.getLength()];
	int gotbytes = read(buffer, 0, audioIn.getLength());
	close();
	if (gotbytes != audioIn.getLength()) {
	    throw new IOException("audio data read error");
	}
	return new AudioData(buffer);
    }

    public int getLength() {
	return audioIn.getLength();
    }
}
