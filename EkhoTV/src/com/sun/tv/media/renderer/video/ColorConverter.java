/*
 * @(#)ColorConverter.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.renderer.video;

import com.sun.tv.media.VidData;
import com.sun.tv.media.format.video.VidFormat;

public interface ColorConverter {

    /**
     * Performs the color conversion.
     */
    boolean convert(VidData in, VidData out);

    /**
     * Returns true if this class is capable of converting data from the 'in'
     * format to the 'out' format.
     */
    boolean canConvert(VidFormat in, VidFormat out);

}
