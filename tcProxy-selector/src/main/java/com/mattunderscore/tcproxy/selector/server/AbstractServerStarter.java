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

import static com.mattunderscore.tcproxy.io.configuration.IOServerSocketChannelConfiguration.defaultConfig;
import static java.lang.String.format;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.IOFactory;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOServerSocketChannelFactory;
import com.mattunderscore.tcproxy.io.IOOutboundSocketFactory;
import com.mattunderscore.tcproxy.io.configuration.IOServerSocketChannelConfiguration;
import com.mattunderscore.tcproxy.selector.SelectorFactory;
import com.mattunderscore.tcproxy.selector.threads.RestartableTask;
import com.mattunderscore.tcproxy.selector.threads.RestartableThread;
import com.mattunderscore.tcproxy.selector.threads.RestartableThreadSet;

/**
 * Abstract implementation of {@link ServerStarter}. The accessor for the {@link SelectorFactory} must be implemented.
 * The accessor for the {@link ThreadFactory} can be overridden.
 * @author Matt Champion on 25/11/2015
 */
public abstract class AbstractServerStarter implements ServerStarter {
    private static final Logger LOG = LoggerFactory.getLogger("server-starter");
    protected final IOFactory ioFactory;
    protected final Iterable<Integer> portsToListenOn;
    protected final int selectorThreads;

    protected AbstractServerStarter(IOFactory ioFactory, Iterable<Integer> portsToListenOn, int selectorThreads) {
        this.ioFactory = ioFactory;
        this.portsToListenOn = portsToListenOn;
        this.selectorThreads = selectorThreads;
    }

    @Override
    public final Collection<IOServerSocketChannel> bindServerSockets() throws IOException {
        final IOOutboundSocketFactory<IOServerSocketChannel> factory = ioFactory
            .socketFactory(
                defaultConfig()
                    .blocking(false)
                    .reuseAddress(true));

        final Collection<IOServerSocketChannel> listenChannels = new HashSet<>();
        try {
            for (final Integer port : portsToListenOn) {
                listenChannels.add(
                    factory
                        .bind(new InetSocketAddress(port))
                        .create());
            }

            return listenChannels;
        }
        catch (IOException createException) {
            // If there was a problem creating either a socket or a selector close any opened sockets and propagate the
            // exception
            for (IOServerSocketChannel channel : listenChannels) {
                try {
                    channel.close();
                }
                catch (IOException closeException) {
                    LOG.warn("Problem closing channel {}", channel, closeException);
                }
            }

            throw createException;
        }
    }

    @Override
    public final RestartableThreadSet createServerThreads(Collection<IOServerSocketChannel> listenChannels, Server server) throws IOException {
        final SelectorFactory<? extends RestartableTask> selectorFactory =
            getSelectorFactory(listenChannels);

        final ThreadFactory threadFactory = getThreadFactory(server);
        final Set<RestartableThread> threads = new HashSet<>();
        for (int i = 0; i < selectorThreads; i++) {
            threads.add(new RestartableThread(threadFactory, selectorFactory.create()));
        }

        return new RestartableThreadSet(threads);
    }

    /**
     * Return a thread factory. A basic factory is returned by default but this method can be overridden.
     * @return A thread factory
     * @param server The server
     */
    protected ThreadFactory getThreadFactory(Server server) {
        return new ThreadFactory() {
            private final AtomicInteger threadCount = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                final Thread t = new Thread(r);
                t.setName(format("selector-%d", threadCount.getAndIncrement()));
                t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        System.err.println(format("Exception on %s not caught", t));
                        e.printStackTrace(System.err);
                    }
                });
                return t;
            }
        };
    }

    /**
     * Factory for selector tasks.
     * @param listenChannels Channels to listen on
     * @return A selector factory
     */
    protected abstract SelectorFactory<? extends RestartableTask> getSelectorFactory(Collection<IOServerSocketChannel> listenChannels);
}
