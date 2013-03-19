/*
 * @(#)DataStarvedEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * <CODE>DataStarvedEvent</CODE> indicates that a <code>Controller</code> has
 * lost data or has stopped receiving data altogether. This transitions the
 * <CODE>Controller</CODE> into a <i>Stopped</i> state.
 * 
 * @see Controller
 * @see ControllerListener
 * 
 * @version 1.19, 98/03/28
 * 
 */
public class DataStarvedEvent extends StopEvent {

    public DataStarvedEvent(Controller from, int previous, int current,
	    int target, Time mediaTime) {
	super(from, previous, current, target, mediaTime);
    }

}
