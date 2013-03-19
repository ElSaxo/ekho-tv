/*
 * @(#)RateConfiguration.java	1.3 08/09/15
 * 
 * Copyright � 2008 Sun Microsystems, Inc. All rights reserved. 
 * Use is subject to license terms.
 * 
 */

package javax.media.protocol;

/**
 * A configuration of streams for a particular rate.
 * 
 * @see DataSource
 * @see RateConfigureable
 * @version 1.9, 98/03/28.
 */

public interface RateConfiguration {

    /**
     * Get the <CODE>RateRange</CODE> for this configuration.
     * 
     * @return The rate supported by this configuration.
     */
    public RateRange getRate();

    /**
     * Get the streams that will have content at this rate.
     * 
     * @return The streams supported at this rate.
     */
    public SourceStream[] getStreams();
}
