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

package com.mattunderscore.tcproxy.io.impl;

import java.io.IOException;

import com.mattunderscore.tcproxy.io.configuration.IOSocketConfiguration;
import com.mattunderscore.tcproxy.io.factory.IOFactory;
import com.mattunderscore.tcproxy.io.factory.IOOutboundSocketFactory;
import com.mattunderscore.tcproxy.io.socket.IOOutboundSocket;

/**
 * Abstract implementation of {@link IOOutboundSocketFactory}.
 * @author Matt Champion on 17/10/2015
 */
abstract class AbstractOutboundSocketFactoryImpl<T extends IOOutboundSocket, S extends IOSocketConfiguration<T, S>> implements IOOutboundSocketFactory<T> {
    protected final IOFactory ioFactory;
    protected final S configuration;

    AbstractOutboundSocketFactoryImpl(
        IOFactory ioFactory,
        S configuration) {

        this.ioFactory = ioFactory;
        this.configuration = configuration;
    }

    @Override
    public IOOutboundSocketFactory<T> receiveBuffer(Integer size) {
        return newBuilder(configuration.receiveBuffer(size));
    }

    @Override
    public IOOutboundSocketFactory<T> sendBuffer(Integer size) {
        return newBuilder(configuration.sendBuffer(size));
    }

    @Override
    public IOOutboundSocketFactory<T> blocking(boolean enabled) {
        return newBuilder(configuration.blocking(enabled));
    }

    @Override
    public IOOutboundSocketFactory<T> linger(Integer time) {
        return newBuilder(configuration.linger(time));
    }

    @Override
    public IOOutboundSocketFactory<T> reuseAddress(boolean enabled) {
        return newBuilder(configuration.reuseAddress(enabled));
    }

    /**
     * @param configuration The configuration
     * @return A concrete builder
     */
    protected abstract AbstractOutboundSocketFactoryImpl<T, S> newBuilder(S configuration);

    @Override
    public final T create() throws IOException {
        final T socket = newSocket();

        configuration.apply(socket);

        return socket;
    }

    /**
     * @return A new concrete socket
     * @throws IOException If there is a problem creating the socket
     */
    protected abstract T newSocket() throws IOException;
}
