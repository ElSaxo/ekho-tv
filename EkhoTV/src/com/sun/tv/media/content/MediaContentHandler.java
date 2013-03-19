/*
 * @(#)MediaContentHandler.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.content;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ContentHandler;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

import javax.media.Manager;
import javax.media.NoPlayerException;

import nl.ekholabs.ekhotv.activity.EkhoTVProxyActivity;
import android.content.Context;

import com.sun.tv.media.ui.PlayerWindow;

public class MediaContentHandler extends ContentHandler {

    private boolean findCaller() {
	Throwable t = new Throwable();
	ByteArrayOutputStream ba = new ByteArrayOutputStream();
	PrintStream pos = new PrintStream(ba);
	t.printStackTrace(pos);
	String trace = ba.toString();
	// Log.e("EkhoTV", trace);
	StringTokenizer tok = new StringTokenizer(trace);
	tok.nextToken();
	tok.nextToken();
	tok.nextToken();
	tok.nextToken();
	tok.nextToken();
	tok.nextToken();
	tok.nextToken();
	tok.nextToken();
	String s = tok.nextToken();
	// Log.e("EkhoTV", s);
	// Log.e("EkhoTV", "CHECKING>>>>");
	if (s.lastIndexOf("hotjava.doc.DocParser.run", s.length()) >= 0)
	    return true;
	// Log.e("EkhoTV",
	tok.nextToken();
	s = tok.nextToken();
	// Log.e("EkhoTV", s);
	if (s.lastIndexOf("hotjava.doc.DocParser.run", s.length()) >= 0)
	    return true;
	return false;
    }

    public Object getContent(URLConnection uc) {
	try {
	    URL url = uc.getURL();
	    javax.media.Player newPlayer;
	    // System.err.println("URL is:" + url);
	    if (findCaller()) {
		// Log.e("EkhoTV", "Attempting to create a player");
		if ((newPlayer = Manager.createPlayer(url)) == null) {
		    String n = new String("Could not create a player for URL "
			    + url + " of MIME type " + uc.getContentType());
		    return n;
		}
		EkhoTVProxyActivity proxy = EkhoTVProxyActivity.getInstance();
		Context context = proxy.getActivity().getApplicationContext();

		PlayerWindow pw = new PlayerWindow(context, newPlayer);
		pw.setKeepScreenOn(true);
		return new String("Playing.....");
	    }
	    return uc.getInputStream();
	} catch (IOException e) {
	    return new String("Error reading URL " + uc.getURL());

	} catch (NoPlayerException e) {
	    return new String("NoPlayerException " + uc.getURL());
	} // catch

    }// getContent()
}
