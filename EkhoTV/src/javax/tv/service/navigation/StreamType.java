/*
 * @(#)StreamType.java	1.4 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.navigation;

/**
 * This class represents values of <code>ServiceComponent</code> stream types
 * (e.g., "video", "audio", "subtitles", "data", "sections", etc.).
 * 
 * @see ServiceComponent
 */
public class StreamType {

    private String name = null;

    /**
     * Creates a stream type object.
     * 
     * @param name
     *            The string name of this type (e.g., "VIDEO").
     */
    protected StreamType(String name) {
	this.name = name;
	if (name == null) {
	    throw new NullPointerException("Name is null");
	}
    }

    /**
     * Provides the string name of the type. For the type objects defined in
     * this class, the string name will be identical to the class variable name.
     */
    public String toString() {
	return name;
    }

    /**
     * Video component.
     */
    public static final StreamType VIDEO;

    /**
     * Audio component.
     */
    public static final StreamType AUDIO;

    /**
     * Subtitles component.
     */
    public static final StreamType SUBTITLES;

    /**
     * Data component.
     */
    public static final StreamType DATA;

    /**
     * MPEG sections component.
     */
    public static final StreamType SECTIONS;

    /**
     * Unknown component.
     */
    public static final StreamType UNKNOWN;

    // Needed for compilation.
    static {
	VIDEO = new StreamType("VIDEO");
	AUDIO = new StreamType("AUDIO");
	SUBTITLES = new StreamType("SUBTITLES");
	DATA = new StreamType("DATA");
	SECTIONS = new StreamType("SECTIONS");
	UNKNOWN = new StreamType("UNKNOWN");
    }
}
