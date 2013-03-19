/*
 * @(#)XletRunnable.java	1.7 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv;


/* Request class:
 * 
 * This class represents the Thread to perform an Xlet lifecycle
 * action.  It tries to pick an action request from a request Holder
 * and put a result back to the result holder after executing this request.
 *
 * It can be stopped by changing its "shouldRun" variable. 
 */
public class XletRunnable implements Runnable {

    Holder reqHolder, resultHolder;

    // To control the termination of this thread
    public boolean shouldRun = true;

    XletRunnable(Holder reqHolder, Holder resultHolder) {
	if (reqHolder != null && resultHolder != null) {
	    this.reqHolder = reqHolder;
	    this.resultHolder = resultHolder;
	} else {
	    throw new IllegalArgumentException(
		    "Req/result holders shouldnot be null");
	}
    }

    public void run() {
	while (shouldRun) {
	    Request req = (Request) reqHolder.get();
	    Result result = req.execReq();
	    // resultHolder.put(result); // TOm Nevin
	}
    }
}
