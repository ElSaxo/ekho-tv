/*
 * @(#)Packet.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.net;

/**
 * Packet.java
 * 
 * This is an abstract representing the top level of the network packet
 * framework.
 * 
 * By subclassing this class, you can provide a packet implementation with your
 * own behaviour that may suit your protocol or device in a more efficient
 * manner than GenericPacket.
 * 
 * sritchie -- Apr 96
 * 
 * notes
 * 
 */
/*
 * adapted for javatv tye -- 11/2/99
 */
public abstract class Packet {

    // useful for building linked lists of packets
    public Packet next;
    public Packet prev;

    // these are convenient placeholders for the UdpEndpoint and
    // DatagramSocketBackend classes.
    public int src_port;
    public int src_ip;
    public int dst_ip;

    public Packet() {
    }

    // Return this packet back to its pool.
    public abstract void recycle();

    // Make a physical, device independent copy of this packet.
    public abstract Packet copy();

    // Perform an Internet checksum on the packet data and return it.
    public abstract int cksum(int offset, int len);

    // -------------------------------------------------------------------------

    protected int length;
    protected int hdr_offset;

    public void shiftHeader(int bytes) {
	hdr_offset += bytes;
    }

    public int getHeaderOffset() {
	return hdr_offset;
    }

    public void setHeaderOffset(int off) {
	hdr_offset = off;
    }

    public int length() {
	return length;
    }

    public int dataLength() {
	return length - hdr_offset;
    }

    public void setDataLength(int len) {
	length = hdr_offset + len;
    }

    public void setLength(int len) {
	length = len;
    }

    // -------------------------------------------------------------------------

    public abstract long getEthAddr(int off);

    public abstract int getInt(int off);

    public abstract int getShort(int off);

    public abstract int getByte(int off);

    public abstract void getBytes(int src_offset, byte dst[], int dst_offset,
	    int len);

    public abstract void putEthAddr(long x, int off);

    public abstract void putInt(int x, int off);

    public abstract void putShort(int x, int off);

    public abstract void putByte(int x, int off);

    public abstract void putBytes(byte src[], int src_offset, int dst_offset,
	    int len);

    public abstract void putBytes(Packet pkt, int src_offset, int dst_offset,
	    int len);
}
