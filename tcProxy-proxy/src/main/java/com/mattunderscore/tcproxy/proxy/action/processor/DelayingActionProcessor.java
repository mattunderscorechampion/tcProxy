/* Copyright © 2014 Matthew Champion
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

package com.mattunderscore.tcproxy.proxy.action.processor;

import com.mattunderscore.tcproxy.proxy.action.Action;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ActionProcessor that delays calling the next processor in the chain.
 * @author Matt Champion on 22/03/14.
 */
public final class DelayingActionProcessor implements ActionProcessor {
    private final ActionProcessor processor;
    private final ScheduledExecutorService executorService;
    private final long delay;
    private final TimeUnit delayUnits;

    /**
     *
     * @param processor The next processor in the chain.
     * @param executorService The executor that is used to call the next action processor after the delay
     * @param delay The magnitude of the delay
     * @param delayUnits The unit of the delay
     */
    public DelayingActionProcessor(ActionProcessor processor, ScheduledExecutorService executorService, long delay, TimeUnit delayUnits) {
        this.processor = processor;
        this.executorService = executorService;
        this.delay = delay;
        this.delayUnits = delayUnits;
    }

    @Override
    public void process(final Action action) {
        executorService.schedule(new DelayedTask(action), delay, delayUnits);
    }

    @Override
    public void flush() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(delay * 2, delayUnits);
    }

    /**
     * Task for calling the next processor.
     */
    private final class DelayedTask implements Runnable {
        private final Action action;

        public DelayedTask(final Action action) {
            this.action = action;
        }

        @Override
        public void run() {
            processor.process(action);
        }
    }
}
