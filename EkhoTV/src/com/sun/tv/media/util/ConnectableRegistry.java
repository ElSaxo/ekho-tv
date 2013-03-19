/*
 * @(#)ConnectableRegistry.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.util;

import java.util.Vector;

import com.sun.tv.media.Connectable;

/**
 * ConnectableRegistry A utility class to implement a registry for the input or
 * output connectables.
 * 
 * @version 1.4, 98/03/28
 */
public class ConnectableRegistry {

    Vector names;
    Vector connectables;

    public ConnectableRegistry() {
	names = new Vector();
	connectables = new Vector();
    }

    /**
     * Return the names of all the connectables as a String array.
     */
    public String[] getNames() {
	if (names.size() == 0)
	    return null;
	String[] nms = new String[names.size()];
	for (int i = 0; i < names.size(); i++) {
	    nms[i] = (String) names.elementAt(i);
	}
	return nms;
    }

    /**
     * Return the connectable given its name.
     */
    public Connectable getConnectable(String nm) {
	for (int i = 0; i < names.size(); i++) {
	    if (((String) names.elementAt(i)).equals(nm))
		return (Connectable) connectables.elementAt(i);
	}
	return null;
    }

    /**
     * Return the connectable given the location in the array.
     */
    public Connectable getConnectable(int i) {
	return (Connectable) connectables.elementAt(i);
    }

    /**
     * Register a connectable with its name.
     */
    public void register(String name, Connectable c) {
	names.addElement(name);
	connectables.addElement(c);
    }

    /**
     * Return the number of connectables stored.
     */
    public int size() {
	return names.size();
    }

}
