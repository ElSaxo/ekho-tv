/*
 * @(#)DisplayManager.java	1.16 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * This class implements the DisplayManager. The DisplayManager is a component
 * to an MHP implementation that controls access to the screen. Currently this
 * class assumes that the Xlets running on the platform are are displaying
 * themselves in a root container. The DisplayManager's only client is the
 * XletManager.
 * <p>
 * 
 * The DisplayManager will create a single frame and then create Containers
 * which will be placed in the root frame.
 * <p>
 * 
 * SCCS Version: @(#)DisplayManager.java 1.16 08/09/15
 * 
 * @version ifa
 */

public class DisplayManager {

    private static DisplayManager displayManager = null;

    private ViewGroup toplevelContainer;
    private ViewGroup rootlevelContainer;

    /**
     * Initialize the DisplayManager.
     */
    public DisplayManager(Context context) {

	// create RootFrame
	toplevelContainer = new RelativeLayout(context) {
	    public void addView(View v) {
		Log.i("EkhoTV", "ROOTFRAME:  ");
		/*
		 * if (c instanceof com.sun.media.amovie.VisualComponent) {
		 * Log.e("EkhoTV", "VIDEO -1:"+c); //Add as the last component
		 * In Zorder //gets draw first. //Thread.dumpStack();
		 * super.add(c, -1); } else { Log.e("EkhoTV", "OTHER 0:"+c);
		 * //Add as the first component In Zorder gets draw last.
		 * super.add(c,0); }
		 */
		Log.i("EkhoTV", "OTHER 0:" + v);
		// Add as the first component In Zorder gets draw last.
		super.addView(v, 0);

		// 6344575: refreshing after adding a component
		invalidate();
	    }

	};
	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
		LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

	toplevelContainer.setLayoutParams(params);
	toplevelContainer.setMinimumWidth(640);
	toplevelContainer.setMinimumHeight(480);
	toplevelContainer.invalidate();
	// to fix 4375018
	// wait for requests
    }

    public static DisplayManager createInstance(Context context) {
	if (displayManager != null) {
	    return displayManager;
	}

	DisplayManager.displayManager = new DisplayManager(context);

	return displayManager;
    }

    public ViewGroup getRootFrame() {
	return (ViewGroup) toplevelContainer;
    }

    /**
     * Create a new XletContainer. Will create a new XletContainer for an Xlet
     * to display it's content.
     */
    public XletContainer createXletContainer(Context context) {
	XletContainer container = new XletContainer(context);

	container.setVisibility(View.VISIBLE);

	container.setBackgroundColor(Color.WHITE);
	// container.setForegroundColor(Color.BLACK);
	container.setEnabled(true);
	toplevelContainer.addView(container);

	return container;
    }

    /**
     *
     *
     */
    public void destroyXletContainer(XletContainer container) {
	if (container != null) {
	    toplevelContainer.removeView(container);
	}
    }

    /**
     * Show the XletContainer.
     * <p>
     * 0) First, remove the existing component if there is one.
     * <p>
     * 1) Then add the new one.
     */
    public void showXletContainer(XletContainer container) {
	toplevelContainer.removeAllViews();
	toplevelContainer.addView(container);
    }

    /**
     * Hide the XletContainer This time we will just remove it.
     */
    public void hideXletContainer(XletContainer container) {
	toplevelContainer.removeAllViews();
    }

    /**
     * Signal the user that a Non Auto Start Xlet is can be executed.
     */
    public void signalUserNonAutoStartXlet() {
	Log.i("EkhoTV",
		"DisplayManager.signalUserNonAutoStartXlet() not implemented!!!!");
    }
}