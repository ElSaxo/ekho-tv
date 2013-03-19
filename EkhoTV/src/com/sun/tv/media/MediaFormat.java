/*
 * @(#)MediaFormat.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;


public abstract class MediaFormat implements Format {

    public Format match(Format other) {
	if (other instanceof MediaFormat)
	    return this;
	else
	    return null;
    }

    abstract public Object clone();

    abstract public long getDuration();

    abstract public String getCodec();

    static public String normalizeCodecName(String codec) {

	if (codec == null || codec.equals(""))
	    return "unknown";

	// Strip off the "JAUDIO_" and "JVIDEO_" prefix
	if (codec.indexOf("JAUDIO_") == 0 || codec.indexOf("JVIDEO_") == 0)
	    codec = codec.substring(7);

	int i, j;

	// All to lower case ...
	codec = codec.toLowerCase();

	// ... run through each char and convert
	// '/' -> '.'
	// !([A-Za-z0--9]) -> '_'
	// spaces are skipped.
	int len = codec.length();

	for (i = 0, j = 0; i < len; i++) {
	    if (codec.charAt(i) != ' ')
		j++;
	}

	char nm[] = new char[j];
	for (i = 0, j = 0; i < len; i++) {
	    char c = codec.charAt(i);
	    if (c == '/') {
		nm[j++] = '.';
	    } else if (c == ' ') {
		// skip spaces.
		continue;
	    } else if ('a' <= c && c <= 'z' || '0' <= c && c <= '9') {
		nm[j++] = c;
	    } else
		nm[j++] = '_';
	}

	return new String(nm);
    }
}
