/*
 * @(#)SampleDataInterface.java	1.6 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.receiver;

/**
 * This interface is used by the <code>SIEmulator class
 * to populate the service information database.
 * A class inplementing this interface is specified in 
 * the properties file.
 */
public interface SampleDataInterface {

    /**
     * This method is the first method called by the SIEmulator. Implementations
     * of this interface should load their SI data via the programatic
     * interfaces in SIEmulator.
     */
    public void play(SIEmulator emulator, String args[]);

    /**
     * This method is called from SIEmulator after the play method is called. It
     * is intended to block until the SI has been loaded, or when the
     * implementation of this interface is satisfied that enough of the SI data
     * has been loaded to continue.
     */
    public void finish();

    /**
     * This method can be called to verify that all the data has been loaded
     * into the SI database. It is not called SIEmulator.
     */
    public boolean verify();

}
