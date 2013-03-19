/*
 * @(#)AudioData.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package sun.audio;

/**
 * A clip of audio data, contains ulaw 8bit, 8000hz data. This data can be used
 * to construct and AudioDataStream, which can be played.
 * <p>
 * 
 * @author Arthur van Hoff
 * @version 1.22, 08/19/02
 * @see AudioDataStream
 * @see AudioPlayer
 */
public class AudioData {
    /**
     * The data
     */
    byte buffer[];

    /**
     * Constructor
     */
    public AudioData(byte buffer[]) {
	this.buffer = buffer;
    }
}
