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

package com.mattunderscore.tcproxy.proxy;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;

import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOSelector;
import com.mattunderscore.tcproxy.io.IOSocketChannel;

/**
 * Abstract selector implementation.
 * @author Matt Champion on 24/09/14.
 */
public abstract class AbstractSelector implements Runnable {
    private final CountDownLatch readyLatch = new CountDownLatch(1);
    private final IOSelector selector;
    private final SelectorBackoff backoff;
    private final IOSelectionKey.Op operation;
    private volatile boolean running = false;

    protected AbstractSelector(IOSelectionKey.Op operation, IOSelector selector, SelectorBackoff backoff) {
        this.operation = operation;
        this.selector = selector;
        this.backoff = backoff;
    }

    @Override
    public final void run() {
        getLogger().debug("{} : Starting", this);
        running = true;
        readyLatch.countDown();
        while (running) {
            try {
                selector.selectNow();
            } catch (final IOException e) {
                getLogger().debug("{} : Error selecting keys", this, e);
            }

            registerKeys();

            final Set<IOSelectionKey> selectedKeys = selector.selectedKeys();
            if (selectedKeys.size() > 0) {
                processKeys(selectedKeys);
                backoff.selected(selectedKeys.size());
            }

            backoff.backoff();
        }
        getLogger().debug("{} : Stopped", this);
    }

    /**
     * Stop the selector.
     */
    public final void stop() {
        running = false;
        getLogger().debug("{} : Stopping", this);
    }

    void waitForReady() throws InterruptedException {
        readyLatch.await();
    }

    /**
     * Register operations with the selector.
     * @param channel The channel
     * @param attachment The attachment
     * @return The selection key
     * @throws ClosedChannelException
     */
    protected final IOSelectionKey register(IOSocketChannel channel, Object attachment) throws ClosedChannelException {
        return channel.register(selector, operation, attachment);
    }

    /**
     * Register any new keys.
     */
    protected abstract void registerKeys();

    /**
     * Process the selected keys.
     * @param selectedKeys The keys to process
     */
    protected abstract void processKeys(Set<IOSelectionKey> selectedKeys);

    /**
     * @return The logger to use in the selector
     */
    protected abstract Logger getLogger();
}
