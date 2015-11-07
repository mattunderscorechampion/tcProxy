package com.mattunderscore.tcproxy.selector;

/**
 * A task that can be repeated started and stopped.
 * @author Matt Champion on 06/11/2015
 */
public interface StartStopLifecycle extends Runnable {
    /**
     * Stop it.
     */
    void stop();

    /**
     * Block until it has started. Will return immediately after starting.
     * @throws InterruptedException
     */
    void waitForRunning() throws InterruptedException;

    /**
     * Block until it has stopped. Will return immediately before starting.
     * @throws InterruptedException
     */
    void waitForStopped() throws InterruptedException;
}
