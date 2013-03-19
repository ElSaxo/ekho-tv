/*
 * @(#)RootFrame.java	1.4 08/09/15
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

class RootFrame extends View {

    public RootFrame(Context context) {
	super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
	if (isShown()) {
	    super.onDraw(canvas);
	}
    }
}