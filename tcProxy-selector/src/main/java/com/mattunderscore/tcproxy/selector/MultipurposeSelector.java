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

package com.mattunderscore.tcproxy.selector;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;

import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOSelector;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketChannel;

/**
 * A multipurpose selector.
 * @author Matt Champion on 24/10/2015
 */
public final class MultipurposeSelector implements Runnable {
    private final AtomicReference<State> state = new AtomicReference<>(State.STOPPED);
    private final BlockingQueue<Registration> registrations = new ArrayBlockingQueue<>(64);
    private final Logger log;
    private final IOSelector selector;
    private volatile CountDownLatch readyLatch = new CountDownLatch(1);
    private volatile CountDownLatch stoppedLatch;

    public MultipurposeSelector(Logger log, IOSelector selector) {
        this.log = log;
        this.selector = selector;
    }

    @Override
    public final void run() {
        resetStartup();

        while (state.get() == State.RUNNING) {
            try {
                selector.selectNow();
            }
            catch (final IOException e) {
                log.debug("{} : Error selecting keys", this, e);
            }

            final Collection<Registration> newRegistrations = new HashSet<>();
            registrations.drainTo(newRegistrations);
            for (final Registration registration : newRegistrations) {
                try {
                    registration.register(selector);
                }
                catch (ClosedChannelException e) {
                    log.debug("{} : Problem registering key", this, e);
                }
            }

            final Iterator<IOSelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                final IOSelectionKey key = selectedKeys.next();
                selectedKeys.remove();
                final Registration registration = (Registration) key.attachment();
                registration.run(key);
            }
        }

        resetShutdown();
    }

    private void resetStartup() {
        if (state.compareAndSet(State.STOPPED, State.RUNNING)) {
            stoppedLatch = new CountDownLatch(1);
            readyLatch.countDown();
            log.debug("{} : Started", this);
        }
        else {
            throw new IllegalStateException("The selector is already running");
        }
    }

    private void resetShutdown() {
        log.debug("{} : Stopped", this);
        stoppedLatch.countDown();
        readyLatch = new CountDownLatch(1);
        state.set(State.STOPPED);
    }

    /**
     * Stop the selector.
     */
    public void stop() {
        if (state.compareAndSet(State.RUNNING, State.STOPPING)) {
            log.debug("{} : Stopping", this);
        }
        else {
            throw new IllegalStateException("The selector is not running");
        }
    }

    /**
     * Block until the selector has started. Will return immediately after starting.
     * @throws InterruptedException
     */
    public void waitForRunning() throws InterruptedException {
        readyLatch.await();
    }

    /**
     * Block until the selector has stopped. Will return immediately before starting.
     * @throws InterruptedException
     */
    public void waitForStopped() throws InterruptedException {
        final CountDownLatch latch = this.stoppedLatch;
        if (latch != null) {
            latch.await();
        }
    }

    /**
     * Register operations with the selector.
     * @param channel The channel
     * @param op The operation
     * @param runnable The runnable
     */
    public void register(IOSocketChannel channel, IOSelectionKey.Op op, SelectorRunnable runnable) {
        registrations.add(new IOSocketChannelSingleRegistration(channel, op, runnable));
    }

    /**
     * Register operations with the selector.
     * @param channel The channel
     * @param ops The operations
     * @param runnable The runnable
     */
    public void register(IOSocketChannel channel, Set<IOSelectionKey.Op> ops, SelectorRunnable runnable) {
        registrations.add(new IOSocketChannelSetRegistration(channel, ops, runnable));
    }

    /**
     * Register accept with the selector.
     * @param channel The channel
     * @param runnable The runnable
     */
    public void register(IOServerSocketChannel channel, ServerSelectorRunnable runnable) {
        registrations.add(new IOServerSocketChannelRegistration(channel, runnable));
    }

    @Override
    public String toString() {
        return "Multipurpose selector";
    }

    private enum State {
        STOPPED,
        RUNNING,
        STOPPING
    }

}
