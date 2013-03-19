/*
 * @(#)Control.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

import android.view.View;

/**
 * The base interface for processing <CODE>Control</CODE> objects.
 * 
 * @version 1.15, 98/03/28
 */

public interface Control {

    /**
     * Get the <code>Component</code> associated with this <code>Control</code>
     * object. For example, this method might return a slider for volume control
     * or a panel containing radio buttons for CODEC control. The
     * <code>getControlComponent</code> method can return <CODE>null</CODE> if
     * there is no GUI control for this <code>Control</code>.
     */
    public View getControlComponent();
}
