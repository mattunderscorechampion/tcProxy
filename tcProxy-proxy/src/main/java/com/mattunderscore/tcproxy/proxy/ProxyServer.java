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

import com.mattunderscore.tcproxy.io.IOSelector;
import com.mattunderscore.tcproxy.io.impl.IOFactory;
import com.mattunderscore.tcproxy.proxy.action.processor.DefaultActionProcessorFactory;
import com.mattunderscore.tcproxy.proxy.settings.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The proxy.
 * @author Matt Champion on 18/02/14.
 */
public class ProxyServer {
    private static final Logger LOG = LoggerFactory.getLogger("proxy");
    private final Acceptor acceptor;
    private final WriteSelector writer;
    private final ReadSelector proxy;

    public ProxyServer(final AcceptorSettings acceptorSettings,
                       final ConnectionSettings connectionSettings,
                       final InboundSocketSettings inboundSocketSettings,
                       final OutboundSocketSettings outboundSocketSettings,
                       final ReadSelectorSettings readSelectorSettings,
                       final ConnectionManager manager) throws IOException {
        final BlockingQueue<Connection> newConnections = new ArrayBlockingQueue<>(5000);
        final BlockingQueue<Direction> newDirections = new ArrayBlockingQueue<>(5000);
        final OutboundSocketFactory socketFactory = new OutboundSocketFactory(outboundSocketSettings);
        final ConnectionFactory connectionFactory = new ConnectionFactory(connectionSettings, manager, new DefaultActionProcessorFactory(newDirections));
        final IOSelector readSelector = IOFactory.openSelector();
        final IOSelector writeSelector = IOFactory.openSelector();

        acceptor = new Acceptor(acceptorSettings, inboundSocketSettings, connectionFactory, socketFactory);
        proxy = new ReadSelector(readSelector,readSelectorSettings, newConnections);
        writer = new WriteSelector(writeSelector, newDirections);
        manager.addListener(new ConnectionManager.Listener() {
            @Override
            public void newConnection(final Connection connection) {
                newConnections.add(connection);
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
