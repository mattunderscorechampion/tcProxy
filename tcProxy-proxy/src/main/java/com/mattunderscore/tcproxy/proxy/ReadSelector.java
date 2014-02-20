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
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * @author matt on 18/02/14.
 */
public class ReadSelector implements Runnable {
    private volatile boolean running = false;
    private final Selector selector;
    private final BlockingQueue<Connection> newConnections;
    private final BlockingQueue<ConnectionWrites> newWrites;

    public ReadSelector(final Selector selector, final BlockingQueue<Connection> newConnections, final BlockingQueue<ConnectionWrites> newWrites) {
        this.selector = selector;
        this.newConnections = newConnections;
        this.newWrites = newWrites;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        final ByteBuffer buffer = ByteBuffer.allocate(1024);
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
                //System.out.println("Register new connection");

                final Direction cTs = connection.clientToServer();
                final SocketChannel channel0 = cTs.getFrom();
                final SelectionKey key0 = channel0.register(selector, SelectionKey.OP_READ);
                key0.attach(cTs.getWrites());

                final Direction sTc = connection.serverToClient();
                final SocketChannel channel1 = sTc.getFrom();
                final SelectionKey key1 = channel1.register(selector, SelectionKey.OP_READ);
                key1.attach(sTc.getWrites());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readBytes(final ByteBuffer buffer) {
        try {
            final Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (final SelectionKey key : selectionKeys) {
                if (!key.isValid()) {
                    key.cancel();
                }
                else if (key.isReadable()) {
                    buffer.position(0);
                    final ConnectionWrites writes = (ConnectionWrites)key.attachment();
                    final SocketChannel channel = (SocketChannel)key.channel();
                    final int bytes = channel.read(buffer);
                    if (bytes > 0) {
                        buffer.flip();
                        final ByteBuffer writeBuffer = ByteBuffer.allocate(buffer.limit());
                        writeBuffer.put(buffer);
                        writeBuffer.flip();

                        informOfData(writes, writeBuffer);
                    }
                    else if (bytes == -1) {
                        key.cancel();
                        informOfClose(writes);
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void informOfData(final ConnectionWrites writes, final ByteBuffer write) {
        informOfWrite(writes, new WriteImpl(writes.getTarget(), write));
    }

    private void informOfClose(final ConnectionWrites writes) {
        informOfWrite(writes, new CloseImpl(writes.getTarget()));
    }

    private void informOfWrite(final ConnectionWrites writes, final Write write) {
        if (!writes.hasData()) {
            writes.add(write);
            newWrites.add(writes);
        }
        else {
            writes.add(write);
        }
    }
}
