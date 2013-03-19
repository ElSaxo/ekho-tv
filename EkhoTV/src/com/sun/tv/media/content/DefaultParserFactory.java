/*
 * @(#)DefaultParserFactory.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.content;

import com.sun.tv.media.Parser;

public class DefaultParserFactory {
    static String DEFAULT_PKG_PREFIX = "com.sun.tv";

    /**
     * Return true if the given parser type is supported.
     */
    static public boolean supports(String name) {
	return (getParserClass(name) != null);
    }

    /**
     * Return a parser object given the parser name.
     */
    static public Parser createParser(String name) {
	Class pClass = getParserClass(name);
	if (pClass == null)
	    return null;

	Parser parser = null;
	try {
	    parser = (Parser) pClass.newInstance();
	} catch (Exception e) {
	    System.err.println("Cannot instantiate the parser: " + name);
	    return null;
	}

	return parser;
    }

    /**
     * Return a parser class given the parser name.
     */
    static public Class getParserClass(String contentType) {
	if (contentType == null)
	    return null;

	String className = DEFAULT_PKG_PREFIX + ".media.content." + contentType
		+ ".Parser";
	Class pClass;

	try {
	    pClass = Class.forName(className);
	} catch (Exception e) {
	    System.err.println("No such parser class: " + className);
	    return null;
	}

	return pClass;
    }
}
