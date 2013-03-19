/*
 * @(#)ConnectionErrorEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * A <CODE>ConnectionErrorEvent</CODE> is posted when an error occurs within a
 * <CODE>DataSource</CODE> when obtaining data or communicating with a server.
 **/

public class ConnectionErrorEvent extends ControllerErrorEvent {

    public ConnectionErrorEvent(Controller from) {
	super(from);
    }

    public ConnectionErrorEvent(Controller from, String why) {
	super(from, why);
    }

}
