/* Copyright © 2016 Matthew Champion
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

package com.mattunderscore.tcproxy.simple;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.factory.IOFactory;
import com.mattunderscore.tcproxy.io.socket.IOServerSocketChannel;
import com.mattunderscore.tcproxy.selector.SelectorBackoff;
import com.mattunderscore.tcproxy.selector.SelectorFactory;
import com.mattunderscore.tcproxy.selector.SocketChannelSelector;
import com.mattunderscore.tcproxy.selector.connecting.ConnectionHandlerFactory;
import com.mattunderscore.tcproxy.selector.server.AbstractServerStarter;
import com.mattunderscore.tcproxy.selector.server.Server;
import com.mattunderscore.tcproxy.selector.server.SocketConfigurator;
import com.mattunderscore.tcproxy.selector.server.SocketSettings;

/**
 * @author Matt Champion on 15/04/2016
 */
/*package*/ final class SimpleProxyServerStarter extends AbstractServerStarter {
    private static final Logger LOG = LoggerFactory.getLogger("server");
    private static final AtomicInteger THREAD_COUNT = new AtomicInteger(0);
    private final SelectorBackoff selectorBackoff;
    private final SocketSettings inboundSocketSettings;
    private final ConnectionHandlerFactory connectionHandlerFactory;
    private final InetSocketAddress remote;

    /*packaage*/ SimpleProxyServerStarter(
        IOFactory ioFactory,
        Iterable<Integer> portsToListenOn,
        int selectorThreads,
        SelectorBackoff selectorBackoff,
        SocketSettings inboundSocketSettings,
        InetSocketAddress remote) {
        super(ioFactory, portsToListenOn, selectorThreads);
        this.selectorBackoff = selectorBackoff;
        this.inboundSocketSettings = inboundSocketSettings;
        this.remote = remote;

        connectionHandlerFactory = new SimpleProxyConnectionHandlerFactory(this.remote);
    }

    @Override
    protected ThreadFactory getThreadFactory(final Server server) {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                final Thread newThread = new Thread(r);
                newThread.setName("Simple Proxy Thread - " + THREAD_COUNT.getAndIncrement());

                newThread.setDaemon(false);

                final ExceptionHandler handler = new ExceptionHandler(server);
                newThread.setUncaughtExceptionHandler(handler);
                return newThread;
            }
        };
    }

    @Override
    protected SelectorFactory<SocketChannelSelector> getSelectorFactory(
            Collection<IOServerSocketChannel> listenChannels) {
        final SocketConfigurator socketConfigurator = new SocketConfigurator(inboundSocketSettings);
        return new SimpleProxySelectorFactory(
            connectionHandlerFactory,
            selectorBackoff,
            listenChannels,
            socketConfigurator);
    }

    private final class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final Server server;

        public ExceptionHandler(Server server) {
            this.server = server;
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            LOG.error("Uncaught exception in thread '{}'", t, e);
            server.stop();
        }
    }
}
