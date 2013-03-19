/*
 * @(#)TChunk.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media.content.video;

/*
 * This class is used for video/sound chunk
 */
public class TChunk {

    private int lStart; // Start Position
    private int lEnd; // End Position
    private int lID; //
    private boolean bKF; // Key Frame
    private int lFrameTime; // Frame Time

    public TChunk() {
	lStart = 0;
	lEnd = 0;
	lID = 0;
	bKF = false;
	lFrameTime = 0;
    }

    public int GetStartEntry() {
	return lStart;
    }

    public int GetEndEntry() {
	return lEnd;
    }

    public int GetIDEntry() {
	return lID;
    }

    public boolean GetKFEntry() {
	return bKF;
    }

    public int GetFrameTimeEntry() {
	return lFrameTime;
    }

    public void SetStartEntry(int ls) {
	lStart = ls;
    }

    public void SetEndEntry(int le) {
	lEnd = le;
    }

    public void SetIDEntry(int lid) {
	lID = lid;
    }

    public void SetKFEntry(boolean bkf) {
	bKF = bkf;
    }

    void SetFrameTimeEntry(int lft) {
	lFrameTime = lft;
    }

}
