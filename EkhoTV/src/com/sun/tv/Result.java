/*
 * @(#)Result.java	1.5 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv;

import javax.tv.xlet.XletStateChangeException;

/* Result class:
 * 
 * This class represents the result from performing an Xlet lifecycle
 * action.  It's constructed after the action and put in a Holder for
 * the XletManager thread to pick up.  The result is basically either
 * successful or a XletStateChangeException is thrown -- unsuccessful.  
 *
 */
class Result extends Object {

    private boolean success;
    private XletStateChangeException sce;

    Result() {
	this.success = true;
    }

    Result(XletStateChangeException sce) {
	this.sce = sce;
	this.success = false;
    }

    public boolean getSuccess() {
	return success;
    }

    public XletStateChangeException getException() {
	return sce;
    }
}
