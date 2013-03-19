/*
 * @(#)AudioDataStream.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package sun.audio;

import java.io.ByteArrayInputStream;

/**
 * An input stream to play AudioData.
 * 
 * @see AudioPlayer
 * @see AudioData
 * @author Arthur van Hoff
 * @version 1.13, 08/19/02
 */
public class AudioDataStream extends ByteArrayInputStream {
    /**
     * Constructor
     */
    public AudioDataStream(AudioData data) {
	super(data.buffer);
    }
}
