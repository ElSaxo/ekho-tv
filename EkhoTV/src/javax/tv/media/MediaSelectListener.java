/*
 * @(#)MediaSelectListener.java	1.12 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.media;


/**
 * The <code>MediaSelectListener</code> interface is implemented by applications
 * in order to receive notification of selection operations on a
 * <code>MediaSelectControl</code>.
 */
public interface MediaSelectListener extends java.util.EventListener {

    /**
     * Notifies the <code>MediaSelectListener</code> that a selection has
     * completed.
     * 
     * @param event
     *            MediaSelectEvent describing the completion of a selection
     *            operation.
     **/
    public void selectionComplete(MediaSelectEvent event);

}
