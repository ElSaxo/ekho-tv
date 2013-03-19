/*
 * @(#)Controls.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media.protocol;

/**
 * 
 * <code>Controls</code> provides an interface for obtaining objects by
 * interface or class name. This is useful in the case where support for a
 * particular interface cannot be determined at runtime, or where a different
 * object is required to implement the behavior. The <code>object</code>
 * returned from <code>getControl</code> is assumed to control the
 * <code>object</code> that <code>getControl</code> was invoked on.
 **/
public interface Controls {

    /**
     * Obtain the collection of objects that control the object that implements
     * this interface.
     * <p>
     * 
     * If no controls are supported, a zero length array is returned.
     * 
     * @return the collection of object controls
     */
    public Object[] getControls();

    /**
     * Obtain the object that implements the specified <code>Class</code> or
     * <code>Interface</code> The full class or interface name must be used.
     * <p>
     * 
     * If the control is not supported then <code>null</code> is returned.
     * 
     * @return the object that implements the control, or <code>null</code>.
     */
    public Object getControl(String controlType);

}
