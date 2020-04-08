package com.nedap.university.eline.exchanger.shared;

/**
	 * Interface for timeout event handlers.
	 * 
	 * @author Jaco ter Braak, University of Twente.
	 * @version 11-01-2014
	 */

public interface TimeoutEventHandlerInterface {
    /**
     * Is triggered when the timeout has elapsed
     */
    void TimeoutElapsed(Object tag);
}
