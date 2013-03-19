/*
 * @(#)DefaultCodecFactory.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.codec;

import java.util.Enumeration;

import com.sun.tv.media.Codec;
import com.sun.tv.media.MediaFormat;
import com.sun.tv.media.util.JMFProperties;

/**
 * This is an implementation base class of the Codec interface to convert
 * encoded/compressed audio or video data to a format that can be understood by
 * a rendering node or device.
 */
public class DefaultCodecFactory {

    static String DEFAULT_PKG_PREFIX = "com.sun.tv";

    /**
     * Return true if the given codec is supported.
     */
    static public boolean supports(String name) {
	return (getCodecClass(name) != null);
    }

    /**
     * Create a codec object given the codec name.
     * 
     * @param name
     *            the codec name of the form: <media>.<codec name>
     * @return the codec object
     */
    static public Codec createCodec(String name) {
	Class cClass = getCodecClass(name);
	if (cClass == null) {
	    // System.err.println("Cannot create codec for: " + name);
	    return null;
	}

	Codec codec;
	try {
	    codec = (Codec) cClass.newInstance();
	} catch (Exception e) {
	    System.err.println("Cannot instantiate the codec: " + name);
	    return null;
	}

	return codec;
    }

    /**
     * Create a codec object given the format.
     * 
     * @param format
     *            the encoded format
     * @return the codec object
     */
    static public Codec createCodec(String name, MediaFormat format,
	    String propsname) {
	Codec codec = null;

	if (name == null && format == null)
	    return null;

	if (name == null)
	    name = format.getCodec();

	if (name != null)
	    codec = createCodec(name);

	if (codec != null && codec.supports(format))
	    return codec;

	//
	// Enumerate all the codec classes as specified in the registry
	// to see if any of the codec supports the given format.
	//

	Enumeration codecList = JMFProperties.str2list(
		JMFProperties.getProperty(propsname)).elements();
	String codecName;

	while (codecList.hasMoreElements()) {
	    codecName = (String) codecList.nextElement();
	    Class cClass;
	    try {
		cClass = Class.forName(codecName);
	    } catch (Exception e) {
		continue;
	    }

	    try {
		Object c = cClass.newInstance();
		if (c instanceof Codec && ((Codec) c).supports(format)) {
		    // Found it!
		    return (Codec) c;
		}
	    } catch (Exception e) {
	    }
	}

	return null;
    }

    /**
     * Get the codec class given the codec name.
     * 
     * @param name
     *            the codec name of the form: <media>.<codec name>
     * @return the Codec class.
     */
    static public Class getCodecClass(String name) {
	if (name == null)
	    return null;

	String className = DEFAULT_PKG_PREFIX + ".media.codec." + name
		+ ".Codec";
	Class cClass;

	try {
	    cClass = Class.forName(className);
	} catch (Exception e) {
	    return null;
	}

	return cClass;
    }

}
