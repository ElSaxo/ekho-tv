/*
 * @(#)InterfaceMapImpl.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.net;

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.tv.locator.Locator;
import javax.tv.locator.LocatorFactory;

/**
 ** Class <code>InterfaceMap</code> reports the local IP address assigned to a
 * given service component. Applications may use the returned IP address to
 * specify the network interface to which an instance of
 * com.ekholabs.java.net.DatagramSocket or com.ekholabs.java.net.MulticastSocket
 * should bind.
 **/
public class InterfaceMapImpl {

    public static Hashtable addrUsed = new Hashtable();
    public static Vector addrFree = new Vector();

    public static boolean isBcastEncapIP(InetAddress addr) {
	if (addrUsed != null && addr != null) {
	    return addrUsed.contains(addr);
	}
	return false;
    }

    public static Locator getLocator(InetAddress addr) {
	if (addrUsed == null || addr == null)
	    return null;

	try {
	    Enumeration list = addrUsed.keys();
	    while (list.hasMoreElements()) {
		String key = (String) list.nextElement();
		InetAddress daddr = (InetAddress) addrUsed.get(key);
		if (daddr == null)
		    continue;

		if (daddr == addr) {
		    LocatorFactory factory = LocatorFactory.getInstance();
		    return factory.createLocator(key);
		}
	    }
	} catch (Exception e) {
	    ;
	}
	return null;
    }

    static void returnToAddrPool(InetAddress addr) {
	if (addrUsed == null || addr == null)
	    return;

	Enumeration list = addrUsed.keys();
	while (list.hasMoreElements()) {
	    String key = (String) list.nextElement();
	    InetAddress daddr = (InetAddress) addrUsed.get(key);
	    if (daddr == null)
		continue;

	    if (daddr == addr) {
		addrUsed.remove(key);
		addrFree.addElement(addr);
		break;
	    }
	}
    }
}
