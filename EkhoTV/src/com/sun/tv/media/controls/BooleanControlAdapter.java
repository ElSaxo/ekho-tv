/*
 * @(#)BooleanControlAdapter.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media.controls;

import javax.media.Control;

import android.view.View;

public class BooleanControlAdapter extends AtomicControlAdapter implements
	BooleanControl {

    public BooleanControlAdapter() {
	super(null, false, null);
    }

    public BooleanControlAdapter(View c, boolean def, Control parent) {
	super(c, def, parent);
    }

    public boolean setValue(boolean val) {
	// dummy
	return val;
    }

    public boolean getValue() {
	// dummy
	return false;
    }
}
