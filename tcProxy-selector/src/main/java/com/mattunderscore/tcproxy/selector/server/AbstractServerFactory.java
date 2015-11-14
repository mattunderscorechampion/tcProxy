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

import static java.lang.String.format;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.mattunderscore.tcproxy.io.IOFactory;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketFactory;
import com.mattunderscore.tcproxy.selector.SelectorFactory;
import com.mattunderscore.tcproxy.selector.threads.RestartableTask;
import com.mattunderscore.tcproxy.selector.threads.RestartableThread;

/**
 * An abstract server factory. Creates {@link IOServerSocketChannel}s based on the {@link AcceptSettings}. Creates a
 * {@link RestartableThread} for each requested selector thread, that runs a task returned by
 * {@link #getSelectorFactory(Collection, ServerConfig)}.
 * @author Matt Champion on 09/11/2015
 */
public abstract class AbstractServerFactory implements ServerFactory {
    protected final IOFactory ioFactory;

    public AbstractServerFactory(IOFactory ioFactory) {
        this.ioFactory = ioFactory;
    }

    @Override
    public final Server build(ServerConfig serverConfig) throws IOException {
        final AcceptSettings acceptSettings = serverConfig.getAcceptSettings();
        final int selectorThreads = serverConfig.getSelectorThreads();

        final IOSocketFactory<IOServerSocketChannel> factory = ioFactory
            .socketFactory(IOServerSocketChannel.class)
            .blocking(false)
            .reuseAddress(true);

        final Collection<IOServerSocketChannel> listenChannels = new HashSet<>();
        for (final Integer port : acceptSettings.getListenOn()) {
            listenChannels.add(
                factory
                    .bind(new InetSocketAddress(port))
                    .create());
        }

        final SelectorFactory<? extends RestartableTask> selectorFactory =
            getSelectorFactory(listenChannels, serverConfig);

        final ThreadFactory threadFactory = getThreadFactory();
        final Collection<RestartableThread> threads = new HashSet<>();
        for (int i = 0; i < selectorThreads; i++) {
            threads.add(new RestartableThread(threadFactory, selectorFactory.create()));
        }

        return new ServerImpl(threads);
    }

    /**
     * Return a thread factory. A basic factory is returned by default but this method can be overridden. Called once
     * for each call to {@link #build(ServerConfig)}.
     * @return A thread factory
     */
    protected ThreadFactory getThreadFactory() {
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
     * @param serverConfig The server config
     * @return A selector factory
     */
    protected abstract SelectorFactory<? extends RestartableTask> getSelectorFactory(
        Collection<IOServerSocketChannel> listenChannels,
        ServerConfig serverConfig);
}
