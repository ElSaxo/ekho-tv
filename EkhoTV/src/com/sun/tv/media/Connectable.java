/*
 * @(#)Connectable.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package com.sun.tv.media;

/**
 * <b>Tentative</b><br>
 * <i> This interface is currently under development and is not yet recommended
 * for use. It is included here as an indication of direction and solicitation
 * for comment. </i>
 * <p>
 * 
 * Defines common interface to input and output Connectables.
 * 
 * @see MediaProcessor
 * @see InputConnectable
 * @see OutputConnectable
 * @see Format
 * 
 * @version 1.30, 98/03/28
 */
public interface Connectable {

    /**
     * <b>Tentative.</b><br>
     * The formats supported by this Connectable. Connectables must support at
     * least one format.
     * 
     * @ return the list of supported formats.
     */
    public Format[] listFormats();

    /**
     * <b>Tentative.</b><br>
     * Selects a format for this Connectable (the default is null). The only
     * reason <b>setFormat()</b> should fail is if the specified format is not
     * in the list of formats returned by <b>listFormats()</b>. The
     * <b>setFormat()</b> method is typically called by the Manager as part of
     * the Manager.connect() method call.
     * 
     */
    public void setFormat(Format format);

    /**
     * <b>Tentative.</b><br>
     * The selected format. If <b>setFormat()</b> has not been called,
     * <b>getFormat()</b> will return null.
     * 
     * @return the currently selected format.
     */
    public Format getFormat();

    /**
     * <b>Tentative.</b><br>
     * MediaProcessor for this Connectable.
     * 
     * return the processor this connectable came from.
     */
    public MediaProcessor getMediaProcessor();
}
