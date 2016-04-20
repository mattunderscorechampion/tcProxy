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

import static java.util.Objects.requireNonNull;

import java.net.InetSocketAddress;

import com.mattunderscore.tcproxy.io.configuration.IOSocketChannelConfiguration;
import com.mattunderscore.tcproxy.io.factory.IOFactory;
import com.mattunderscore.tcproxy.io.impl.JSLIOFactory;
import com.mattunderscore.tcproxy.selector.NoBackoff;
import com.mattunderscore.tcproxy.selector.SelectorBackoff;
import com.mattunderscore.tcproxy.selector.server.AbstractServerBuilder;
import com.mattunderscore.tcproxy.selector.server.AcceptSettings;
import com.mattunderscore.tcproxy.selector.server.Server;
import com.mattunderscore.tcproxy.selector.server.ServerImpl;

/**
 * @author Matt Champion on 16/04/2016
 */
public final class SimpleProxyBuilder extends AbstractServerBuilder<SimpleProxyBuilder> {
    private final IOFactory ioFactory;
    private final int selectorThreads;
    private final SelectorBackoff selectorBackoff;
    private final InetSocketAddress remote;

    protected SimpleProxyBuilder(
            AcceptSettings acceptSettings,
            IOSocketChannelConfiguration socketSettings,
            IOFactory ioFactory,
            int selectorThreads,
            SelectorBackoff selectorBackoff,
            InetSocketAddress remote) {
        super(acceptSettings, socketSettings);
        this.ioFactory = ioFactory;
        this.selectorThreads = selectorThreads;
        this.selectorBackoff = selectorBackoff;
        this.remote = remote;
    }

    public SimpleProxyBuilder remote(InetSocketAddress remote) {
        return new SimpleProxyBuilder(acceptSettings, socketSettings, ioFactory, selectorThreads, selectorBackoff, remote);
    }

    @Override
    public Server build() {
        requireNonNull(acceptSettings, "The accept settings have not been provided");
        requireNonNull(remote, "The target has not been provided");

        return new ServerImpl(
            new SimpleProxyServerStarter(
                ioFactory,
                acceptSettings.getListenOn(),
                selectorThreads,
                selectorBackoff,
                socketSettings,
                remote));
    }

    @Override
    protected SimpleProxyBuilder newServerBuilder(AcceptSettings acceptSettings, IOSocketChannelConfiguration socketSettings) {
        return new SimpleProxyBuilder(
            acceptSettings,
            socketSettings,
            ioFactory,
            selectorThreads,
            selectorBackoff,
            remote);
    }

    /**
     * @return A new builder for simple proxies
     */
    public static SimpleProxyBuilder builder() {
        return new SimpleProxyBuilder(
            null,
            IOSocketChannelConfiguration
                .defaultConfig()
                .receiveBuffer(4096)
                .sendBuffer(4096),
            new JSLIOFactory(),
            1,
            new NoBackoff(),
            null);
    }

    public static void main(String[] args) {
        final Server server = SimpleProxyBuilder
            .builder()
            .remote(new InetSocketAddress("localhost", 8080))
            .acceptSettings(
                AcceptSettings
                    .builder()
                    .listenOn(8085)
                    .build())
            .build();

        server.start();
        server.waitForStopped();
    }
}
