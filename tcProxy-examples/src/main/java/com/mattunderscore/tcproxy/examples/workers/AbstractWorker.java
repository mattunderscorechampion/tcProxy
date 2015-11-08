/* Copyright © 2015 Matthew Champion
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
 * Neither the name of mattunderscore.com nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL MATTHEW CHAMPION BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

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
