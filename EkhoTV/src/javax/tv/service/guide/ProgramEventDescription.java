/*
 * @(#)ProgramEventDescription.java	1.8 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service.guide;

import javax.tv.service.SIRetrievable;

/**
 * This <code>SIElement</code> provides a textual description of a
 * <code>ProgramEvent</code>. In ATSC PSIP, this information is obtained from
 * the Extended Text Table; in DVB SI, from the Short Event Descriptor.)
 * */
public interface ProgramEventDescription extends SIRetrievable {

    /**
     * Provides a textual description of the <code>ProgramEvent</code>.
     * 
     * @return A textual description of the <code>ProgramEvent</code>, or an
     *         empty string if no description is available.
     */
    public String getProgramEventDescription();

}
