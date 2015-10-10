package com.mattunderscore.tcproxy.examples.workers;

import java.io.IOException;

/**
 * Abstract worker.
 * @author Matt Champion on 09/10/2015
 */
public abstract class AbstractWorker {
    private volatile boolean running = false;

    /**
     * Constructor.
     */
    public AbstractWorker() {
    }

    /**
     * Start the worker thread.
     */
    public final void start() {
        final Thread thread = new Thread(new InternalRunnable());
        thread.setName(getName());
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }

    /**
     * Stop the worker thread
     */
    public final void stop() {
        running = false;
    }

    /**
     * @return If the tread is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * @return The name of the worker
     */
    public abstract String getName();

    /**
     * @return The work being done
     */
    public abstract WorkerRunnable getTask();

    private final class InternalRunnable implements Runnable {
        @Override
        public void run() {
            final WorkerRunnable runnable = getTask();
            running = true;
            while (running) {
                try {
                    runnable.run();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    running = false;
                }
            }
        }
    }

    /**
     * Worker runnable.
     */
    public interface WorkerRunnable {
        /**
         * Action to take.
         * @throws IOException
         */
        void run() throws IOException;
    }
}
