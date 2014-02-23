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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * @author matt on 19/02/14.
 */
public class WriteSelector implements Runnable {
    public static final Logger LOG = LoggerFactory.getLogger("writer");
    private final Selector selector;
    private final BlockingQueue<ActionQueue> newWrites;
    private volatile boolean running = false;

    public WriteSelector(final Selector selector, final BlockingQueue<ActionQueue> newWrites) {
        this.selector = selector;
        this.newWrites = newWrites;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                selector.selectNow();
            } catch (IOException e) {
                LOG.debug("Error selecting", e);
            }

            registerKeys();

            writeBytes();
        }
    }

    public void stop() {
        running = false;
    }

    private void registerKeys() {
        final Set<ActionQueue> writes = new HashSet<>();
        newWrites.drainTo(writes);
        for (final ActionQueue newWrite : writes)
        {
            try {
                newWrite.getDirection().getTo().register(selector, SelectionKey.OP_WRITE, newWrite);
            }
            catch (final ClosedChannelException e) {
                LOG.debug("Already closed");
            }
        }
    }

    private void writeBytes() {
        final Set<SelectionKey> keys = selector.selectedKeys();
        for (final SelectionKey key : keys) {
            if (key.isWritable()) {
                final ActionQueue write = (ActionQueue)key.attachment();
                final Action data = write.current();
                try {
                    if (data != null) {
                        data.writeToSocket();
                    }
                    else {
                        LOG.debug("Finished write");
                        key.cancel();
                        if (write.current() != null) {
                            LOG.debug("Add back");
                            newWrites.add(write);
                        }
                    }
                }
                catch (final IOException e) {
                    LOG.debug("Error writing", e);
                    key.cancel();
                }
            }
        }
    }
}
