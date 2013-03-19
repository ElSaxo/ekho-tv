/*
 * @(#)TChunkList.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media.content.video;

import android.util.Log;

// TChunkList Class
// This class manages the TChunk and acts like an array of
// TChunk class.
public class TChunkList {

    public static final String VIDEO_TYPE = "VIDEO_TYPE";
    public static final String AUDIO_TYPE = "AUDIO_TYPE";

    protected TChunk acChunk[];
    protected int lNextIndex;
    protected int arraySize;
    protected String type; // video/audio type

    public boolean DEBUG = false;

    public TChunkList(int as, String t) {
	type = new String(t);
	lNextIndex = 0;
	arraySize = as;
	acChunk = new TChunk[arraySize];
    }

    public int getArraySize() {
	return arraySize;
    }

    public void dispose() {
	// $ Log.e("EkhoTV",
	// "Free up TChunkList, TChunkList.dispose is called.");
	type = null;
	acChunk = null;
	lNextIndex = 0;
    }

    public String getType() {
	return type;
    }

    public int AppendEntry(int lStart, int lEnd, int lID, boolean bKF,
	    int lFrameTime) {

	if (arraySize == lNextIndex) {
	    // $ Log.e("EkhoTV", "Reallocate Chunk, video");
	    arraySize++;
	    TChunk tempChunk[] = new TChunk[arraySize];
	    for (int i = 0; i < (arraySize - 1); i++)
		tempChunk[i] = acChunk[i];
	    acChunk = tempChunk;
	}
	acChunk[lNextIndex] = new TChunk();
	acChunk[lNextIndex].SetStartEntry(lStart);
	acChunk[lNextIndex].SetEndEntry(lEnd);
	acChunk[lNextIndex].SetIDEntry(lID);
	acChunk[lNextIndex].SetKFEntry(bKF);
	acChunk[lNextIndex].SetFrameTimeEntry(lFrameTime);

	lNextIndex++;
	return 0;
    }

    public int AppendEntry(int lStart, int lEnd, int lID, boolean bKF) {

	if (arraySize == lNextIndex) {
	    // Log.e("EkhoTV", "Reallocate Chunk, audio");
	    arraySize++;
	    TChunk tempChunk[] = new TChunk[arraySize];
	    for (int i = 0; i < (arraySize - 1); i++)
		tempChunk[i] = acChunk[i];
	    acChunk = tempChunk;
	}
	acChunk[lNextIndex] = new TChunk();
	acChunk[lNextIndex].SetStartEntry(lStart);
	acChunk[lNextIndex].SetEndEntry(lEnd);
	acChunk[lNextIndex].SetIDEntry(lID);
	acChunk[lNextIndex].SetKFEntry(bKF);

	lNextIndex++;
	return 0;
    }

    public boolean GetChunk(int lIndex) {

	if (DEBUG) {
	    Log.e("EkhoTV", "lIndex = " + lIndex);
	    Log.e("EkhoTV", "lNextIndex = " + GetNumEntries());
	}

	if (lIndex >= GetNumEntries())
	    return false;

	return true;
    }

    public int GetStartEntry(int lIndex) {
	if (GetChunk(lIndex)) {
	    return acChunk[lIndex].GetStartEntry();
	}
	return 0;
    }

    public int GetEndEntry(int lIndex) {
	if (GetChunk(lIndex)) {
	    return acChunk[lIndex].GetEndEntry();
	}
	return 0;
    }

    public int GetIDEntry(int lIndex) {
	if (GetChunk(lIndex)) {
	    return acChunk[lIndex].GetIDEntry();
	}
	return 0;
    }

    public boolean GetKFEntry(int lIndex) {
	if (GetChunk(lIndex)) {
	    return acChunk[lIndex].GetKFEntry();
	}
	return false;
    }

    public int GetFrameTimeEntry(int lIndex) {
	if (GetChunk(lIndex)) {
	    return acChunk[lIndex].GetFrameTimeEntry();
	}
	return 0;
    }

    public int GetNumEntries() {
	return lNextIndex;
    }

    public void SetStartEntry(int lIndex, int lStart) {
	if (GetChunk(lIndex)) {
	    acChunk[lIndex].SetStartEntry(lStart);
	}
    }

    public void SetEndEntry(int lIndex, int lEnd) {
	if (GetChunk(lIndex)) {
	    acChunk[lIndex].SetEndEntry(lEnd);
	}
    }

    public void SetIDEntry(int lIndex, int lID) {
	if (GetChunk(lIndex)) {
	    acChunk[lIndex].SetIDEntry(lID);
	}
    }

    public void SetKFEntry(int lIndex, boolean bKF) {
	if (GetChunk(lIndex)) {
	    acChunk[lIndex].SetKFEntry(bKF);
	}
    }

    public void SetFrameTimeEntry(int lIndex, int lFrameTime) {
	if (GetChunk(lIndex)) {
	    acChunk[lIndex].SetFrameTimeEntry(lFrameTime);
	}
    }

    //
    // Given the time, in nanoseconds, to calculate the frame index.
    //
    public int GetFrameIndex(long lTime) {
	long lUsec = 0L;
	int iIndex = 0;
	long usecTime = lTime / 1000L;
	int nextIndex = GetNumEntries();

	for (int i = 0; i < nextIndex; i++) {
	    lUsec += GetFrameTimeEntry(i);
	    if (lUsec < usecTime)
		iIndex++;
	    else
		break;
	}
	return iIndex;
    }

    //
    // Given the bytes to calculate the sound index
    //
    public int GetSoundIndex(long lBytes) {
	long lTBytes = 0;
	int iIndex = 0;

	int nextIndex = GetNumEntries();

	for (int i = 0; i < nextIndex; i++) {
	    lTBytes += (acChunk[i].GetEndEntry() - acChunk[i].GetStartEntry());
	    if (lTBytes < lBytes)
		iIndex++;
	    else
		break;
	}
	return iIndex;
    }

    public int GetTotalSize() {
	int total = 0;

	for (int i = 0; i < GetNumEntries(); i++) {
	    total += (acChunk[i].GetEndEntry() - acChunk[i].GetStartEntry());
	}
	return total;
    }

    public int GetTotalSize(int index) {
	int total = 0;

	// Log.e("EkhoTV", "GetTotalSize's index = " + index);
	// Log.e("EkhoTV", "acChunk.length = " + acChunk.length);

	// Put a sanity check
	if (acChunk.length < index)
	    return total;

	for (int i = 0; i < index; i++) {
	    total += (acChunk[i].GetEndEntry() - acChunk[i].GetStartEntry());
	}
	return total;
    }
}
