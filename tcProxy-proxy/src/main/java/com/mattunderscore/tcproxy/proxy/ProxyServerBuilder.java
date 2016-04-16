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

package com.mattunderscore.tcproxy.proxy;

import static java.util.Objects.requireNonNull;

import com.mattunderscore.tcproxy.io.configuration.IOSocketChannelConfiguration;
import com.mattunderscore.tcproxy.io.configuration.IOSocketConfiguration;
import com.mattunderscore.tcproxy.io.factory.IOFactory;
import com.mattunderscore.tcproxy.io.impl.JSLIOFactory;
import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;
import com.mattunderscore.tcproxy.proxy.connection.ConnectionManager;
import com.mattunderscore.tcproxy.proxy.settings.ConnectionSettings;
import com.mattunderscore.tcproxy.proxy.settings.OutboundSocketSettings;
import com.mattunderscore.tcproxy.proxy.settings.ReadSelectorSettings;
import com.mattunderscore.tcproxy.selector.NoBackoff;
import com.mattunderscore.tcproxy.selector.SelectorBackoff;
import com.mattunderscore.tcproxy.selector.server.AbstractServerBuilder;
import com.mattunderscore.tcproxy.selector.server.AcceptSettings;
import com.mattunderscore.tcproxy.selector.server.Server;
import com.mattunderscore.tcproxy.selector.server.ServerBuilder;
import com.mattunderscore.tcproxy.selector.server.ServerImpl;

/**
 * An implementation of {@link ServerBuilder} for proxy servers.
 * @author Matt Champion on 13/04/2016
 */
public final class ProxyServerBuilder extends AbstractServerBuilder<ProxyServerBuilder> {
    private final ConnectionSettings connectionSettings;
    private final OutboundSocketSettings outboundSocketSettings;
    private final ReadSelectorSettings readSelectorSettings;
    private final SelectorBackoff selectorBackoff;
    private final IOFactory ioFactory;
    private final int selectorThreads;
    private final ConnectionManager manager;

    protected ProxyServerBuilder(
            AcceptSettings acceptSettings,
            IOSocketConfiguration<IOSocketChannel> socketSettings,
            ConnectionSettings connectionSettings,
            OutboundSocketSettings outboundSocketSettings,
            ReadSelectorSettings readSelectorSettings,
            SelectorBackoff selectorBackoff,
            IOFactory ioFactory,
            int selectorThreads,
            ConnectionManager manager) {
        super(acceptSettings, socketSettings);
        this.connectionSettings = connectionSettings;
        this.outboundSocketSettings = outboundSocketSettings;
        this.readSelectorSettings = readSelectorSettings;
        this.selectorBackoff = selectorBackoff;
        this.ioFactory = ioFactory;
        this.selectorThreads = selectorThreads;
        this.manager = manager;
    }

    public ProxyServerBuilder connectionSettings(ConnectionSettings connectionSettings) {
        requireNonNull(connectionSettings, "Connections settings cannot be null");

        return new ProxyServerBuilder(
            acceptSettings,
            socketSettings,
            connectionSettings,
            outboundSocketSettings,
            readSelectorSettings,
            selectorBackoff,
            ioFactory,
            selectorThreads,
            manager);
    }

    public ProxyServerBuilder outboundSocketSettings(OutboundSocketSettings outboundSocketSettings) {
        requireNonNull(outboundSocketSettings, "Outbound socket settings settings cannot be null");

        return new ProxyServerBuilder(
            acceptSettings,
            socketSettings,
            connectionSettings,
            outboundSocketSettings,
            readSelectorSettings,
            selectorBackoff,
            ioFactory,
            selectorThreads,
            manager);
    }

    public ProxyServerBuilder readSelectorSettings(ReadSelectorSettings readSelectorSettings) {
        requireNonNull(readSelectorSettings, "Read selector settings cannot be null");

        return new ProxyServerBuilder(
            acceptSettings,
            socketSettings,
            connectionSettings,
            outboundSocketSettings,
            readSelectorSettings,
            selectorBackoff,
            ioFactory,
            selectorThreads,
            manager);
    }

    public ProxyServerBuilder backoff(SelectorBackoff selectorBackoff) {
        requireNonNull(selectorBackoff, "Selector backoff cannot be null");

        return new ProxyServerBuilder(
            acceptSettings,
            socketSettings,
            connectionSettings,
            outboundSocketSettings,
            readSelectorSettings,
            selectorBackoff,
            ioFactory,
            selectorThreads,
            manager);
    }

    public ProxyServerBuilder ioFactory(IOFactory ioFactory) {
        requireNonNull(ioFactory, "IOFactory cannot be null");

        return new ProxyServerBuilder(
            acceptSettings,
            socketSettings,
            connectionSettings,
            outboundSocketSettings,
            readSelectorSettings,
            selectorBackoff,
            ioFactory,
            selectorThreads,
            manager);
    }

    public ProxyServerBuilder selectorThreads(int selectorThreads) {
        return new ProxyServerBuilder(
            acceptSettings,
            socketSettings,
            connectionSettings,
            outboundSocketSettings,
            readSelectorSettings,
            selectorBackoff,
            ioFactory,
            selectorThreads,
            manager);
    }

    public ProxyServerBuilder connectionManager(ConnectionManager manager) {
        return new ProxyServerBuilder(
            acceptSettings,
            socketSettings,
            connectionSettings,
            outboundSocketSettings,
            readSelectorSettings,
            selectorBackoff,
            ioFactory,
            selectorThreads,
            manager);
    }

    @Override
    public Server build() {
        if (acceptSettings == null) {
            throw new IllegalStateException("Accept settings not provided");
        }

        if (outboundSocketSettings == null) {
            throw new IllegalStateException("Outbound socket settings not provided");
        }

        final ProxyServerStarter serverStarter = new ProxyServerStarter(
            ioFactory,
            acceptSettings.getListenOn(),
            selectorThreads,
            outboundSocketSettings,
            selectorBackoff,
            connectionSettings,
            manager != null ? manager : new ConnectionManager(),
            socketSettings,
            readSelectorSettings);
        return new ServerImpl(serverStarter);
    }

    @Override
    protected ProxyServerBuilder newServerBuilder(AcceptSettings acceptSettings, IOSocketConfiguration<IOSocketChannel> socketSettings) {
        return new ProxyServerBuilder(
            acceptSettings,
            socketSettings,
            connectionSettings,
            outboundSocketSettings,
            readSelectorSettings,
            selectorBackoff,
            ioFactory,
            selectorThreads,
            manager);
    }

    /**
     * @return A new {@link ProxyServerBuilder}
     */
    public static ProxyServerBuilder builder() {
        return new ProxyServerBuilder(
            null,
            IOSocketChannelConfiguration
                .defaultConfig()
                .receiveBuffer(1024)
                .sendBuffer(1024),
            ConnectionSettings
                .builder()
                .batchSize(1024)
                .writeQueueSize(1024)
                .build(),
            null,
            ReadSelectorSettings
                .builder()
                .readBufferSize(1024)
                .build(),
            new NoBackoff(),
            new JSLIOFactory(),
            1,
            null);
    }
}
