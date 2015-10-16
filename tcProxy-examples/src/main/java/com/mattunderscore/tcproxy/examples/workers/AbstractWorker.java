package com.mattunderscore.tcproxy.examples.workers;

import java.io.IOException;
import java.util.Objects;

/**
 * Abstract worker. Common implementation of example worker threads.
 * @author Matt Champion on 09/10/2015
 */
public abstract class AbstractWorker {
    private final String name;
    private volatile boolean running = false;

    /**
     * Constructor.
     */
    public AbstractWorker(String name) {
        Objects.requireNonNull(name);
        this.name = name;
    }

    /**
     * Start the worker thread.
     */
    public final void start() {
        final Thread thread = new Thread(new InternalRunnable());
        thread.setName(name);
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                running = false;
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
    public final boolean isRunning() {
        return running;
    }

    /**
     * @return The name of the worker
     */
    public final String getName() {
        return name;
    }

    /**
     * Performs the worker task.
     * @throws IOException An I/O exception, if thrown the worker will stop
     */
    public abstract void doWork() throws IOException;

    private final class InternalRunnable implements Runnable {
        @Override
        public void run() {
            running = true;
            while (running) {
                try {
                    doWork();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    running = false;
                }
            }
        }
    }
}
