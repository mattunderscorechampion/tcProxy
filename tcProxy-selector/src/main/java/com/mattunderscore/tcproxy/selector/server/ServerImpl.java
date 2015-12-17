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

package com.mattunderscore.tcproxy.selector.server;

import java.io.IOException;
import java.util.Collection;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.socket.IOServerSocketChannel;
import com.mattunderscore.tcproxy.selector.threads.LifecycleState;
import com.mattunderscore.tcproxy.selector.threads.RestartableThreadSet;

/**
 * A basic server implementation. Relies on a {@link ServerStarter} to bind the sockets and create the selector threads.
 * The listening sockets are closed if the server fails to start and on shutdown.
 * @author Matt Champion on 09/11/2015
 */
@ThreadSafe
public final class ServerImpl implements Server {
    private static final Logger LOG = LoggerFactory.getLogger(ServerImpl.class);
    private final LifecycleState state = new LifecycleState();
    private final ServerStarter serverStarter;
    @GuardedBy("this")
    private RestartableThreadSet serverThreads;
    @GuardedBy("this")
    private Collection<IOServerSocketChannel> serverSockets;

    /**
     * Constructor.
     * @param serverStarter The starter for the server
     */
    public ServerImpl(ServerStarter serverStarter) {
        this.serverStarter = serverStarter;
    }

    @Override
    public synchronized void start() {
        state.beginStartup();

        try {
            serverSockets = serverStarter.bindServerSockets();
            serverThreads = serverStarter.createServerThreads(serverSockets, this);
        }
        catch (IOException e) {
            shutdownSockets();
            throw new IllegalStateException(e);
        }

        serverThreads.start();
    }

    @Override
    public synchronized void stop() {
        state.beginShutdown();

        serverThreads.stop();
        serverThreads.waitForStopped();
        serverThreads = null;
        shutdownSockets();

        state.endShutdown();
    }

    @Override
    public synchronized void restart() {
        state.beginShutdown();
        serverThreads.stop();
        state.endShutdown();
        state.beginStartup();
        serverThreads.start();
    }

    @Override
    public synchronized void waitForRunning() {
        state.waitForRunning();
        serverThreads.waitForRunning();
    }

    @Override
    public synchronized void waitForStopped() {
        state.waitForStopped();
    }

    private synchronized void shutdownSockets() {
        if (serverSockets != null) {
            for (final IOServerSocketChannel serverSocketChannel : serverSockets) {
                try {
                    serverSocketChannel.close();
                }
                catch (IOException e) {
                    LOG.warn("Failed to close server socket", e);
                }
            }
            serverSockets = null;
        }
    }
}
