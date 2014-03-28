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

package com.mattunderscore.tcproxy;

import com.mattunderscore.tcproxy.proxy.Connection;
import com.mattunderscore.tcproxy.proxy.ConnectionManager;
import com.mattunderscore.tcproxy.proxy.Direction;
import com.mattunderscore.tcproxy.proxy.settings.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.LogManager;

/**
 * @author matt on 18/02/14.
 */
public final class ProxyServerMain {
    public static final Logger LOG = LoggerFactory.getLogger("cli");

    public static void main(final String[] args) throws IOException, InterruptedException {
        LogManager.getLogManager().readConfiguration(ProxyServerMain.class.getResourceAsStream("/logging.properties"));

        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            final ConnectionManager manager = new ConnectionManager();
            final com.mattunderscore.tcproxy.proxy.ProxyServer server = new com.mattunderscore.tcproxy.proxy.ProxyServer(
                    new AcceptorSettings(8085),
                    new ConnectionSettings(10000),
                    new InboundSocketSettings(8192, 8192),
                    new OutboundSocketSettings(8080, "localhost", 8192, 8192),
                    new ReadSelectorSettings(2048),
                    manager);

            manager.addListener(new ConnectionManager.Listener() {
                private final Map<Connection, Future<?>> tasks = new ConcurrentHashMap<>();
                @Override
                public void newConnection(final Connection connection) {
                    LOG.info("New connection");
                    tasks.put(connection, executor.scheduleAtFixedRate(new Runnable() {
                        @Override
                        public void run() {
                            final Direction clientToServer = connection.clientToServer();
                            final Direction serverToClient = connection.serverToClient();
                            LOG.info(String.format("Read %d bytes from %s", clientToServer.read(), clientToServer.getFrom()));
                            LOG.info(String.format("Wrote %d bytes to %s %d ops queued", clientToServer.written(), clientToServer.getTo(), clientToServer.getQueue().opsPending()));
                            LOG.info(String.format("Read %d bytes from %s", serverToClient.read(), serverToClient.getFrom()));
                            LOG.info(String.format("Wrote %d bytes to %s %d ops queued", serverToClient.written(), serverToClient.getTo(), serverToClient.getQueue().opsPending()));
                        }
                    }, 1, 5, TimeUnit.SECONDS));
                }

                @Override
                public void closedConnection(final Connection connection) {
                    final Future<?> task = tasks.get(connection);
                    task.cancel(true);
                    LOG.info("Connection closed");
                    final Direction clientToServer = connection.clientToServer();
                    final Direction serverToClient = connection.serverToClient();
                    LOG.info(String.format("Read %d bytes from %s", clientToServer.read(), clientToServer.getFrom()));
                    LOG.info(String.format("Wrote %d bytes to %s", clientToServer.written(), clientToServer.getTo()));
                    LOG.info(String.format("Read %d bytes from %s", serverToClient.read(), serverToClient.getFrom()));
                    LOG.info(String.format("Wrote %d bytes to %s", serverToClient.written(), serverToClient.getTo()));
                }
            });

            server.start();
        }
        catch (final Throwable t) {
            LOG.error("Uncaught error", t);
        }
        new CountDownLatch(1).await();
    }
}
