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

import com.mattunderscore.tcproxy.cli.arguments.*;
import com.mattunderscore.tcproxy.proxy.connection.Connection;
import com.mattunderscore.tcproxy.proxy.connection.ConnectionManager;
import com.mattunderscore.tcproxy.proxy.direction.Direction;
import com.mattunderscore.tcproxy.proxy.ProxyServer;
import com.mattunderscore.tcproxy.proxy.settings.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.LogManager;

/**
 * @author matt on 18/02/14.
 */
public final class ProxyServerMain {
    public static final Logger LOG = LoggerFactory.getLogger("cli");
    private static final Option<Void> HELP = Option.create("-h", "--help", "Display usage");
    private static final Option<Integer> INBOUND_PORT = Option.create("-ip", "--ip", "Inbound port", 8085, IntegerParser.PARSER);
    private static final Option<String> OUTBOUND_HOST = Option.create("-oh", "--oh", "Outbound host", "localhost", StringParser.PARSER);
    private static final Option<Integer> OUTBOUND_PORT = Option.create("-op", "--op", "Outbound port", 8080, IntegerParser.PARSER);
    private static final Option<Integer> QUEUE_SIZE = Option.create("-qs", "--qs", "Queue size", 10000, IntegerParser.PARSER);
    private static final Option<Integer> BATCH_SIZE = Option.create("-bs", "--bs", "Batch size", 2048, IntegerParser.PARSER);
    private static final Option<Integer> SEND_BUFFER = Option.create("-sb", "--sb", "Send buffer, ", 10240, IntegerParser.PARSER);
    private static final Option<Integer> RECEIVE_BUFFER = Option.create("-rb", "--rb", "Receive buffer", 10240, IntegerParser.PARSER);

    public static void main(final String[] args) throws IOException, InterruptedException {
        LogManager.getLogManager().readConfiguration(ProxyServerMain.class.getResourceAsStream("/logging.properties"));

        final Map<Option<?>, Object> settings = getSettings(args);
        if (settings.containsKey(HELP)) {
            final HelpDisplay usage = new HelpDisplay(getOptions());
            usage.printTo(System.out);
            return;
        }

        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            final ConnectionManager manager = new ConnectionManager();
            final ProxyServer server = new ProxyServer(
                new AcceptorSettings((Integer)settings.get(INBOUND_PORT)),
                new ConnectionSettings((Integer)settings.get(QUEUE_SIZE), (Integer)settings.get(BATCH_SIZE)),
                new InboundSocketSettings((Integer)settings.get(RECEIVE_BUFFER), (Integer)settings.get(SEND_BUFFER)),
                new OutboundSocketSettings(
                    (Integer)settings.get(OUTBOUND_PORT),
                    (String)settings.get(OUTBOUND_HOST),
                    (Integer)settings.get(RECEIVE_BUFFER),
                    (Integer)settings.get(SEND_BUFFER)),
                new ReadSelectorSettings((Integer)settings.get(RECEIVE_BUFFER)),
                manager);

            manager.addListener(new ConnectionManager.Listener() {
                private final Map<Connection, Future<?>> tasks = new ConcurrentHashMap<>();
                @Override
                public void newConnection(final Connection connection) {
                    LOG.info("New connection");
                    if (LOG.isInfoEnabled()) {
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
                }

                @Override
                public void closedConnection(final Connection connection) {
                    final Future<?> task = tasks.get(connection);
                    if (task != null) {
                        task.cancel(true);
                    }
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
            new CountDownLatch(1).await();
        }
        catch (final Throwable t) {
            LOG.error("Uncaught error", t);
        }
    }

    private static final Option<?>[] getOptions() {
        return new Option<?>[] {
            HELP,
            INBOUND_PORT,
            OUTBOUND_HOST,
            OUTBOUND_PORT,
            QUEUE_SIZE,
            BATCH_SIZE,
            SEND_BUFFER,
            RECEIVE_BUFFER
        };
    }

    private static final Map<Option<?>, Object> getSettings(String[] args) {
        final Map<Option<?>, Object> map = new HashMap<>();
        final Option<?>[] options = getOptions();
        final OptionsParser parser = new OptionsParser(options);
        final List<Setting<?>> settings = parser.parse(args);
        for (Setting<?> setting : settings) {
            map.put(setting.getOption(), setting.getValue());
        }
        return map;
    }
}
