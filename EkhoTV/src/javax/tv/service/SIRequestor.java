/*
 * @(#)SIRequestor.java	1.19 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.tv.service;

/**
 * This interface is implemented by application classes to receive the results
 * of asynchronous SI retrieval requests. The <code>SIRequestor</code> registers
 * itself at the time of the asynchronous call for a single request and is
 * automatically unregistered when the request is completed. Applications can
 * disambiguate retrieval operations by registering a unique
 * <code>SIRequestor</code> for each retrieval request.
 * <p>
 * 
 * The asynchronous SI retrieval mechanisms invoke the methods of this interface
 * using system threads that are guaranteed to not hold locks on application
 * objects.
 * */
public interface SIRequestor {

    /**
     * Notifies the <code>SIRequestor</code> of successful asynchronous SI
     * retrieval.
     * 
     * @param result
     *            The previously requested data.
     */
    public void notifySuccess(SIRetrievable[] result);

    /**
     * Notifies the <code>SIRequestor</code> of unsuccessful asynchronous SI
     * retrieval.
     * 
     * @param reason
     *            The reason why the asynchronous request failed.
     */
    public void notifyFailure(SIRequestFailureType reason);
}
