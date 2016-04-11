/* Copyright © 2016 Matthew Champion
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

package com.mattunderscore.tcproxy.workers;

/**
 * A simple {@link Worker} that delegates work to a {@link WorkerRunnable} and manages the state of the worker itself.
 * @author Matt Champion on 11/04/2016
 */
public final class SimpleWorker implements Worker {
    private final LifecycleState lifecycleState = new LifecycleState();
    private final WorkerRunnable task;

    /**
     * Constructor.
     * @param task A runnable that will be adapted to a {@link WorkerRunnable}
     */
    public SimpleWorker(Runnable task) {
        this.task = new RunnableAdapter(task);
    }

    /**
     * Constructor.
     * @param task A worker runnable
     */
    public SimpleWorker(WorkerRunnable task) {
        this.task = task;
    }

    @Override
    public void start() {
        lifecycleState.beginStartup();

        task.onStart();

        while (lifecycleState.isRunning()) {
            task.run();
        }

        task.onStop();

        lifecycleState.endShutdown();
    }

    @Override
    public void stop() {
        lifecycleState.beginShutdown();
    }

    @Override
    public void waitForRunning() {
        lifecycleState.waitForRunning();
    }

    @Override
    public void waitForStopped() {
        lifecycleState.waitForStopped();
    }

    private static final class RunnableAdapter implements WorkerRunnable {
        private final Runnable task;

        public RunnableAdapter(Runnable task) {
            this.task = task;
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onStop() {
        }

        @Override
        public void run() {
            task.run();
        }
    }
}
