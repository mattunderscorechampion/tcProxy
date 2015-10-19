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

import com.mattunderscore.tcproxy.io.IOSocket;
import com.mattunderscore.tcproxy.io.IOSocketFactory;
import com.mattunderscore.tcproxy.io.IOSocketOption;

/**
 * Abstract implementation of {@link IOSocketFactory}.
 * @author Matt Champion on 17/10/2015
 */
abstract class AbstractSocketFactoryImpl<T extends IOSocket> implements IOSocketFactory<T> {
    protected final Integer receiveBuffer;
    protected final Integer sendBuffer;
    protected final boolean blocking;
    protected final Boolean keepAlive;
    protected final Integer linger;
    protected final boolean reuseAddress;
    protected final Boolean noDelay;
    protected final SocketAddress boundSocket;

    AbstractSocketFactoryImpl() {
        boundSocket = null;
        receiveBuffer = null;
        sendBuffer = null;
        blocking = true;
        keepAlive = false;
        linger = null;
        reuseAddress = false;
        noDelay = false;
    }

    AbstractSocketFactoryImpl(
        Integer receiveBuffer,
        Integer sendBuffer,
        boolean blocking,
        Boolean keepAlive,
        Integer linger,
        boolean reuseAddress,
        Boolean noDelay,
        SocketAddress boundSocket) {

        this.receiveBuffer = receiveBuffer;
        this.sendBuffer = sendBuffer;
        this.blocking = blocking;
        this.keepAlive = keepAlive;
        this.linger = linger;
        this.reuseAddress = reuseAddress;
        this.noDelay = noDelay;
        this.boundSocket = boundSocket;
    }

    @Override
    public IOSocketFactory<T> receiveBuffer(Integer size) {
        return newBuilder(size, sendBuffer, blocking, keepAlive, linger, reuseAddress, noDelay, boundSocket);
    }

    @Override
    public IOSocketFactory<T> sendBuffer(Integer size) {
        return newBuilder(receiveBuffer, size, blocking, keepAlive, linger, reuseAddress, noDelay, boundSocket);
    }

    @Override
    public IOSocketFactory<T> blocking(boolean enabled) {
        return newBuilder(receiveBuffer, sendBuffer, enabled, keepAlive, linger, reuseAddress, noDelay, boundSocket);
    }

    @Override
    public IOSocketFactory<T> keepAlive(boolean enabled) {
        return newBuilder(receiveBuffer, sendBuffer, blocking, enabled, linger, reuseAddress, noDelay, boundSocket);
    }

    @Override
    public IOSocketFactory<T> linger(Integer time) {
        return newBuilder(receiveBuffer, sendBuffer, blocking, keepAlive, time, reuseAddress, noDelay, boundSocket);
    }

    @Override
    public IOSocketFactory<T> reuseAddress(boolean enabled) {
        return newBuilder(receiveBuffer, sendBuffer, blocking, keepAlive, linger, enabled, noDelay, boundSocket);
    }

    @Override
    public IOSocketFactory<T> noDelay(boolean enabled) {
        return newBuilder(receiveBuffer, sendBuffer, blocking, keepAlive, linger, reuseAddress, enabled, boundSocket);
    }

    @Override
    public IOSocketFactory<T> bind(SocketAddress newSocket) {
        return newBuilder(receiveBuffer, sendBuffer, blocking, keepAlive, linger, reuseAddress, noDelay, newSocket);
    }

    /**
     * @param receiveBuffer The receive buffer size
     * @param sendBuffer The send buffer size
     * @param blocking Enable blocking
     * @param keepAlive Enable keep alive
     * @param linger Set linger
     * @param reuseAddress Enable reuse address
     * @param noDelay Enable no delay
     * @param boundSocket Local address
     * @return A concrete builder
     */
    protected abstract IOSocketFactory<T> newBuilder(
        Integer receiveBuffer,
        Integer sendBuffer,
        boolean blocking,
        Boolean keepAlive,
        Integer linger,
        boolean reuseAddress,
        Boolean noDelay,
        SocketAddress boundSocket);

    @Override
    public final T create() throws IOException {
        final T socket = newSocket();

        if (receiveBuffer != null) {
            socket.set(IOSocketOption.RECEIVE_BUFFER, receiveBuffer);
        }
        if (sendBuffer != null) {
            socket.set(IOSocketOption.SEND_BUFFER, sendBuffer);
        }
        socket.set(IOSocketOption.BLOCKING, blocking);
        if (keepAlive != null) {
            socket.set(IOSocketOption.KEEP_ALIVE, keepAlive);
        }
        if (linger != null) {
            socket.set(IOSocketOption.LINGER, linger);
        }
        socket.set(IOSocketOption.REUSE_ADDRESS, reuseAddress);
        if (noDelay != null) {
            socket.set(IOSocketOption.TCP_NO_DELAY, noDelay);
        }
        socket.bind(boundSocket);

        return socket;
    }

    /**
     * @return A new concrete socket
     * @throws IOException If there is a problem creating the socket
     */
    protected abstract T newSocket() throws IOException;
}
