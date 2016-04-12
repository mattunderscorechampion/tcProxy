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

package com.mattunderscore.tcproxy.workers;

import java.util.concurrent.ThreadFactory;

/**
 * This worker can be started and stopped. A new {@link Thread} is created every time this worker is started.
 * @author Matt Champion on 10/11/2015
 */
public final class WorkerThread implements RestartableWorker {
    private final LifecycleState state = new LifecycleState();
    private final ThreadFactory threadFactory;
    private final InnerTask innerTask;

    public WorkerThread(ThreadFactory threadFactory, Worker task) {
        this.threadFactory = threadFactory;
        this.innerTask = new InnerTask(state, task);
    }

    @Override
    public void start() {
        state.beginStartup();

        threadFactory.newThread(innerTask).start();
    }

    @Override
    public void stop() {
        state.beginShutdown();
        innerTask.stop();
    }

    @Override
    public void restart() {
        stop();
        waitForStopped();
        start();
    }

    @Override
    public void waitForRunning() {
        state.waitForRunning();
    }

    @Override
    public void waitForStopped() {
        state.waitForStopped();
    }

    private static final class InnerTask implements Runnable {
        private final LifecycleState state;
        private final Worker task;

        public InnerTask(LifecycleState state, Worker task) {
            this.state = state;
            this.task = task;
        }

        @Override
        public void run() {
            try {
                task.start();
            }
            finally {
                state.endShutdown();
            }
        }

        public void stop() {
            task.stop();
        }
    }
}
