/*
 * @(#)Track.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */
package com.sun.tv.media;

import javax.media.Time;

import android.util.Log;

import com.sun.tv.media.content.video.TChunkList;
import com.sun.tv.media.format.audio.AudioFormat;
import com.sun.tv.media.format.video.VideoFormat;

public class Track {

    protected TChunkList plst;
    protected Format format;
    protected boolean tableDriven;
    public boolean DEBUG;

    public Track() {
	plst = null;
	format = null;
	tableDriven = false;
    }

    public Track(TChunkList t) {
	super();
	setList(t);
	tableDriven = true;
    }

    public TChunkList getList() {
	return plst;
    }

    protected void setList(TChunkList t) {
	plst = t;
    }

    /**
     * Media Information
     */
    public Format getFormat() {
	return format;
    }

    public void setFormat(Format f) {
	format = f;
    }

    /**
     * This method returns the number of entries of the corresponding track
     * list.
     */
    public int getNumIndexes() {
	int total = 0;
	if (plst != null)
	    total = plst.GetNumEntries();
	return total;
    }

    /**
     * This method calculate the size of the track in bytes corresponding to the
     * trackID.
     */
    public int getChunkSize(int index) {
	int size = 0;
	if (plst != null)
	    size = plst.GetEndEntry(index) - plst.GetStartEntry(index);
	return size;
    }

    /**
     * This method calculate the track start offset corresponding to the
     * trackID.
     */
    public long index2Offset(int index) {
	if (plst == null)
	    return -1;

	return plst.GetStartEntry(index);
    }

    public Time index2Time(int index) {
	Time time = null;
	long offset;

	if (tableDriven) {
	    offset = index2Offset(index);
	    time = offset2Time(offset);
	}
	return time;
    }

    /**
     * This method converses time in second to actual file offset in bytes.
     * 
     * @param time
     *            where to position the stream, in second.
     * @return the actual position, in bytes.
     */
    public long time2Offset(Time time) {

	int index;
	long offset = -1L, offsetdiff = 0L;
	int header;
	long timediff = 0L;
	if (!tableDriven && (format instanceof AudioFormat)) {
	    AudioFormat af = (AudioFormat) format;
	    long bytesPerSec = ((AudioFormat) format).getBytesPerSec();
	    header = af.getHeaderSize();
	    offset = (time.getNanoseconds() * bytesPerSec) / 1000000000L;
	    int align = af.getnBlockAlign();
	    if (align != 0)
		offset = offset - (offset % align);
	    offset += header;
	    return offset;
	}

	if (plst != null) {
	    index = time2Index(time);
	    if (plst.getType().equals(TChunkList.AUDIO_TYPE)) {

		long currentTime = time.getNanoseconds();
		long totalByte = plst.GetTotalSize(index);

		long BytePerSec = ((AudioFormat) format).getBytesPerSec();
		long audioTime = (long) (totalByte * 1000000000L / BytePerSec);
		timediff = currentTime - audioTime;
		offsetdiff = (timediff * BytePerSec) / 1000000000L;

		if (DEBUG) {
		    Log.e("EkhoTV", "T2O: SOff = " + plst.GetStartEntry(index));
		    Log.e("EkhoTV", "     EOff = " + plst.GetEndEntry(index));
		    Log.e("EkhoTV", "     index = " + index);
		    Log.e("EkhoTV", "     offsetdiff = " + offsetdiff);
		    Log.e("EkhoTV", "     timediff = " + timediff);
		    Log.e("EkhoTV", "     audioTime = " + audioTime);
		    Log.e("EkhoTV", "     currentTime = " + currentTime);
		    Log.e("EkhoTV", "     totalByte = " + totalByte);
		    Log.e("EkhoTV", "     BytePerSec = " + BytePerSec);
		}
	    }

	    // $$ Sanity check - cannot skip more than the chunk size.
	    if (offsetdiff > (plst.GetEndEntry(index) - plst
		    .GetStartEntry(index))) {
		offsetdiff = 0;
	    }

	    // Note: odd number will cause noise in JavaSound.
	    if ((offsetdiff % 2) != 0)
		offsetdiff -= 1;
	    if (format instanceof AudioFormat) {
		int align = ((AudioFormat) format).getnBlockAlign();
		if (align != 0)
		    if ((offsetdiff % align) != 0)
			offsetdiff = offsetdiff - (offsetdiff % align);
	    }
	    offset = plst.GetStartEntry(index) + offsetdiff;
	}
	return offset;
    }

    // Note: need optimize the code - cania
    public Time offset2Time(long offset, int ref) {
	long start = 0L, end = 0;
	long result = 0L; // in nanoseconds
	int entries = plst.GetNumEntries();
	int i = 0;

	for (i = ref; i < entries; i++) {
	    end = plst.GetEndEntry(i);
	    if (offset <= end)
		break;
	}
	if (plst.getType().equals(TChunkList.VIDEO_TYPE)) {
	    for (int j = 0; j < i; j++)
		result += plst.GetFrameTimeEntry(j);

	    result /= 1000000L;
	}
	if (plst.getType().equals(TChunkList.AUDIO_TYPE)) {
	    long totalSize = plst.GetTotalSize(i);
	    result = totalSize / ((AudioFormat) format).getBytesPerSec();
	}

	double sec = (double) result;
	return (new Time(sec));
    }

    public Time offset2Time(long offset) {
	long result = 0L;
	long total = 0L;

	if (!tableDriven) {
	    if (((AudioFormat) format).getBytesPerSec() != 0) {
		result = (offset - (long) ((AudioFormat) format)
			.getHeaderSize())
			* 1000000000L
			/ ((AudioFormat) format).getBytesPerSec();
	    }
	} else {

	    if (plst.getType().equals(TChunkList.AUDIO_TYPE)) {
		for (int i = 0; i < plst.GetNumEntries(); i++) {
		    if (plst.GetEndEntry(i) > offset) {
			total = plst.GetTotalSize(i);
			break;
		    }
		}
		result = total * 1000000000L
			/ ((AudioFormat) format).getBytesPerSec();
	    }

	    if (plst.getType().equals(TChunkList.VIDEO_TYPE)) {
		for (int i = 0; i < plst.GetNumEntries(); i++) {
		    if (plst.GetEndEntry(i) > offset) {
			result *= 1000L;
			break;
		    }
		    result += plst.GetFrameTimeEntry(i);
		}
	    }
	}
	return (new Time(result));
    }

    // This method calculates the index given time in nanoseconds.
    //
    public int time2Index(Time time) {
	int index = 0;
	if (plst == null)
	    return -1;

	if (plst.getType().equals(TChunkList.VIDEO_TYPE)) {
	    index = plst.GetFrameIndex(time.getNanoseconds());
	    while (index > 0) {
		if (plst.GetKFEntry(index))
		    break;
		index--;
	    }
	} else if (plst.getType().equals(TChunkList.AUDIO_TYPE)) {
	    long byteSkip = (long) (time.getNanoseconds() * ((AudioFormat) format)
		    .getBytesPerSec()) / 1000000000L;
	    index = plst.GetSoundIndex(byteSkip);
	}
	return index;
    }

    /**
     * This method calculates suggested buffer size.
     */
    public int getSuggestedBufferSize() {
	int lm = 0;

	if (!tableDriven && (format instanceof AudioFormat)) {
	    lm = ((AudioFormat) format).getBytesPerSec();
	    return lm;
	}

	if (plst == null)
	    return lm;

	if (plst.getType().equals(TChunkList.AUDIO_TYPE))
	    lm = ((AudioFormat) format).getBytesPerSec();

	if (plst.getType().equals(TChunkList.VIDEO_TYPE))
	    lm = 30 * ((VideoFormat) format).getMaxVideoSize();

	return lm;
    }

    public void dispose() {
	if (plst != null)
	    plst.dispose();
    }

}
