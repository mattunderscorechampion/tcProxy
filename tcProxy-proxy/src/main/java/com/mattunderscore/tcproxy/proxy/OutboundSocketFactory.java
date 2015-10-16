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
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketFactory;
import com.mattunderscore.tcproxy.io.IOSocketOption;
import com.mattunderscore.tcproxy.io.impl.StaticIOFactory;
import com.mattunderscore.tcproxy.proxy.settings.OutboundSocketSettings;

/**
 * Factory for outbound sockets.
 * @author Matt Champion on 18/02/14.
 */
public final class OutboundSocketFactory {
    private static final Logger LOG = LoggerFactory.getLogger("outbound socket factory");
    private static final long backOff = 5L;
    private final IOSocketFactory factory;
    private final InetSocketAddress remote;

    public OutboundSocketFactory(final OutboundSocketSettings settings) {
        factory = StaticIOFactory
            .socketFactoryBuilder()
            .setSocketOption(IOSocketOption.SEND_BUFFER, settings.getSendBuffer())
            .setSocketOption(IOSocketOption.RECEIVE_BUFFER, settings.getReceiveBuffer())
            .setSocketOption(IOSocketOption.BLOCKING, false)
            .build();
        remote = new InetSocketAddress(settings.getHost(), settings.getPort());
    }

    public IOSocketChannel createSocket() throws IOException {
        final IOSocketChannel channel = factory.create();
        channel.bind(null);
        channel.connect(remote);
        while (!channel.finishConnect()) {
            try {
                Thread.sleep(backOff);
            } catch (InterruptedException e) {
                LOG.debug("Interrupted while waiting for socket to be connected");
            }
        }
        return channel;
    }
}
