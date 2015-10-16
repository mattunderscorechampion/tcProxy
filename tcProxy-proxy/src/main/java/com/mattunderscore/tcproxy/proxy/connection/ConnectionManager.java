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

package com.mattunderscore.tcproxy.proxy.connection;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Manager for connections.
 * @author Matt Champion on 22/02/14.
 */
public final class ConnectionManager {
    private final Set<Connection> connections = new CopyOnWriteArraySet<>();
    private final Set<Listener> listeners = new CopyOnWriteArraySet<>();

    public void register(final Connection connection) {
        connections.add(connection);
        for (final Listener listener : listeners) {
            listener.newConnection(connection);
        }
    }

    public void unregister(final Connection connection) {
        connections.remove(connection);
        for (final Listener listener : listeners) {
            listener.closedConnection(connection);
        }
    }

    public Set<Connection> getConnections() {
        return connections;
    }

    public void addListener(final Listener listener) {
        listeners.add(listener);
    }

    public interface Listener {
        void newConnection(Connection connection);
        void closedConnection(Connection connection);
    }
}
