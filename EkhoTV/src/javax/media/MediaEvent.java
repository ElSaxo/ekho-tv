/*
 * @(#)MediaEvent.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * <code>MediaEvent</code> is the base interface for events supported by the
 * media framework.
 * <p>
 * <h2>Java Beans support</h2>
 * 
 * In order to support the Java Beans event model an implementation of
 * MediaEvent is required to sub-class java.util.EventObject. If an
 * implementation is designed to support the 1.0.2 JDK then it may alternatively
 * sub-class sunw.util.EventObject to provide the support appropriate support.
 * 
 * <b>Any class that subclasses <code>MediaEvent</code> must resolve to either
 * java.util.EventObject or sunw.util.EventObject.
 * 
 * @see ControllerEvent
 * @see GainChangeEvent
 * 
 **/
public interface MediaEvent {

    public Object getSource();

}
