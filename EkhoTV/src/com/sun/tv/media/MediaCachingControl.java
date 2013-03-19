/*
 * @(#)MediaCachingControl.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

import java.io.IOException;

import javax.media.CachingControl;
import javax.media.protocol.PullSourceStream;

import android.util.Log;
import android.view.View;

import com.sun.tv.media.protocol.reliable.caching.CachingInputStream;
import com.sun.tv.media.protocol.reliable.caching.FileCachingInputStream;
import com.sun.tv.media.protocol.reliable.caching.NewRFCachingInputStream;
import com.sun.tv.media.protocol.reliable.caching.RAMCachingInputStream;
import com.sun.tv.media.util.LoopThread;
import com.sun.tv.media.util.Update;

/**
 * MediaCachingControl implements Caching Control supported by <b>Players</b>
 * that are capable of reporting download progress. Typically this control is
 * discovered via the <b>Controller.getControls</b> method.
 * 
 * A Controller that supports this control will generate a
 * <b>CachingControlEvent</b> event. The event is generated often enough to
 * implement a progress GUI.
 * 
 */

public class MediaCachingControl implements CachingControl {

    private View progressBarComponent = null;
    // private CacheControlComponent controlComponent = null;
    private long length; // Length of the file.
    private long contentProgress;
    private String localPath = null;
    private CachingInputStream cacheStream = null;
    private int bufferSize;
    private DnloadThread dnloadThread;
    private Update callback = null;

    // DEBUGGING PURPOSE
    public String ID; // debugging purpose only (remove later)
    public boolean DEBUG = false;

    // Helper class: Print out the debug message.
    //
    public void PRINT_DEBUG_MSG(String str) {
	if (DEBUG)
	    Log.e("EkhoTV", str);
    }

    public void setID(String id) {
	ID = id;
	cacheStream.setID(id);
    }

    public MediaCachingControl() {
	contentProgress = 0L;
	// controlComponent = null;
	dnloadThread = null;
	bufferSize = 0;
    }

    public MediaCachingControl(PullSourceStream p, long len) {
	this();
	cacheStream = new RAMCachingInputStream(p);
	if (len <= 0)
	    length = LENGTH_UNKNOWN;
	else
	    length = len;
    }

    public MediaCachingControl(PullSourceStream p, long len, String lp) {
	this(p, len, lp, true); // use ring by default
    }

    public MediaCachingControl(PullSourceStream p, long len, String lp,
	    boolean useRing) {
	this();
	localPath = lp;
	if (useRing) {
	    PRINT_DEBUG_MSG("Streamable File. Using a ring File buffer.");
	    cacheStream = new NewRFCachingInputStream(p, localPath);
	} else {
	    cacheStream = new FileCachingInputStream(p, localPath);
	    PRINT_DEBUG_MSG("NonStreamable file. Will download the entire file.");
	}
	if (len <= 0)
	    length = LENGTH_UNKNOWN;
	else
	    length = len;
    }

    protected void setCacheStream(CachingInputStream i) {
	cacheStream = i;
    }

    protected void setLength(long l) {
	length = l;
    }

    protected void setLocalPath(String l) {
	localPath = l;
    }

    public void setCallback(Update u) {
	this.callback = u;
    }

    public Object clone() {
	MediaCachingControl mcc = new MediaCachingControl();
	mcc.setLocalPath(localPath);
	mcc.setCacheStream((CachingInputStream) (((NewRFCachingInputStream) cacheStream)
		.clone()));
	mcc.setLength(length);
	mcc.callback = callback;
	return mcc;
    }

    public int setBufferSize(int bsize) {
	bufferSize = -1;
	if (cacheStream != null) {
	    cacheStream.setBufferSize(bsize);
	    bufferSize = cacheStream.getBufferSize();
	}
	return bufferSize;
    }

    /**
     * True if media is downloading.
     * 
     * @return downloading state.
     */
    public boolean isDownloading() {
	boolean downloadFlag = false;

	if (cacheStream != null) {
	    if (cacheStream.getContentLength() == 0)
		downloadFlag = true;
	}
	return downloadFlag;
    }

    /**
     * The total number of bytes in the media, or it returns
     * <b>LENGTH_UNKNOWN</b> if this is not known.
     * 
     * @return content length in bytes, or <b>LENGTH_UNKNOWN</b>.
     */
    public long getContentLength() {
	return length;
    }

    /**
     * The total number of bytes in the media downloaded so far.
     * 
     * @return content downloaded in bytes.
     */
    public long getContentProgress() {
	contentProgress = cacheStream.getContentLength();
	return contentProgress;
    }

    /**
     * A Component displaying download progress.
     * 
     * @return progress bar GUI.
     */

    public View getProgressBarComponent() {
	/*
	 * if (progressBarComponent == null) progressBarComponent = new
	 * ProgressBar(this);
	 */
	return progressBarComponent;
    }

    /**
     * A Component for additional down load control.
     * 
     * If there is nothing more than the progress bar this will be null.
     * 
     * @return download control GUI.
     */
    public View getControlComponent() {
	return null;
	/*
	 * if ((controlComponent == null) && (progressBarComponent == null)) {
	 * 
	 * Player player = null;
	 * 
	 * // Total kludge ahead: we need to find the player // that is
	 * associated with this, so that we can // shut it down if the user
	 * selects the cancel button if ((callback != null) && (callback
	 * instanceof MediaController)) { Controller c =
	 * ((MediaController)callback).getParent();
	 * 
	 * if (c instanceof Player) { player = (Player)c; } }
	 * 
	 * controlComponent = new CacheControlComponent(this, player);
	 * progressBarComponent = controlComponent.getProgressBar(); } return
	 * controlComponent;
	 */
    }

    public void startDownload() {
	PRINT_DEBUG_MSG("$$$$ startDownload");
	if (dnloadThread == null) {
	    // try {
	    // JMFSecurity.enablePrivilege.invoke(
	    // JMFSecurity.privilegeManager, JMFSecurity.threadArgs);
	    // JMFSecurity.enablePrivilege.invoke(
	    // JMFSecurity.privilegeManager,
	    // JMFSecurity.threadGroupArgs);
	    // } catch (Exception e) {
	    // }

	    dnloadThread = new DnloadThread(this);
	    dnloadThread.setName("Download Timer Thread "
		    + getClass().getName());
	}
	dnloadThread.restart();
    }

    public void stopDownload() {
	if (dnloadThread != null)
	    dnloadThread.pause();
	PRINT_DEBUG_MSG("$$$$ stopDownload");
    }

    public void changeStream(PullSourceStream pss) {
	dnloadThread.pause();
	// Log.e("EkhoTV", "Attempting to stop the download");
	// try {
	// JMFSecurity.enablePrivilege.invoke(JMFSecurity.privilegeManager,
	// JMFSecurity.threadArgs);
	// JMFSecurity.enablePrivilege.invoke(JMFSecurity.privilegeManager,
	// JMFSecurity.threadGroupArgs);
	// } catch (Exception e) {
	// }
	try {
	    dnloadThread.stop();
	    // Log.e("EkhoTV", "Successful");
	} catch (Exception e) {
	    Log.e("EkhoTV", "FAILED");
	}
	dnloadThread = null;

	if (localPath == null) {
	    cacheStream.dispose();
	    cacheStream = new RAMCachingInputStream(pss);
	    cacheStream.setID(ID); // DEBUG
	    cacheStream.setBufferSize(bufferSize);
	}
	if (cacheStream instanceof NewRFCachingInputStream)
	    ((NewRFCachingInputStream) cacheStream).sync();
	dnloadThread = new DnloadThread(this);
	dnloadThread.restart();
	contentProgress = 0;
	// PRINT_DEBUG_MSG("     changeStream to " + cacheStream);
    }

    // Currently, localPath is hardcoded. It should based on what platform
    // the player runs on. Or the system.property.
    // (Note: Need to discuss the details with jmp-imp team. Cania)
    public String getLocalPath() {
	return localPath;
    }

    public final void dispose() {
	PRINT_DEBUG_MSG("MediaCachingControl.dispose() ");
	if (dnloadThread != null) {
	    // dnloadThread.kill();
	    // try {
	    // JMFSecurity.enablePrivilege.invoke(
	    // JMFSecurity.privilegeManager, JMFSecurity.threadArgs);
	    // JMFSecurity.enablePrivilege.invoke(
	    // JMFSecurity.privilegeManager,
	    // JMFSecurity.threadGroupArgs);
	    // } catch (Exception e) {
	    // }
	    dnloadThread.stop();
	    dnloadThread = null;

	    if (cacheStream != null)
		cacheStream.dispose();
	}
    }

    public PullSourceStream getStream() {
	return cacheStream;
    }

    public long seek(long to) {
	long where = -1;
	if (cacheStream != null)
	    where = cacheStream.seek(to);
	return where;
    }

    public long getStart() {
	long start = 0;
	if (cacheStream != null)
	    start = cacheStream.getStartAvail();
	return start;
    }

    public long getEnd() {
	long end = 0;
	if (cacheStream != null)
	    end = cacheStream.getEndAvail();
	return end;
    }

    // class DnloadThread extends Timer {
    class DnloadThread extends LoopThread {

	MediaCachingControl mcc;
	private long lastTime;
	private long lastProgress;

	DnloadThread(MediaCachingControl m) {
	    // super(0);
	    super();
	    this.pause();
	    // useAudioPriority();
	    useVideoPriority();
	    mcc = m;
	    // Log.e("EkhoTV", "DnloadThread created " + hashCode());
	    this.start();
	}

	// public void processTimer() {
	public boolean process() {
	    int readBytes;
	    CachingInputStream cis = (CachingInputStream) mcc.getStream();
	    try {
		readBytes = cis.download();

		if (MediaCachingControl.this.callback != null) {
		    long progress = getContentProgress();
		    if (progress - lastProgress > 2048) { // atleast 2K
			long nowTime = System.currentTimeMillis();
			if (nowTime - lastTime > 200) { // atleast 200 millisecs
			    if (MediaCachingControl.this.progressBarComponent != null)
				MediaCachingControl.this.progressBarComponent
					.invalidate();
			    MediaCachingControl.this.callback.update();
			    lastProgress = progress;
			    lastTime = nowTime;
			}
		    }
		}

		if (readBytes == -1) {
		    this.pause();
		    length = getContentProgress();
		    if (MediaCachingControl.this.progressBarComponent != null)
			MediaCachingControl.this.progressBarComponent
				.invalidate();
		    if (MediaCachingControl.this.callback != null)
			MediaCachingControl.this.callback.update();
		}
	    } catch (IOException e) {
		Log.e("EkhoTV", "IOException caught: " + e);
		cis = null;
		this.pause();
	    }
	    return true;
	}

	public void pause() {
	    super.pause();
	}

	public void restart() {
	    lastTime = System.currentTimeMillis();
	    lastProgress = 0;
	    super.restart();
	}
    }
}