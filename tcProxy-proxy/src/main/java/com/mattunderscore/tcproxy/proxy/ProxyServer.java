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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.impl.CircularBufferImpl;
import com.mattunderscore.tcproxy.proxy.connection.Connection;
import com.mattunderscore.tcproxy.proxy.connection.ConnectionManager;
import com.mattunderscore.tcproxy.proxy.selector.AcceptorTask;
import com.mattunderscore.tcproxy.proxy.selector.ProxyConnectionHandlerFactory;
import com.mattunderscore.tcproxy.proxy.selector.ReadTask;
import com.mattunderscore.tcproxy.proxy.selector.WriteTask;
import com.mattunderscore.tcproxy.proxy.settings.ConnectionSettings;
import com.mattunderscore.tcproxy.proxy.settings.OutboundSocketSettings;
import com.mattunderscore.tcproxy.proxy.settings.ReadSelectorSettings;
import com.mattunderscore.tcproxy.selector.connecting.ConnectionHandlerFactory;
import com.mattunderscore.tcproxy.selector.server.AcceptSettings;
import com.mattunderscore.tcproxy.selector.server.SocketSettings;

/**
 * The proxy.
 * @author Matt Champion on 18/02/14.
 */
public final class ProxyServer {
    private static final Logger LOG = LoggerFactory.getLogger("proxy");
    private final AcceptorTask acceptor;
    private final WriteTask writer;
    private final ReadTask proxy;

    public ProxyServer(
        final AcceptSettings acceptorSettings,
        final ConnectionSettings connectionSettings,
        final SocketSettings inboundSocketSettings,
        final OutboundSocketSettings outboundSocketSettings,
        final ReadSelectorSettings readSelectorSettings,
        final ConnectionManager manager) throws IOException {

        final OutboundSocketFactory socketFactory = new OutboundSocketFactory(outboundSocketSettings);

        writer = new WriteTask();
        final ConnectionHandlerFactory connectionHandlerFactory = new ProxyConnectionHandlerFactory(
            socketFactory,
            connectionSettings,
            manager,
            writer);
        acceptor = new AcceptorTask(acceptorSettings, inboundSocketSettings, connectionHandlerFactory);
        proxy = new ReadTask(CircularBufferImpl.allocateDirect(readSelectorSettings.getReadBufferSize()));

        manager.addListener(new ConnectionManager.Listener() {
            @Override
            public void newConnection(final Connection connection) {
                proxy.queueNewConnection(connection);
            }

            @Override
            public void closedConnection(final Connection connection) {
            }
        });
    }

    public void start() {
        final Thread acceptorThread = new Thread(acceptor);
        final Thread readerThread = new Thread(proxy);
        final Thread writerThread = new Thread(writer);

        acceptorThread.setName("tcProxy - Acceptor Thread");
        readerThread.setName("tcProxy - Reader Thread");
        writerThread.setName("tcProxy - Writer Thread");

        acceptorThread.setDaemon(false);
        readerThread.setDaemon(false);
        writerThread.setDaemon(false);

        final ExceptionHandler handler = new ExceptionHandler();
        acceptorThread.setUncaughtExceptionHandler(handler);
        readerThread.setUncaughtExceptionHandler(handler);
        writerThread.setUncaughtExceptionHandler(handler);

        acceptorThread.start();
        readerThread.start();
        writerThread.start();

        try {
            acceptor.waitForReady();
            proxy.waitForReady();
            writer.waitForReady();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        writer.stop();
        proxy.stop();
        acceptor.stop();
    }

    private final class ExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            LOG.error("Uncaught exception in thread '{}'", t, e);
            stop();
        }
    }
}
