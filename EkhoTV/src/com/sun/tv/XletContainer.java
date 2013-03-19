/*
 * @(#)XletContainer.java	1.9 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * This class is an AWT container that can be used by an Xlet. In particular,
 * the <code>getParent()</code> method on the Xlet will be overridden to prevent
 * the Xlet from gaining access to the root frame.
 */
public class XletContainer extends RelativeLayout {

    public XletContainer(Context context) {
	super(context);
    }

    @Override
    public void addView(View child) {
	super.addView(child);
    }
}