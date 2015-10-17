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
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.mattunderscore.tcproxy.io.IOSocket;
import com.mattunderscore.tcproxy.io.IOSocketFactory;
import com.mattunderscore.tcproxy.io.IOSocketOption;

/**
 * Abstract implementation of {@link IOSocketFactory}.
 * @author Matt Champion on 17/10/2015
 */
abstract class AbstractSocketFactoryImpl<T extends IOSocket> implements IOSocketFactory<T> {
    protected final Map<IOSocketOption<?>, Object> options;
    protected final SocketAddress boundSocket;

    AbstractSocketFactoryImpl() {
        boundSocket = null;
        options = new HashMap<>();
    }

    AbstractSocketFactoryImpl(SocketAddress boundSocket, Map<IOSocketOption<?>, Object> options) {
        this.boundSocket = boundSocket;
        this.options = options;
    }

    @Override
    public final <O> IOSocketFactory<T> set(IOSocketOption<O> option, O value) {
        final Map<IOSocketOption<?>, Object> newOptions = new HashMap<>(options);
        newOptions.put(option, value);
        return newBuilder(boundSocket, newOptions);
    }

    @Override
    public IOSocketFactory<T> bind(SocketAddress localAddress) {
        return newBuilder(localAddress, options);
    }

    /**
     * @param newAddress The new socket address
     * @param newOptions The new options
     * @return A new concrete builder
     */
    protected abstract IOSocketFactory<T> newBuilder(SocketAddress newAddress, Map<IOSocketOption<?>, Object> newOptions);

    @Override
    public final T create() throws IOException {
        final T socket = newSocket();

        // Apply builder options to socket
        for (Map.Entry<IOSocketOption<?>, Object> entry : options.entrySet()) {
            socket.set((IOSocketOption) entry.getKey(), entry.getValue());
        }

        return socket;
    }

    /**
     * @return A new concrete socket
     * @throws IOException If there is a problem creating the socket
     */
    protected abstract T newSocket() throws IOException;
}
