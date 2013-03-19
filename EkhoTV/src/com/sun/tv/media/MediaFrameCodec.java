/*
 * @(#)MediaFrameCodec.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;


/**
 * An implementation class to decode a "frame" of compressed data to a "frame"
 * of decompressed data. A "frame" doesn't have to refer to a "video frame."
 * It's just a logical unit of data.
 */
abstract public class MediaFrameCodec extends MediaCodec {

    private boolean dropFrame = false;
    private boolean lastFrameDropped = false;
    private boolean flushing = false;

    /**
     * Implement this to do the real decoding work.
     * 
     * @param in
     *            input compressed data.
     * @param out
     *            output compressed data.
     * @return true if the decoding is successful.
     */
    abstract public boolean decode(Data in, Data out);

    /**
     * Switch on or off the mechanism to drop frame. If drop frame is on, the
     * system will decide to drop frame if the frame is already too late to be
     * presented. In such case, the frame will not reach the decode() function.
     * Whether to take advantage of this is up to the codec writer. The
     * advantage is that for compute intensive codecs, a delayed frame will not
     * need to be processed if it will not be presented anyway. That saves quite
     * some CPU cycles. This will not work for codec's that couldn't handle drop
     * frames well.
     * 
     * @param on
     *            turn on/off the frame dropping code.
     */
    public void allowDropFrame(boolean on) {
	dropFrame = on;
    }

    /**
     * Gets called by the framework with a chunk of compressed data.
     * 
     * @param inData
     *            input compressed data.
     * @return true if the decoding is successful.
     */
    public boolean decode(Data inData) {

	if (inData.getLength() == Data.EOM) {
	    Data out = getOutputBuffer(getOutputFormat());
	    out.setPresentationTime(inData.getPresentationTime());
	    out.setDuration(inData.getDuration());
	    out.setLength(Data.EOM);
	    decodeUpdate(out);
	    return true;
	}

	// If dropping frame is allowed and the source node tells me
	// to discard the frame, we'll do so here.
	if (dropFrame && inData.getDiscard()) {
	    lastFrameDropped = true;
	    return true;
	}

	flushing = false;

	Data out = getOutputBuffer(getOutputFormat());
	out.setPresentationTime(inData.getPresentationTime());
	out.setDuration(inData.getDuration());

	if (flushing) {
	    decodeFailureUpdate(out);
	    flushing = false;
	    return true;
	}

	// If frames are dropped here, there is no need to drop frames
	// again at the renderer. Otherwise, we'll hint to the renderer
	// that the frame could be dropped if necessary.
	// The semantics of setDiscard() is a bit different here in that
	// it's signalling a permission to discard, not a "yes" to discard.
	if (lastFrameDropped) {
	    out.setDiscard(false);
	    lastFrameDropped = false;
	} else
	    out.setDiscard(true);

	if (decode(inData, out)) {
	    decodeUpdate(out);
	    return true;
	} else {
	    out.setLength(0);
	    decodeFailureUpdate(out);
	    return false;
	}
    }

    public void flush() {
	flushing = true;
    }
}
