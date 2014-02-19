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
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author matt on 18/02/14.
 */
public class Connection {
    private final SocketChannel clientSide;
    private final SocketChannel serverSide;
    private final Queue<ByteBuffer> writesToClient;
    private final Queue<ByteBuffer> writesToServer;
    private final Direction clientToServer;
    private final Direction serverToClient;
    private final ConnectionWrites cwToClient;
    private final ConnectionWrites cwToServer;

    public Connection(final SocketChannel clientSide, final SocketChannel serverSide) {
        this.clientSide = clientSide;
        this.serverSide = serverSide;
        writesToClient = new LinkedBlockingQueue<>();
        writesToServer = new LinkedBlockingQueue<>();
        clientToServer = new ClientToServer();
        serverToClient = new ServerToClient();
        cwToClient = new ClientWrites();
        cwToServer = new ServerWrites();
    }

    public void close() {
        try {
            clientSide.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        try {
            serverSide.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Direction clientToServer() {
        return clientToServer;

    }

    public Direction serverToClient() {
        return serverToClient;
    }

    private final class ClientToServer implements Direction {
        public ClientToServer() {
        }

        @Override
        public SocketChannel getFrom() {
            return clientSide;
        }

        @Override
        public SocketChannel getTo() {
            return serverSide;
        }

        @Override
        public Connection getConnection() {
            return Connection.this;
        }

        @Override
        public ConnectionWrites getWrites() {
            return cwToServer;
        }

    }

    private final class ServerToClient implements Direction {
        public ServerToClient() {
        }

        @Override
        public SocketChannel getFrom() {
            return serverSide;
        }

        @Override
        public SocketChannel getTo() {
            return clientSide;
        }

        @Override
        public Connection getConnection() {
            return Connection.this;
        }

        @Override
        public ConnectionWrites getWrites() {
            return cwToClient;
        }
    }

    private final class ServerWrites implements ConnectionWrites {
        public SocketChannel getTarget() {
            return serverSide;
        }

        public ByteBuffer current() {
            final ByteBuffer buffer = writesToServer.peek();
            if (buffer == null) {
                return null;
            }
            else if (buffer.remaining() > 0) {
                return buffer;
            }
            else {
                writesToServer.poll();
                return current();
            }
        }

        public void add(final ByteBuffer data) {
            writesToServer.add(data);
        }

        public boolean hasData() {
            return !writesToServer.isEmpty();
        }

        public Connection getConnection() {
            return Connection.this;
        }
    }

    private final class ClientWrites implements ConnectionWrites {
        public SocketChannel getTarget() {
            return serverSide;
        }

        public ByteBuffer current() {
            final ByteBuffer buffer = writesToClient.peek();
            if (buffer == null) {
                return null;
            }
            else if (buffer.remaining() > 0) {
                return buffer;
            }
            else {
                writesToClient.poll();
                return current();
            }
        }

        public void add(final ByteBuffer data) {
            writesToClient.add(data);
        }

        public boolean hasData() {
            return !writesToClient.isEmpty();
        }

        public Connection getConnection() {
            return Connection.this;
        }
    }
}
