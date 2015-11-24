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

import static com.mattunderscore.tcproxy.io.impl.StaticIOFactory.openSelector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.CircularBuffer;
import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.io.impl.CircularBufferImpl;
import com.mattunderscore.tcproxy.io.impl.StaticIOFactory;
import com.mattunderscore.tcproxy.proxy.connection.Connection;
import com.mattunderscore.tcproxy.proxy.connection.ConnectionManager;
import com.mattunderscore.tcproxy.proxy.direction.Direction;
import com.mattunderscore.tcproxy.proxy.direction.DirectionAndConnection;
import com.mattunderscore.tcproxy.proxy.selector.ProxyConnectionHandlerFactory;
import com.mattunderscore.tcproxy.proxy.selector.ReadSelectionRunnable;
import com.mattunderscore.tcproxy.proxy.settings.ConnectionSettings;
import com.mattunderscore.tcproxy.proxy.settings.OutboundSocketSettings;
import com.mattunderscore.tcproxy.proxy.settings.ProxyServerSettings;
import com.mattunderscore.tcproxy.proxy.settings.ReadSelectorSettings;
import com.mattunderscore.tcproxy.selector.SelectorBackoff;
import com.mattunderscore.tcproxy.selector.SelectorFactory;
import com.mattunderscore.tcproxy.selector.SocketChannelSelector;
import com.mattunderscore.tcproxy.selector.connecting.ConnectingSelector;
import com.mattunderscore.tcproxy.selector.connecting.ConnectionHandlerFactory;
import com.mattunderscore.tcproxy.selector.connecting.SharedConnectingSelectorFactory;
import com.mattunderscore.tcproxy.selector.general.GeneralPurposeSelector;
import com.mattunderscore.tcproxy.selector.server.AcceptSettings;
import com.mattunderscore.tcproxy.selector.server.Server;
import com.mattunderscore.tcproxy.selector.server.SocketConfigurator;
import com.mattunderscore.tcproxy.selector.server.SocketSettings;
import com.mattunderscore.tcproxy.selector.threads.RestartableThread;
import com.mattunderscore.tcproxy.selector.threads.UncheckedInterruptedException;

/**
 * The proxy.
 * @author Matt Champion on 18/02/14.
 */
public final class ProxyServer implements Server {
    private static final Logger LOG = LoggerFactory.getLogger("proxy");
    private static final AtomicInteger THREAD_COUNT = new AtomicInteger(0);
    private final AcceptSettings acceptorSettings;
    private final ConnectionSettings connectionSettings;
    private final SocketSettings inboundSocketSettings;
    private final OutboundSocketSettings outboundSocketSettings;
    private final ReadSelectorSettings readSelectorSettings;
    private final SelectorBackoff selectorBackoff;
    private final ConnectionManager manager;
    private final ThreadFactory factory;
    private volatile CountDownLatch currentReadyLatch = new CountDownLatch(1);
    private volatile SocketChannelSelector currentSelector;

    public ProxyServer(
        ProxyServerSettings settings,
        ConnectionManager manager) throws IOException {
        this.acceptorSettings = settings.getAcceptSettings();
        this.connectionSettings = settings.getConnectionSettings();
        this.inboundSocketSettings = settings.getInboundSocketSettings();
        this.outboundSocketSettings = settings.getOutboundSocketSettings();
        this.readSelectorSettings = settings.getReadSelectorSettings();
        this.selectorBackoff = settings.getBackoff();
        this.manager = manager;
        this.factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                final Thread newThread = new Thread(r);
                newThread.setName("tcProxy - Proxy Thread - " + THREAD_COUNT.getAndIncrement());

                newThread.setDaemon(false);

                final ExceptionHandler handler = new ExceptionHandler();
                newThread.setUncaughtExceptionHandler(handler);
                return newThread;
            }
        };
    }

    @Override
    public void start() {
        final SocketChannelSelector selector;
        try {
            final OutboundSocketFactory socketFactory = new OutboundSocketFactory(outboundSocketSettings);
            final GeneralPurposeSelector generalPurposeSelector =
                new GeneralPurposeSelector(openSelector(), selectorBackoff);
            final ConnectionHandlerFactory connectionHandlerFactory = new ProxyConnectionHandlerFactory(
                socketFactory,
                connectionSettings,
                manager);

            final IOServerSocketChannel channel = StaticIOFactory
                .socketFactory(IOServerSocketChannel.class)
                .receiveBuffer(inboundSocketSettings.getReceiveBuffer())
                .reuseAddress(true)
                .blocking(false)
                .bind(new InetSocketAddress(acceptorSettings.getListenOn().iterator().next()))
                .create();

            final SelectorFactory<ConnectingSelector> connectingSelectorFactory = new SharedConnectingSelectorFactory(
                generalPurposeSelector,
                channel,
                connectionHandlerFactory,
                new SocketConfigurator(inboundSocketSettings));

            selector = connectingSelectorFactory.create();
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to start proxy server", e);
        }

        final CircularBuffer circularBuffer = CircularBufferImpl.allocateDirect(readSelectorSettings.getReadBufferSize());
        manager.addListener(new ConnectionManager.Listener() {
            @Override
            public void newConnection(final Connection connection) {
                final Direction cTs = connection.clientToServer();
                final DirectionAndConnection dc0 = new DirectionAndConnection(cTs, connection);
                final IOSocketChannel channel0 = cTs.getFrom();
                selector.register(channel0, IOSelectionKey.Op.READ, new ReadSelectionRunnable(dc0, circularBuffer));

                final Direction sTc = connection.serverToClient();
                final DirectionAndConnection dc1 = new DirectionAndConnection(sTc, connection);
                final IOSocketChannel channel1 = sTc.getFrom();
                selector.register(channel1, IOSelectionKey.Op.READ, new ReadSelectionRunnable(dc1, circularBuffer));
            }

            @Override
            public void closedConnection(final Connection connection) {
            }
        });

        final RestartableThread selectorThread = new RestartableThread(factory, selector);

        selectorThread.start();
        currentSelector = selector;
        selector.waitForRunning();
        currentReadyLatch.countDown();
    }

    @Override
    public void stop() {
        try {
            currentReadyLatch.await();
        }
        catch (InterruptedException e) {
            throw new UncheckedInterruptedException(e);
        }
        currentReadyLatch = new CountDownLatch(1);
        currentSelector.stop();
    }

    @Override
    public void restart() {
        stop();
        start();
    }

    @Override
    public void waitForRunning() {
        try {
            currentReadyLatch.await();
        }
        catch (InterruptedException e) {
            throw new UncheckedInterruptedException(e);
        }
        currentSelector.waitForRunning();
    }

    @Override
    public void waitForStopped() {
        try {
            currentReadyLatch.await();
        }
        catch (InterruptedException e) {
            throw new UncheckedInterruptedException(e);
        }
        currentSelector.waitForStopped();
    }

    private final class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            LOG.error("Uncaught exception in thread '{}'", t, e);
            stop();
        }
    }
}
