/*
 * @(#)MediaPullDataSource.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.media.Duration;
import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.Seekable;
import javax.media.protocol.SourceStream;

import com.sun.tv.media.MediaPullSourceStream;
import com.sun.tv.media.Parser;
import com.sun.tv.media.Track;
import com.sun.tv.media.content.InvalidTrackIDException;

public abstract class MediaPullDataSource extends
	javax.media.protocol.PullDataSource implements
	javax.media.protocol.Positionable {

    protected String contentType = null;
    protected boolean connected = false;
    protected PullSourceStream[] srcStreams;
    protected Parser parser;
    protected Track thisTrack;
    protected int trackID;

    // ************ Debugging only ******************
    private int activeID = 0;
    private int ID = 0;
    private static final boolean DEBUG = false;

    public void setID(int id) {
	ID = id;
    }

    // **********************************************

    public MediaPullDataSource() {
	parser = null;
	connected = false;
	thisTrack = null;
    }

    public MediaPullDataSource(MediaLocator source) {
	this();
	super.setLocator(source);
    }

    public void setLocator(MediaLocator ml) {

	// If it's file protocol, we'll try to strip out special characters
	// in the URL syntax:
	// %xx = the ASCII represented by the hexadecimal number "xx".
	if (ml.getProtocol() != null && ml.getProtocol().equals("file")) {
	    int idx;
	    MediaLocator saved = ml;
	    String file = ml.getRemainder();
	    boolean changed = false;

	    if (file == null) {
		super.setLocator(ml);
		return;
	    }
	    try {
		idx = 0;
		while ((idx = file.indexOf("%", idx)) >= 0) {
		    if (file.length() > idx + 2) {
			byte[] bytes = new byte[1];
			try {
			    bytes[0] = (byte) Integer.valueOf(
				    file.substring(idx + 1, idx + 3), 16)
				    .intValue();
			    file = file.substring(0, idx) + new String(bytes)
				    + file.substring(idx + 3);
			    changed = true;
			} catch (NumberFormatException ne) {
			}
		    }
		    idx++;
		}
		if (changed)
		    ml = new MediaLocator(ml.getProtocol() + ":" + file);
	    } catch (Exception e) {
		ml = saved;
	    }
	}

	super.setLocator(ml);
    }

    public abstract Object clone();

    public void setParser(Parser p) {
	parser = p;
    }

    public void setTrackID(int id) throws InvalidTrackIDException {
	if (parser == null)
	    return;
	Track tkList[] = parser.getTracks();
	trackID = id;
	if ((tkList == null) || (id >= tkList.length))
	    throw new InvalidTrackIDException();
	thisTrack = tkList[trackID];
    }

    /**
     * Obtain a string describing the content-type of media the source is
     * providing.
     * <p>
     * It is an error to call this if the source is unconnected.
     * 
     * @return Name describing media content.
     */
    public String getContentType() {
	if (!connected) {
	    // $$ TODO:
	    PRINT_DEBUG_MSG("Error: DataSource not connected");
	    return null;
	}
	return ContentDescriptor.mimeTypeToPackageName(contentType);
    }

    abstract public MediaPullSourceStream createPullSourceStream(
	    InputStream is, ContentDescriptor type, URL u, long length);

    public URL getURL() {
	try {
	    return getLocator().getURL();
	} catch (MalformedURLException e) {
	    // Don't know how to deal with non-URL locator yet!
	    return null;
	}
    }

    /**
     * Opens a connection to the source described by the URL.
     * <p>
     * Connect initiates communmication with the source.
     * 
     * @exception IOException
     *                thrown if the connect has IO trouble.
     */
    public void connect() throws IOException {

	URL url;
	long contentLength;

	try {
	    url = getLocator().getURL();
	} catch (MalformedURLException e) {
	    // Don't know how to deal with non-URL locator yet!
	    System.err.println(getLocator()
		    + ": Don't know how to deal with non-URL locator yet!");
	    throw (new IOException(this + ": connect() failed"));
	}

	URLConnection urlC;

	PRINT_DEBUG_MSG("DataSource.connect(), connect = " + connected
		+ "; URL = " + url);

	if (url == null) {
	    // $$ TODO: ??
	    return;
	}

	if (connected)
	    return;

	srcStreams = new PullSourceStream[1];
	// try {
	// JMFSecurity.enablePrivilege.invoke(JMFSecurity.privilegeManager,
	// JMFSecurity.connectArgs);
	// } catch (Exception e) {}
	urlC = url.openConnection();
	contentType = urlC.getContentType();
	contentLength = (long) urlC.getContentLength();
	if (contentLength < 0)
	    contentLength = SourceStream.LENGTH_UNKNOWN;

	// Log.e("EkhoTV", "contentType is " + contentType);
	// $$$ TODO Replace if with getCorrectedContentType
	if (contentType != null) {
	    if (contentType.equals("audio/wav")) {
		contentType = "audio/x-wav";
		// Log.e("EkhoTV", "audio/wav ==> audio/x-wav");
	    } else if (contentType.equals("audio/aiff")) {
		contentType = "audio/x-aiff";
		// Log.e("EkhoTV", "audio/aiff ==> audio/x-aiff");
	    } else if (contentType.equals("application/x-troff-msvideo")) {
		// $$ WORKAROUND DUE TO WRONG MIME TYPE GIVEN FOR AVI
		// System.out.print("MIME TYPE BUG: ");
		// Log.e("EkhoTV",
		// "application/x-troff-msvideo ==> video/x-msvideo");
		contentType = "video/x-msvideo";
	    } else if (contentType.equals("video/msvideo")) {
		contentType = "video/x-msvideo";
	    } else if (contentType.equals("content/unknown")
		    || contentType.startsWith("text/")) {
		// Catch a few well known types even if they are not defined
		// in the system MIME table.
		String type = guessContentType(getLocator());
		if (type != null)
		    contentType = type;
	    }
	} else
	    contentType = "content/unknown";

	Object inputStream = urlC.getInputStream();

	PRINT_DEBUG_MSG("Content type is " + contentType);
	PRINT_DEBUG_MSG("getContent() ==> " + inputStream);

	if ((inputStream == null) || !(inputStream instanceof InputStream)) {
	    srcStreams[0] = null;
	    return;
	}

	srcStreams[0] = createPullSourceStream((InputStream) inputStream,
		new ContentDescriptor(contentType), url, contentLength);
	connected = true;
    }

    // Guess the contentType based on the file extension.
    private String guessContentType(MediaLocator locator) {
	String path = locator.getRemainder();
	int i = path.lastIndexOf(".");
	if (i != -1) {
	    String ext = path.substring(i + 1).toLowerCase();

	    if (ext.equals("mov"))
		return "video/quicktime";
	    else if (ext.equals("avi"))
		return "video/x_msvideo";
	    else if (ext.equals("mpg"))
		return "video/mpeg";
	    else if (ext.equals("viv"))
		return "video/vivo";
	    else if (ext.equals("au"))
		return "audio/basic";
	    else if (ext.equals("wav"))
		return "audio/x_wav";
	    else if (ext.equals("mid") || ext.equals("midi"))
		return "audio/midi";
	    else if (ext.equals("rmf"))
		return "audio/rmf";
	    else if (ext.equals("gsm"))
		return "audio/x_gsm";
	    else if (ext.equals("mp2"))
		return "audio/x-mpegaudio";
	    else if (ext.equals("swf"))
		return "application/x-shockwave-flash";
	    else if (ext.equals("spl"))
		return "application/futuresplash";
	}
	return null;
    }

    /**
     * Close the connection to the source described by the URL.
     * <p>
     * Disconnect frees resources used to maintain a connection to the source.
     * If no resources are in use, disconnect is ignored. Implies a stop, if
     * stop hasn't already been called.
     */
    public void disconnect() {
	PRINT_DEBUG_MSG("DataSource: disconnect: stub");
	if (srcStreams != null) {
	    for (int i = 0; i < srcStreams.length; i++) {
		if (srcStreams[i] != null
			&& srcStreams[i] instanceof URLPullSourceStream) {
		    ((URLPullSourceStream) srcStreams[i]).close();
		}
	    }
	}
    }

    /**
     * Initiates data-transfer. Start must be called before data is available.
     * Connect must be called before start.
     * 
     * @exception IOException
     *                thrown if the source has IO trouble at startup time.
     */
    public void start() throws IOException {
    }

    /**
     * Stops data-transfer. If the source has not already been connected and
     * started, stop does nothing.
     */
    public void stop() {
    }

    /**
     * Obtain the collection of streams that this source manages. The collection
     * of streams is entirely content dependent. The mime-type of this
     * DataSource provides the only indication of what streams can be available
     * on this connection.
     * 
     * @return collection of streams for this source.
     */
    public PullSourceStream[] getStreams() {
	if (!connected)
	    return null;
	return srcStreams;
    }

    /**
     * Return if this source can position anywhere.. If not it can only be
     * repositioned to the begining.
     * 
     * @return true if sources is random access, false if only resetable.
     */
    public boolean isRandomAccess() {
	return true;
    }

    public boolean dataAvailable() {
	return false;
    }

    public void close() {
    }

    /**
     * Position to the specified chunk in a active track.
     * 
     * @param index
     *            chunk index of the active track.
     * @return the actual position set, in nanoseconds.
     */
    public long setPosition(int index) {
	long computedPosition = -1L;
	long actualPosition = -1L;
	// MediaPullSourceStream mpss;

	if (parser == null)
	    return actualPosition;

	if (srcStreams[0] instanceof Seekable) {
	    if (index >= 0)
		computedPosition = thisTrack.index2Offset(index);
	    else
		computedPosition = 0;
	    actualPosition = ((Seekable) srcStreams[0]).seek(computedPosition);
	}
	return actualPosition;
    }

    /**
     * Position to the specified time. The actual rounded position is returned.
     * 
     * @param time
     *            where to position the stream in nanoseconds.
     * @param round
     *            RoundUp, RoundDown, RoundNearest.
     * @return the actual position set, in nanoseconds.
     */
    public Time setPosition(Time time, int rounding) {
	long computedPosition = -1L;
	long actualPosition = -1L;
	Time actualTime = new Time(0);

	if (parser == null)
	    return new Time(actualPosition);

	if (srcStreams[0] instanceof Seekable) {
	    computedPosition = thisTrack.time2Offset(time);
	    actualPosition = ((Seekable) srcStreams[0]).seek(computedPosition);
	    if (actualPosition <= -1L)
		return new Time(-1L);
	    actualTime = thisTrack.offset2Time(actualPosition);
	    return actualTime;
	} else {
	    return new Time(-1L);
	}
    }

    public Time getDuration() {
	return Duration.DURATION_UNKNOWN;
    }

    /**
     * Returns an zero length array because no controls are supported.
     * 
     * @return a zero length <code>Object</code> array.
     */
    public Object[] getControls() {
	return new Object[0];
    }

    /**
     * Returns <code>null</code> because no controls are implemented.
     * 
     * @return <code>null</code>.
     */
    public Object getControl(String controlName) {
	return null;
    }

    protected String getCorrectedContentType(String contentType) {
	if (contentType != null) {
	    if (contentType.equals("audio/wav")) {
		contentType = "audio/x-wav";
		// Log.e("EkhoTV", "audio/wav ==> audio/x-wav");
	    } else if (contentType.equals("audio/aiff")) {
		contentType = "audio/x-aiff";
		// Log.e("EkhoTV", "audio/aiff ==> audio/x-aiff");
	    } else if (contentType.equals("application/x-troff-msvideo")) {
		// $$ WORKAROUND DUE TO WRONG MIME TYPE GIVEN FOR AVI
		// System.out.print("MIME TYPE BUG: ");
		// Log.e("EkhoTV",
		// "application/x-troff-msvideo ==> video/x-msvideo");
		contentType = "video/x-msvideo";
	    } else if (contentType.equals("video/msvideo")) {
		contentType = "video/x-msvideo";
	    } else if (contentType.equals("content/unknown")) {
		// Catch a few well known types even if they are not defined
		// in the system MIME table.
		String type = guessContentType(getLocator());
		if (type != null)
		    contentType = type;
	    }
	}
	return contentType;
    }

    public void PRINT_DEBUG_MSG(String str) {
	if (DEBUG)
	    System.err.println(str);
    }
}
