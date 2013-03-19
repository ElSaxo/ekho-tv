/*
 * @(#)Blitter.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import android.view.View;

import com.sun.tv.media.format.video.VidFormat;

public interface Blitter {

    /**
     * Sets the Java Component to which we need to blit. Returns true if there
     * were no complaints.
     */
    public boolean setComponent(View comp);

    /**
     * Specify the buffer that is going to be used. This method does not have to
     * be called. Its a way of preparing the blitter to use a certain buffer,
     * probably during the prefetch stage, so that we don't take a hit when
     * displaying the first frame. Returns true if there was no problem.
     */
    public boolean setBuffer(VidData data);

    /**
     * Sets the size to which the frame is to be scaled when the next frame is
     * blitted. Returns true if the size was acceptable.
     */
    public boolean setOutputSize(int width, int height);

    /**
     * Returns instances of the different formats of video frames that this
     * class can take as input.
     */
    public VidFormat[] getFormats();

    /**
     * Returns the specific format that this class would prefer. This is
     * probably the format of the display and hence gives the best blit speed.
     */
    public VidFormat getPreferredFormat();

    /**
     * Specify the format of the data that is going to be blitted. This is used
     * as a preparatory hint. Does not have to be called. Returns true if the
     * format was acceptable.
     */
    public boolean setFormat(VidFormat format);

    /**
     * Draws the image at the specified coordinate. Returns true if the
     * operation was succesful.
     */
    public boolean draw(VidData data, int x, int y);

    public void close();

}
