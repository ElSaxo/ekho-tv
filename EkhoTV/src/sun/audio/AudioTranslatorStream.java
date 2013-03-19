/*
 * @(#)AudioTranslatorStream.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package sun.audio;

import java.io.IOException;
import java.io.InputStream;

/**
 * A Sun-specific AudioStream that supports the .au file format.
 * 
 * @version 1.7, 08/19/02
 */
public class AudioTranslatorStream extends NativeAudioStream {
    private int length = 0;

    public AudioTranslatorStream(InputStream in) throws IOException {
	super(in);
	// No translators supported yet.
	throw new InvalidAudioFormatException();
    }

    public int getLength() {
	return length;
    }
}
