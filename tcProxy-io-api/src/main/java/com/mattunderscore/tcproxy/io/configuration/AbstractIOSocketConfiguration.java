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

package com.mattunderscore.tcproxy.io.configuration;

import static com.mattunderscore.tcproxy.io.socket.IOSocketOption.BLOCKING;
import static com.mattunderscore.tcproxy.io.socket.IOSocketOption.LINGER;
import static com.mattunderscore.tcproxy.io.socket.IOSocketOption.RECEIVE_BUFFER;
import static com.mattunderscore.tcproxy.io.socket.IOSocketOption.REUSE_ADDRESS;
import static com.mattunderscore.tcproxy.io.socket.IOSocketOption.SEND_BUFFER;

import java.io.IOException;

import com.mattunderscore.tcproxy.io.socket.IOSocket;

/**
 * Abstract implementation of socket configuration that contains the common properties of all sockets.
 * @author Matt Champion on 02/12/2015
 */
public abstract class AbstractIOSocketConfiguration<T extends IOSocket, S extends IOSocketConfiguration<T, S>> implements IOSocketConfiguration<T, S> {
    protected final Integer receiveBuffer;
    protected final Integer sendBuffer;
    protected final Boolean blocking;
    protected final Integer linger;
    protected final Boolean reuseAddress;

    protected AbstractIOSocketConfiguration() {
        receiveBuffer = null;
        sendBuffer = null;
        blocking = true;
        linger = null;
        reuseAddress = false;
    }

    /**
     * Constructor.
     * @param receiveBuffer The receive buffer size
     * @param sendBuffer The send buffer
     * @param blocking If the socket should be blocking
     * @param linger The linger time
     * @param reuseAddress If the address can be reused without waiting
     */
    protected AbstractIOSocketConfiguration(Integer receiveBuffer, Integer sendBuffer, Boolean blocking, Integer linger, Boolean reuseAddress) {
        this.receiveBuffer = receiveBuffer;
        this.sendBuffer = sendBuffer;
        this.blocking = blocking;
        this.linger = linger;
        this.reuseAddress = reuseAddress;
    }

    @Override
    public T apply(T ioSocket) throws IOException {
        ioSocket.set(BLOCKING, blocking != null ? blocking : false);
        ioSocket.set(REUSE_ADDRESS, reuseAddress != null ? reuseAddress : false);
        if (receiveBuffer != null) {
            ioSocket.set(RECEIVE_BUFFER, receiveBuffer);
        }
        if (sendBuffer != null) {
            ioSocket.set(SEND_BUFFER, sendBuffer);
        }
        if (linger != null) {
            ioSocket.set(LINGER, linger);
        }
        return ioSocket;
    }

    @Override
    public final S receiveBuffer(Integer size) {
        return newConfiguration(size, sendBuffer, blocking, linger, reuseAddress);
    }

    @Override
    public final S sendBuffer(Integer size) {
        return newConfiguration(receiveBuffer, size, blocking, linger, reuseAddress);
    }

    @Override
    public final S blocking(boolean enabled) {
        return newConfiguration(receiveBuffer, sendBuffer, enabled, linger, reuseAddress);
    }

    @Override
    public final S linger(Integer time) {
        return newConfiguration(receiveBuffer, sendBuffer, blocking, time, reuseAddress);
    }

    @Override
    public final S reuseAddress(boolean enabled) {
        return newConfiguration(receiveBuffer, sendBuffer, blocking, linger, enabled);
    }

    /**
     * @param receiveBuffer The receive buffer size
     * @param sendBuffer    The send buffer size
     * @param blocking      Enable blocking
     * @param linger        Set linger
     * @param reuseAddress  Enable reuse address
     * @return A concrete builder
     */
    protected abstract S newConfiguration(
        Integer receiveBuffer,
        Integer sendBuffer,
        boolean blocking,
        Integer linger,
        boolean reuseAddress);

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AbstractIOSocketConfiguration<T, S> that = (AbstractIOSocketConfiguration<T, S>) o;
        return blocking == that.blocking &&
            reuseAddress == that.reuseAddress &&
            !(receiveBuffer != null ? !receiveBuffer.equals(that.receiveBuffer) : that.receiveBuffer != null) &&
            !(sendBuffer != null ? !sendBuffer.equals(that.sendBuffer) : that.sendBuffer != null) &&
            !(linger != null ? !linger.equals(that.linger) : that.linger != null);
    }

    @Override
    public int hashCode() {
        int result = receiveBuffer != null ? receiveBuffer.hashCode() : 0;
        result = 31 * result + (sendBuffer != null ? sendBuffer.hashCode() : 0);
        result = 31 * result + (blocking ? 1 : 0);
        result = 31 * result + (linger != null ? linger.hashCode() : 0);
        result = 31 * result + (reuseAddress ? 1 : 0);
        return result;
    }
}
