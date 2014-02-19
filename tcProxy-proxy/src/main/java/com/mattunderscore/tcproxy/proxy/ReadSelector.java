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
import java.util.Queue;
import java.util.Set;

/**
 * @author matt on 18/02/14.
 */
public class ReadSelector implements Runnable {
    private volatile boolean running = false;
    private final Selector selector;
    private final Queue<Connection> newConnections;
    private Queue<ConnectionWrites> newWrites;

    public ReadSelector(final Selector selector, final Queue<Connection> newConnections, final Queue<ConnectionWrites> newWrites) {
        this.selector = selector;
        this.newConnections = newConnections;
        this.newWrites = newWrites;
    }

    @Override
    public void run() {
        final ByteBuffer buffer = ByteBuffer.allocate(4 * 1024);
        running = true;
        while (running) {
            registerKeys();

            readBytes(buffer);
        }
    }

    private void registerKeys() {
        while (!newConnections.isEmpty()) {
            try {
                final Connection connection = newConnections.poll();
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
            selector.select(100);
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
                        //System.out.println("Read " + bytes + " bytes from " + channel);
                        buffer.flip();
                        final ByteBuffer writeBuffer = ByteBuffer.allocate(buffer.limit());
                        writeBuffer.put(buffer);
                        writeBuffer.flip();
                        if (!writes.hasData()) {
                            //System.out.println("New write");
                            newWrites.add(writes);
                        }
                        writes.add(writeBuffer);
                    }
                    else if (bytes == -1) {
                        //System.out.println("EOF reached, cancelling key");
                        key.cancel();
                        //writes.getConnection().close();
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
