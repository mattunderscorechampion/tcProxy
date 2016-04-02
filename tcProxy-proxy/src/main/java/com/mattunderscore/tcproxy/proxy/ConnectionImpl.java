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
import java.util.concurrent.atomic.AtomicBoolean;

import com.mattunderscore.tcproxy.io.selection.IOSelectionKey;
import com.mattunderscore.tcproxy.proxy.connection.Connection;
import com.mattunderscore.tcproxy.proxy.connection.ConnectionManager;
import com.mattunderscore.tcproxy.proxy.direction.Direction;
import com.mattunderscore.tcproxy.proxy.selector.WriteSelectionRunnable;
import com.mattunderscore.tcproxy.selector.SocketChannelSelector;

/**
 * Implementation of {@link Connection}.
 * @author Matt Champion on 18/02/14.
 */
public final class ConnectionImpl implements Connection {
    private final Direction clientToServer;
    private final Direction serverToClient;
    private final ConnectionManager manager;
    private final AtomicBoolean halfClosed;
    private final SocketChannelSelector selector;

    public ConnectionImpl(ConnectionManager manager, Direction clientToServer, Direction serverToClient, SocketChannelSelector selector) {
        this.manager = manager;
        this.clientToServer = clientToServer;
        this.serverToClient = serverToClient;
        halfClosed = new AtomicBoolean(false);
        this.selector = selector;

        final DirectionListener listener = new DirectionListener();
        clientToServer.addListener(listener);
        serverToClient.addListener(listener);
    }

    @Override
    public Direction clientToServer() {
        return clientToServer;
    }

    @Override
    public Direction serverToClient() {
        return serverToClient;
    }

    @Override
    public void close() throws IOException {
        clientToServer.close();
        serverToClient.close();
    }

    @Override
    public void needsWrite(Direction direction) {
        if (direction != clientToServer && direction != serverToClient) {
            throw new IllegalArgumentException("The direction is not valid for this connection");
        }

        selector.register(direction.getTo(), IOSelectionKey.Op.WRITE, new WriteSelectionRunnable(direction.getQueue(), this));
    }

    /**
     * @param direction A direction
     * @return The other direction of the connection
     */
    public Direction otherDirection(final Direction direction) {
        if (direction == clientToServer) {
            return serverToClient;
        }
        else if (direction == serverToClient) {
            return clientToServer;
        }
        else {
            throw new IllegalArgumentException("Unknown direction" + direction);
        }
    }

    @Override
    public String toString() {
        return String.format("c - s : %s, s - c %s",
                clientToServer,
                serverToClient);
    }

    /**
     * Listener for direction closes.
     */
    private final class DirectionListener implements Direction.Listener {
        @Override
        public void dataRead(Direction direction, int bytesRead) {
        }

        @Override
        public void dataWritten(Direction direction, int bytesWritten) {
        }

        @Override
        public void closed(Direction direction) {
            if (!halfClosed.compareAndSet(false, true)) {
                manager.unregister(ConnectionImpl.this);
            }
        }
    }
}
