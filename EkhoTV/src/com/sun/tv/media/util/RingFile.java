/*
 * @(#)RingFile.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.util.Log;

/**
 * RingBuffer
 */
// $$TODO: Change the name of this class to something more appropriate
// $$TODO: The previous version of this class can be used as a RingFile
// $$TODO: if necessary.
public class RingFile {

    private static boolean DEBUG = false;
    final private int MIN_CACHE_SIZE = 2500000;
    private static boolean nativeLoaded = false; // Don't change this
    private LoopThread downloadThread;
    private int lowMark, highMark;
    private boolean loading = true;
    public String ID;
    private boolean filling = true; // are we filling the buffer?
    private RandomAccessFile writeRAF;
    private RandomAccessFile readRAF;
    // There is a bug in this native IO code or a bug in native IO system code.
    private boolean nativeFlag = true; // are we using native code to bypass
    // Security Manager?
    private boolean b_flushed = false;
    private long eos = -1;
    private int currentRead; // current read file offset (in Ring)
    private int currentWrite; // current write file offset (in Ring)

    private int readBytes; // current read file offset (in the real file)
    private int writtenBytes; // current write file offset (in the real file)

    private int bufferSize;
    private int fileSize = 1; // The initial value has to be 1 !!

    private int wFd;
    private int rFd;
    private int LENGTH;
    private File bf;

    // Native methods
    private native void initFile(String fullName);

    private native int nativeRead(int fd, byte[] buffer, int offset, int length);

    private native int nativeWrite(int fd, byte[] buffer, int offset, int length);

    private native boolean nativeSeek(int fd, int pos);

    private native void nativeClose(int fd);

    private native int nativeTell(int fd);

    private native void nativeDelete(String fullName);

    private native static boolean checkAccess(String fileName);

    private String fullFileName;

    private boolean blockread = true;
    private boolean disposed = false;

    public RingFile(File f, int bs) {
	// Log.e("EkhoTV", Thread.currentThread() + " created RingFile " +
	// this + ": " + f + ": " + bs);
	filling = true;
	bf = f;
	if (bs < MIN_CACHE_SIZE)
	    bs = MIN_CACHE_SIZE;

	bufferSize = bs;
	lowMark = bufferSize / 5;
	highMark = 4 * bufferSize / 5;

	if (!nativeFlag) {
	    try {
		File path = new File(f.getParent());
		if (!path.exists())
		    path.mkdirs();
		if (f.exists())
		    f.delete();
		if (!f.exists()) {
		    writeRAF = new RandomAccessFile(f, "rw");
		    readRAF = new RandomAccessFile(f, "r");
		    // Log.e("EkhoTV", "RingFile: using Java IO system");
		    if (DEBUG)
			PRINT_DEBUG_MSG("RingFile: using Java IO system");
		    // Log.e("EkhoTV", "File is " + f);
		    fullFileName = f.toString();
		}
	    } catch (IOException e) {
		Log.e("EkhoTV", "FileCaching ctor: IOException is caught: " + e);
		return;
	    } catch (SecurityException e) {
		System.out
			.println("FileCaching ctor: SecurityException is caught: "
				+ e);
		nativeFlag = true; // try native IO
	    }
	}
	if (nativeFlag) {
	    // Log.e("EkhoTV", "RingFile: trying native IO system");
	    if (DEBUG)
		PRINT_DEBUG_MSG("RingFile: trying native IO system");
	    try {
		if (!nativeLoaded) {
		    if (DEBUG)
			PRINT_DEBUG_MSG("RingFile: loading library");
		    // JMFSecurity.loadLibrary("jmutil");
		    nativeLoaded = true;
		}
		if (DEBUG)
		    PRINT_DEBUG_MSG("The file was " + f.getAbsolutePath());
		// String snmae = f.getName();
		// fullFileName = snmae =
		// ( (com.sun.media.MediaSourceNode.findLocalPath()) +
		// (com.sun.media.MediaSourceNode.generateFileName(snmae)));

		String snmae = f.getPath();
		fullFileName = snmae;
		initFile(snmae);

		if (DEBUG)
		    PRINT_DEBUG_MSG("RingFile: native init successful");
	    } catch (Exception exc) {
		Log.e("EkhoTV", "Failed to load native library" + exc);
		exc.printStackTrace();
		nativeFlag = false;
	    }
	}
    }

    public int getFileSize() {
	return fileSize - 1;
    }

    public boolean willReadBlock() {
	return blockread;
    }

    public void waitUntilReadWontBlock() {
	if (!blockread)
	    return;
	while (blockread) {
	    synchronized (this) {
		try {
		    wait();
		} catch (InterruptedException e) {
		}
	    }
	}
    }

    // Modified
    public long getStart() {
	// long t = readBytes - getAvailable();
	// return t;
	return 0;
    }

    // Modified
    public long getEnd() {
	// return writtenBytes;
	return fileSize;
    }

    public boolean isValid() {
	return (((writeRAF != null) && (readRAF != null)) || ((rFd != 0) && (wFd != 0)));
    }

    public void setCurrentIndex(long l) {
	if (DEBUG)
	    PRINT_DEBUG_MSG("RB.setCurrentIndex to " + l);
	readBytes = writtenBytes = (int) l;
    }

    public void setID(String id) {
	ID = id;
    }

    public int getBufferSize() {
	return bufferSize;
    }

    public synchronized boolean loadCondition() {
	if (blockread) {
	    if ((eos != -1) || (getLength() >= highMark)) {
		blockread = false;
		notify();
	    }
	}
	return false;
    }

    public synchronized boolean drainCondition() {
	if (eos != -1) {
	    if (blockread) {
		blockread = false;
		notify();
	    }
	    return false;
	}

	if (blockread) {
	    if (getLength() < highMark) {
		return true;
	    } else {
		blockread = false;
		notify();
		return false;
	    }
	} else {
	    if (getLength() < (highMark / 5)) {
		blockread = true;
		return true;
	    } else {
		return false;
	    }
	}
    }

    public synchronized int write(byte buffer[], int offset, int size)
	    throws IOException {
	int len = size;
	int i;
	// Log.e("EkhoTV", "RF: write: currentWrite BEFORE is " +
	// currentWrite);
	if (DEBUG)
	    PRINT_DEBUG_MSG("RingFile: writing. ReadP = " + currentRead
		    + ", writeP = " + currentWrite + ", read = " + readBytes
		    + ", written = " + writtenBytes + ", bufSize = "
		    + bufferSize + ", size = " + size + ", fileSize = "
		    + fileSize);
	if (nativeFlag)
	    if (DEBUG)
		PRINT_DEBUG_MSG("Write pointer is at " + nativeTell(wFd));
	    else if (DEBUG)
		PRINT_DEBUG_MSG("Write pointer is at "
			+ writeRAF.getFilePointer());

	while (loadCondition()) {
	    if (DEBUG)
		PRINT_DEBUG_MSG("RingFile: full, about to block");
	    try {
		wait();
	    } catch (InterruptedException e) {
	    }
	}
	if (b_flushed)
	    return size; // full my parent into thinking I'm done
	if (len > getAvailable())
	    len = getAvailable();
	if (DEBUG)
	    PRINT_DEBUG_MSG("RingFile: will write " + len);
	boolean wrap;
	if ((len + currentWrite) > bufferSize)
	    wrap = true;
	else
	    wrap = false;

	// Log.e("EkhoTV", "RF: write wrap is " + wrap);
	if (wrap) {
	    int chunk2 = (len + currentWrite) % bufferSize;
	    int chunk1 = len - chunk2;
	    if (DEBUG)
		PRINT_DEBUG_MSG("RingFile: will wrap with " + chunk1 + " and "
			+ chunk2);
	    // System.in.read();
	    if (nativeFlag) {
		if (nativeWrite(wFd, buffer, offset, chunk1) != chunk1)
		    Log.e("EkhoTV", "CANNOT WRITE1 "
			    + Thread.currentThread().hashCode() + ": " + rFd
			    + ": " + wFd);
		if (!nativeSeek(wFd, 0))
		    Log.e("EkhoTV", "SEEK FAILED");
		if (nativeWrite(wFd, buffer, offset + chunk1, chunk2) != chunk2)
		    Log.e("EkhoTV", "CANNOT WRITE2 "
			    + Thread.currentThread().hashCode() + ": " + rFd
			    + ": " + wFd);
		// if (DEBUG)
		PRINT_DEBUG_MSG("Write pointer is at " + nativeTell(wFd));
	    } else {
		writeRAF.write(buffer, offset, chunk1);
		writeRAF.seek(0);
		writeRAF.write(buffer, offset + chunk1, chunk2);
	    }
	    // System.in.read();
	} else {
	    if (nativeFlag) {
		// Log.e("EkhoTV", Thread.currentThread().getName() +
		// "  nativeWrite: " + offset + ": " + len);
		int actualBytesWritten = -1;
		actualBytesWritten = nativeWrite(wFd, buffer, offset, len);

		if (actualBytesWritten != len) {
		    return (-1);
		}
	    } else
		writeRAF.write(buffer, offset, len);
	}
	fileSize += len;
	if (fileSize > bufferSize)
	    fileSize = bufferSize;
	currentWrite = (currentWrite + len) % bufferSize;
	writtenBytes += len;
	LENGTH -= len;
	if (DEBUG)
	    PRINT_DEBUG_MSG("RingFile: writing. ReadP = " + currentRead
		    + ", writeP = " + currentWrite + ", read = " + readBytes
		    + ", written = " + writtenBytes + ", bufSize = "
		    + bufferSize + ", size = " + size + ", fileSize = "
		    + fileSize);

	if (!isEmpty())
	    notify();

	// Log.e("EkhoTV", "  RF: write: currentWrite AFTER is " +
	// currentWrite);

	return len;
    }

    public synchronized int read(byte buffer[], int offset, int len)
	    throws IOException {
	int avail = len;
	// Log.e("EkhoTV", "RF: read: currentRead BEFORE is " + currentRead);
	// Log.e("EkhoTV", "RF: read: readBytes BEFORE is " + readBytes);

	if (DEBUG)
	    PRINT_DEBUG_MSG("RingFile: reading. ReadP = " + currentRead
		    + ", writeP = " + currentWrite + ", read = " + readBytes
		    + ", written = " + writtenBytes + ", bufSize = "
		    + bufferSize + ", size = " + len + ", fileSize = "
		    + fileSize + " offset = " + offset);

	if (nativeFlag)
	    if (DEBUG)
		PRINT_DEBUG_MSG("Read pointer is at " + nativeTell(rFd));
	    else if (DEBUG)
		PRINT_DEBUG_MSG("Read pointer is at "
			+ readRAF.getFilePointer());
	if (drainCondition())
	    notify();

	while (drainCondition()) {
	    if (DEBUG)
		PRINT_DEBUG_MSG("RingFile: empty, about to block");
	    try {
		wait(100);
		if (atEnd())
		    return -1;
	    } catch (InterruptedException e) {
	    }
	}

	if (disposed) {
	    return -1;
	}

	boolean wrap;
	if (len > getLength()) {
	    avail = getLength();
	}
	{
	    // $$$ ADDED BY BABU 3/10/98 //
	    if (avail == 0) {
		return 0;
	    }
	}
	if (DEBUG)
	    PRINT_DEBUG_MSG("RingFile: will read " + avail);
	if ((currentRead + avail) > bufferSize)
	    wrap = true;
	else
	    wrap = false;

	if (wrap) {
	    int chunk2 = (currentRead + avail) % bufferSize;
	    int chunk1 = avail - chunk2;
	    if (DEBUG)
		PRINT_DEBUG_MSG("RingFile: will wrap with " + chunk1 + " and "
			+ chunk2);
	    if (nativeFlag) {
		if (nativeRead(rFd, buffer, offset, chunk1) != chunk1)
		    Log.e("EkhoTV", "CANNOT READ");
		if (!nativeSeek(rFd, 0))
		    Log.e("EkhoTV", "CANNOT SEEK");
		if (nativeRead(rFd, buffer, offset + chunk1, chunk2) != chunk2)
		    Log.e("EkhoTV", "CANNOT READ");
	    } else {
		readRAF.read(buffer, offset, chunk1);
		readRAF.seek(0);
		readRAF.read(buffer, offset + chunk1, chunk2);
	    }
	    /*
	     * Log.e("EkhoTV", "READING BEFORE CUT "+(((int)
	     * buffer[offset+chunk1-1])&0xff)); Log.e("EkhoTV",
	     * "READING AFTER CUT "+(((int) buffer[offset+chunk1])&0xff));
	     */
	} else {
	    if (nativeFlag) {
		avail = nativeRead(rFd, buffer, offset, avail);
		// if (DEBUG)
		// PRINT_DEBUG_MSG("Native Read "+avail+" bytes");
	    } else
		readRAF.read(buffer, offset, avail);
	}
	currentRead = (currentRead + avail) % bufferSize;
	readBytes += avail;
	LENGTH += avail;
	if (DEBUG)
	    PRINT_DEBUG_MSG("RingFile: reading. ReadP = " + currentRead
		    + ", writeP = " + currentWrite + ", read = " + readBytes
		    + ", written = " + writtenBytes + ", bufSize = "
		    + bufferSize + ", size = " + len + ", fileSize = "
		    + fileSize);

	notify();
	// Log.e("EkhoTV", "  RF: read: currentRead AFTER is " +
	// currentRead);
	// Log.e("EkhoTV", "  RF: read: readBytes AFTER is " + readBytes);
	// Log.e("EkhoTV", "rf: read bytes " + avail);
	// Added. after a read blockread may become true
	drainCondition(); // to update blockread
	return avail;
    }

    // ADDED
    public int tell() {
	return readBytes;
    }

    public synchronized long seek(long hoff) {
	// Assumption: things are being written in consecutive order
	int diff;
	diff = (int) (hoff - readBytes);
	if (DEBUG)
	    PRINT_DEBUG_MSG("Seeking by " + diff + " bytes, file Size = "
		    + fileSize);
	try {
	    if (nativeFlag) {
		if (DEBUG)
		    PRINT_DEBUG_MSG("Write pointer is at " + nativeTell(wFd));
		if (DEBUG)
		    PRINT_DEBUG_MSG("Read pointer is at " + nativeTell(rFd));
	    } else {
		if (DEBUG)
		    PRINT_DEBUG_MSG("Write pointer is at "
			    + writeRAF.getFilePointer());
		if (DEBUG)
		    PRINT_DEBUG_MSG("Read pointer is at "
			    + readRAF.getFilePointer());
	    }
	} catch (IOException e) {
	}
	boolean canSeek;
	int tst = currentRead - currentWrite;
	if (diff > 0)
	    tst = -tst;
	tst = tst % fileSize;
	if (DEBUG)
	    PRINT_DEBUG_MSG("FILE SIZE = " + fileSize);
	if (tst <= 0)
	    tst += fileSize;
	// canSeek = (tst > Math.abs(diff));
	if (fileSize < bufferSize) { // Bug fix: 4099315
	    canSeek = (tst >= Math.abs(diff));
	} else {
	    canSeek = (tst > Math.abs(diff));
	}

	if (canSeek) {
	    readBytes = (int) hoff;
	    currentRead += diff;
	    LENGTH += diff;
	    currentRead %= fileSize;
	    if (currentRead < 0)
		currentRead += fileSize;
	    if (DEBUG)
		PRINT_DEBUG_MSG("Seeking to " + currentRead + " offset");
	    if (nativeFlag)
		nativeSeek(rFd, currentRead);
	    else
		try {
		    readRAF.seek(currentRead);
		} catch (Exception e) {
		    if (DEBUG)
			PRINT_DEBUG_MSG("tried Seeking by " + diff
				+ " bytes, file Size = " + fileSize);
		    if (DEBUG)
			PRINT_DEBUG_MSG("SYSTEM STATE:");
		    if (DEBUG)
			PRINT_DEBUG_MSG("currentRead: " + currentRead
				+ "totalRead: " + readBytes);
		    if (DEBUG)
			PRINT_DEBUG_MSG("currentWrite: " + currentWrite
				+ "totalWrite: " + writtenBytes);
		    if (DEBUG)
			PRINT_DEBUG_MSG("fileSize: " + fileSize + "MaxSize: "
				+ bufferSize);

		    try {
			if (nativeFlag) {
			    if (DEBUG)
				PRINT_DEBUG_MSG("Write pointer is at "
					+ nativeTell(wFd));
			    if (DEBUG)
				PRINT_DEBUG_MSG("Read pointer is at "
					+ nativeTell(rFd));
			} else {
			    if (DEBUG)
				PRINT_DEBUG_MSG("Write pointer is at "
					+ writeRAF.getFilePointer());
			    if (DEBUG)
				PRINT_DEBUG_MSG("Read pointer is at "
					+ readRAF.getFilePointer());
			}
		    } catch (IOException f) {
		    }

		    System.err.println("Something bad happened in seek" + e);
		    e.printStackTrace();
		}
	    // DEBUG = false;
	} else {// somethign happens
	    if (DEBUG)
		PRINT_DEBUG_MSG("Cannot seek there..." + hoff + " bytesRead="
			+ readBytes + " writtenBytes=" + writtenBytes);
	    if (diff > 0) { // we can catch up ...
		while (writtenBytes <= ((int) hoff)) {
		    readBytes = writtenBytes;
		    currentRead = currentWrite;
		    LENGTH = bufferSize;
		    try {
			if (nativeFlag)
			    if (DEBUG)
				PRINT_DEBUG_MSG("Advancing to " + readBytes
					+ " read pos " + nativeTell(rFd)
					+ " write pos " + nativeTell(wFd));
			    else if (DEBUG)
				PRINT_DEBUG_MSG("Advancing to " + readBytes
					+ " read pos "
					+ readRAF.getFilePointer()
					+ " write pos "
					+ writeRAF.getFilePointer());
		    } catch (IOException e) {
		    }
		    notify();
		    // try{
		    // System.in.read();
		    // }catch(IOException e) {}
		    try {
			wait();
		    } catch (InterruptedException e) {
		    }
		}
		diff = (int) (hoff - readBytes);
		readBytes = (int) hoff;
		currentRead = (currentRead + diff) % fileSize;
		// DEBUG = false;
		if (nativeFlag)
		    nativeSeek(rFd, currentRead);
		else
		    try {
			readRAF.seek(currentRead);
		    } catch (Exception e) {
			System.err
				.println("Something bad happened in seek" + e);
			e.printStackTrace();
		    }
	    } else {
		flush();
		notify();
	    }
	}
	{
	    // Log.e("EkhoTV", "rf: seek calling drainCondition: offset " +
	    // hoff);
	    drainCondition(); // to update blockread
	    // $$ TODO: can remove the notify as it is done in drainCondition
	    notify();
	}
	return readBytes;
    }

    public synchronized void setLowWatermark(int l) {
	lowMark = l;
	// Log.e("EkhoTV", "rf: set lowMark " + lowMark);
	notify();
    }

    public synchronized void setHighWatermark(int h) {
	if (h > highMark)
	    loading = true;

	highMark = h;
	// Log.e("EkhoTV", Thread.currentThread() + ": rf: set highMark " +
	// highMark);
	notify();
    }

    public int getHighWatermark() {
	return highMark;
    }

    private boolean ORIGaboveHigh() {
	int length = getAvailable();
	return (length < lowMark);
    }

    private boolean aboveHigh() {
	int length = getLength();
	return (length >= highMark);
    }

    private boolean belowLow() {
	int length = getLength();
	return (length < lowMark);
    }

    private boolean isFull() {
	return (((currentWrite + 1) % bufferSize) == currentRead);
    }

    private boolean isEmpty() {
	return (currentRead == currentWrite);
    }

    private int getAvailable() {
	int diff = currentRead - currentWrite - 1;
	if (diff < 0)
	    diff += bufferSize;
	return diff;
    }

    public int getLength() {
	int diff = currentWrite - currentRead;
	if (diff < 0)
	    diff += bufferSize;
	return diff;
    }

    public static boolean canCreateCacheFile(String file) {
	// TODO: Add non-native later
	boolean status = checkAccess(file);
	if (!status) {
	    System.err.println("Cannot create cache file " + file);
	}
	return status;
    }

    public synchronized void dispose() {
	blockread = false;

	if (nativeFlag) {
	    nativeClose(rFd);
	    nativeClose(wFd);
	    nativeDelete(fullFileName);
	    rFd = wFd = 0; // ADDED
	} else {
	    try {
		readRAF.close();
		writeRAF.close();
		bf.delete();
	    } catch (Exception e) {
		// Log.e("EkhoTV", "dispose " + fullFileName +
		// " got Exception: " + e);
		// Log.e("EkhoTV",
		// "dispose calling nativeDelete as SecurityException is thrown");
		nativeDelete(fullFileName); // when Java fails go native!
	    }
	}
	disposed = true;
	synchronized (this) {
	    notify();
	}
    }

    private void flush() {
	if (DEBUG)
	    PRINT_DEBUG_MSG("flush called");
	// Thread.currentThread().dumpStack();
	currentRead = currentWrite = 0;
	if (nativeFlag) {
	    nativeSeek(rFd, 0);
	    nativeSeek(wFd, 0);
	} else {
	    try {
		readRAF.seek(0);
		writeRAF.seek(0);
	    } catch (IOException e) {
	    }
	}
	readBytes = writtenBytes = 0;
	fileSize = 1;
	// fileSize = 1;
	// TODO: actually truncate the file???.
	b_flushed = true;
	downloadThread.pause();
    }

    public synchronized boolean flushed() {
	boolean ret;
	ret = b_flushed;
	b_flushed = false;
	return ret;
    }

    public void setEos() {
	loading = false;
	eos = writtenBytes;
	// Log.e("EkhoTV", "RF: setEos: loading is false: total length is " +
	// eos);
	synchronized (this) {
	    blockread = false;
	    // Log.e("EkhoTV", "setEos: notify blockread false");
	    notify();
	}
    }

    public boolean isEosReached() {
	return (eos != -1);
    }

    public void setEnd(long e) {
	eos = e;
    }

    public boolean atEnd() {
	return (eos == readBytes);
    }

    public synchronized void setDownloadThread(LoopThread t) {
	downloadThread = t;
    }

    private void PRINT_DEBUG_MSG(String str) {
	if (DEBUG)
	    // Log.e("EkhoTV", Thread.currentThread().toString() + str); //$$
	    Log.e("EkhoTV", str);
    }

    protected void finalize() {
	// dispose();
    }
}
