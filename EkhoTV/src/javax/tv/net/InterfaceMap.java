/*
 * @(#)InterfaceMap.java	1.28 08/11/14
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.net;

import java.io.IOException;
import java.net.InetAddress;

import javax.tv.locator.InvalidLocatorException;
import javax.tv.locator.Locator;

/**
 * Class <code>InterfaceMap</code> reports the local IP address assigned to a
 * given service component that carries IP data. Applications may use the
 * returned IP address to specify the network interface to which an instance of
 * <code>com.ekholabs.java.net.DatagramSocket</code> or
 * <code>com.ekholabs.java.net.MulticastSocket</code> should bind.
 * 
 * @see com.ekholabs.java.net.DatagramSocket#DatagramSocket(int,
 *      com.ekholabs.java.net.InetAddress)
 * @see com.ekholabs.java.net.MulticastSocket#setInterface
 * 
 */
public class InterfaceMap {

    private static int fourthNumber = 0;
    private static int thirdNumber = 1;

    private InterfaceMap() {
    }

    /**
     * Reports the local IP address assigned to the given service component.
     * 
     * @param locator
     *            The service component for which the local IP address mapping
     *            is required.
     * 
     * @return The IP address assigned to this service component.
     * 
     * @throws InvalidLocatorException
     *             If the given locator does not refer to a valid source of IP
     *             data, or if this system does not support the reception of
     *             broadcast IP data.
     * 
     * @throws IOException
     *             If a local IP address is not available to be assigned to the
     *             source of IP data.
     * 
     */
    public static InetAddress getLocalAddress(Locator locator)
	    throws InvalidLocatorException, IOException {

	if (locator == null) {
	    throw new NullPointerException();
	}
	if (locator.toExternalForm() == null) {
	    throw new NullPointerException();
	}

	// 6771238
	throw new InvalidLocatorException(locator,
		"Broadcast IP data not supported.");

	// 6771238
	// if (com.sun.tv.LocatorImpl.isServiceComponent(locator) == false) {
	// throw new InvalidLocatorException(locator,
	// "Not a ServiceComponent locator");
	// }
	//
	// if (com.sun.tv.net.EncapIPStream.isIPStreamLocator(locator) == false)
	// {
	// throw new InvalidLocatorException(locator,
	// "Not a source of IP data");
	// }
	//
	// Hashtable addrUsed = com.sun.tv.net.InterfaceMapImpl.addrUsed;
	// InetAddress addr =
	// (InetAddress)addrUsed.get(locator.toExternalForm());
	// if (addr == null) {
	// addr = getNextIP();
	// if (addr != null) {
	// addrUsed.put(locator.toExternalForm(), addr);
	// }
	// }
	// if (addr == null) {
	// throw new IOException("Can't get the next local IP address");
	// }
	// return addr;
    }

    /**
     * Expand the address pool by adding the next POOL_EXPN_SIZE addresses in
     * 192.168.x.x class to the addrPool variable.
     * 
     * @return true if expansion is successful, false if out of address
     */
    // 6771238
    // private static InetAddress getNextIP() {
    // InetAddress addr = null;
    //
    // Vector addrFree = com.sun.tv.net.InterfaceMapImpl.addrFree;
    // if (addrFree.size() > 0) {
    // addr = (InetAddress)addrFree.elementAt(0);
    // addrFree.removeElementAt(0);
    // return addr;
    // }
    //
    // fourthNumber++;
    //
    // // start with the first 256 addresses
    // if (fourthNumber >= 0xff) {
    // fourthNumber = 1;
    // thirdNumber++;
    // if ( thirdNumber > 0xff ) {
    // fourthNumber = 1;
    // thirdNumber = 1;
    // }
    // }
    //
    // String ip = "192.168."+thirdNumber+"."+fourthNumber;
    // try {
    // addr = InetAddress.getByName(ip);
    // } catch (Exception e) {
    // ;
    // }
    // return addr;
    // }
}
