/*
 * @(#)IMA4State.java	1.2 98/11/25
 *
 *  Licensed Materials - Property of IBM
 *  "Restricted Materials of IBM"
 *  5648-B81
 *  (c) Copyright IBM Corporation 1997,1998 All Rights Reserved
 *  US Government Users Restricted Rights - Use, duplication or
 *  disclosure restricted by GSA ADP Schedule Contract with
 *  IBM Corporation.
 *
 */

package com.sun.tv.media.codec.audio.ima4;

// this is public class since the DVI decoder is using it

/**
 * IMA4/DVI history structure
 */

public class IMA4State {
    public int valprev; // Previous output value
    public int index; // Index into stepsize table
}
