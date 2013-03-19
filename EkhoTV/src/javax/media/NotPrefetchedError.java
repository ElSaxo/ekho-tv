/*
 * @(#)NotPrefetchedError.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * <code>NotPrefetchedError</code> is thrown when a method that requires a
 * <CODE>Controller</CODE> to be in the <I>Prefetched</I> state is called and
 * the <CODE>Controller</CODE> has not been <i>Prefetched</i>.
 * <p>
 * This typically happens when <code>syncStart</code> is invoked on a
 * <I>Stopped</I>&nbsp;<code>Controller</code> that hasn't been
 * <I>Prefetched</I>.
 * 
 * @see Controller
 * @version 1.14, 98/03/28.
 */

public class NotPrefetchedError extends MediaError {

    public NotPrefetchedError(String reason) {
	super(reason);
    }
}
