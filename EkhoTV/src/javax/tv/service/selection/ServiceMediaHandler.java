/*
 * @(#)ServiceMediaHandler.java	1.13 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.selection;

import javax.media.Player;

/**
 * <code>ServiceMediaHandler</code> represents an handler of service components
 * that are real time media sharing the same clock. A
 * <code>ServiceMediaHandler</code> is associated with the <code>Service</code>
 * currently selected in the <code>ServiceContext</code> from which it was
 * obtained.
 * 
 * @see ServiceContext
 * @see javax.tv.media.MediaSelectControl
 */
public interface ServiceMediaHandler extends Player, ServiceContentHandler {
}
