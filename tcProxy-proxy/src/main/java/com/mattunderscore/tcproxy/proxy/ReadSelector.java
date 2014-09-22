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

import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOSelector;
import com.mattunderscore.tcproxy.proxy.action.Close;
import com.mattunderscore.tcproxy.proxy.action.Write;
import com.mattunderscore.tcproxy.proxy.action.queue.ActionQueue;
import com.mattunderscore.tcproxy.proxy.settings.ReadSelectorSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * The read selector for the proxy.
 * @author Matt Champion on 18/02/14.
 */
public final class ReadSelector implements Runnable {
    public static final Logger LOG = LoggerFactory.getLogger("reader");
    private volatile boolean running = false;
    private final IOSelector selector;
    private final ReadSelectorSettings settings;
    private final BlockingQueue<Connection> newConnections;

    public ReadSelector(final IOSelector selector, final ReadSelectorSettings settings, final BlockingQueue<Connection> newConnections) {
        this.selector = selector;
        this.settings = settings;
        this.newConnections = newConnections;
    }

    public void stop() {
        running = false;
        LOG.debug("{} : Stopping", this);
    }

    @Override
    public void run() {
        LOG.debug("{} : Starting", this);
        final ByteBuffer buffer = ByteBuffer.allocate(settings.getReadBufferSize());
        running = true;
        while (running) {
            try {
                selector.selectNow();
            }
            catch (final IOException e) {
                LOG.debug("{} : Error selecting", this, e);
            }

            registerKeys();

            readBytes(buffer);
        }
    }

    void registerKeys() {
        final Set<Connection> connections = new HashSet<>();
        newConnections.drainTo(connections);
        for (final Connection connection : connections) {
            try {
                final Direction cTs = connection.clientToServer();
                final DirectionAndConnection dc0 = new DirectionAndConnection(cTs, connection);
                final IOSocketChannel channel0 = cTs.getFrom();
                channel0.register(selector, IOSelectionKey.Op.READ, dc0);

                final Direction sTc = connection.serverToClient();
                final DirectionAndConnection dc1 = new DirectionAndConnection(sTc, connection);
                final IOSocketChannel channel1 = sTc.getFrom();
                channel1.register(selector, IOSelectionKey.Op.READ, dc1);
            }
            catch (final IOException e) {
                LOG.debug("{} : Error registering", this, e);
            }
        }
    }

    void readBytes(final ByteBuffer buffer) {
        final Set<IOSelectionKey> selectionKeys = selector.selectedKeys();
        for (final IOSelectionKey key : selectionKeys) {
            if (key.isValid() && key.isReadable()) {
                final DirectionAndConnection dc = (DirectionAndConnection)key.attachment();
                final Direction direction = dc.getDirection();
                final ActionQueue queue = direction.getQueue();
                if (!queue.queueFull()) {
                    buffer.position(0);
                    final ByteChannel channel = direction.getFrom();
                    try {
                        final int bytes = direction.read(buffer);
                        if (bytes > 0) {
                            buffer.flip();
                            final ByteBuffer writeBuffer = ByteBuffer.allocate(buffer.limit());
                            writeBuffer.put(buffer);
                            writeBuffer.flip();

                            informOfData(direction, writeBuffer);
                        }
                        else if (bytes == -1) {
                            key.cancel();
                            informOfClose(direction);
                            final ConnectionImpl conn = (ConnectionImpl) dc.getConnection();
                            final Direction otherDirection = conn.otherDirection(direction);
                            LOG.debug("{} : Closed {} ", this, otherDirection);
                            otherDirection.close();
                        }
                    }
                    catch (final ClosedChannelException e) {
                        LOG.debug("{} : Channel {} already closed", this, channel);
                        key.cancel();
                    }
                    catch (final IOException e) {
                        LOG.debug("{} : Error on channel {}, {}", this, channel, key, e);
                    }
                }
            }
        }
    }

    void informOfData(final Direction direction, final ByteBuffer write) {
        direction.getProcessor().process(new Write(direction, write));
    }

    void informOfClose(final Direction direction) {
        direction.getProcessor().process(new Close(direction));
    }

    @Override
    public String toString() {
        return "Read Selector";
    }
}
