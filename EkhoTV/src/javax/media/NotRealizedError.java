/*
 * @(#)NotRealizedError.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media;

/**
 * <code>NotRealizedError</code> is thrown when a method that requires a
 * <code>Controller</code> to be in the <i>Realized</i> state is called and the
 * <code>Controller</code> is not <i>Realized</i>.
 * <p>
 * 
 * For example, this can happen when <code>getComponents</code> is called on an
 * <i>Unrealized</i>&nbsp; <code>Player</code>.
 * 
 * @see Controller
 * @see Player
 * @version 1.10, 98/03/28.
 */

public class NotRealizedError extends MediaError {

    public NotRealizedError(String reason) {
	super(reason);
    }
}
