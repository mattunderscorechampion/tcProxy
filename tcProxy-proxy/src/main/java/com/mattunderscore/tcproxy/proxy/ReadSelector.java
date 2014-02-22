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

import com.mattunderscore.tcproxy.proxy.com.mattunderscore.tcproxy.settings.ReadSelectorSettings;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * @author matt on 18/02/14.
 */
public class ReadSelector implements Runnable {
    private volatile boolean running = false;
    private final Selector selector;
    private final ReadSelectorSettings settings;
    private final BlockingQueue<Connection> newConnections;
    private final BlockingQueue<ActionQueue> newWrites;

    public ReadSelector(final Selector selector, final ReadSelectorSettings settings, final BlockingQueue<Connection> newConnections, final BlockingQueue<ActionQueue> newWrites) {
        this.selector = selector;
        this.settings = settings;
        this.newConnections = newConnections;
        this.newWrites = newWrites;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        final ByteBuffer buffer = ByteBuffer.allocate(settings.getReadBufferSize());
        running = true;
        while (running) {
            try {
                selector.selectNow();
            } catch (IOException e) {
                e.printStackTrace();
            }

            registerKeys();

            readBytes(buffer);
        }
    }

    private void registerKeys() {
        final Set<Connection> connections = new HashSet<>();
        newConnections.drainTo(connections);
        for (final Connection connection : connections) {
            try {
                final Direction cTs = connection.clientToServer();
                final SocketChannel channel0 = cTs.getFrom();
                channel0.register(selector, SelectionKey.OP_READ, cTs);

                final Direction sTc = connection.serverToClient();
                final SocketChannel channel1 = sTc.getFrom();
                channel1.register(selector, SelectionKey.OP_READ, sTc);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readBytes(final ByteBuffer buffer) {
        final Set<SelectionKey> selectionKeys = selector.selectedKeys();
        for (final SelectionKey key : selectionKeys) {
            if (key.isValid() && key.isReadable()) {
                final Direction direction = (Direction)key.attachment();
                final ActionQueue queue = direction.getQueue();
                if (!queue.queueFull()) {
                    buffer.position(0);
                    final SocketChannel channel = (SocketChannel)key.channel();
                    try {
                        final int bytes = direction.read(buffer);
                        if (bytes > 0) {
                            buffer.flip();
                            final ByteBuffer writeBuffer = ByteBuffer.allocate(buffer.limit());
                            writeBuffer.put(buffer);
                            writeBuffer.flip();

                            informOfData(queue, writeBuffer);
                        }
                        else if (bytes == -1) {
                            key.cancel();
                            informOfClose(queue);
                            System.out.println("Closed " + channel);
                            channel.close();
                        }
                    }
                    catch (final ClosedChannelException e) {
                        System.out.println("Channel already closed");
                        key.cancel();
                    }
                    catch (final IOException e) {
                        System.err.println("Error on channel " + channel + ", key " + key);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void informOfData(final ActionQueue writes, final ByteBuffer write) {
        informOfWrite(writes, new Write(writes.getDirection(), write));
    }

    private void informOfClose(final ActionQueue writes) {
        informOfWrite(writes, new Close(writes.getDirection()));
    }

    private void informOfWrite(final ActionQueue writes, final Action action) {
        if (!writes.hasData()) {
            writes.add(action);
            newWrites.add(writes);
        }
        else {
            writes.add(action);
        }
    }
}
