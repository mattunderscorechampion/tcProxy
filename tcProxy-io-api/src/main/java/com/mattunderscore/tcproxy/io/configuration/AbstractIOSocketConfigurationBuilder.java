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

import java.net.SocketAddress;

import com.mattunderscore.tcproxy.io.IOSocket;

/**
 * An abstract socket configuration builder. Provides the common settings.
 * @author Matt Champion on 03/12/2015
 */
public abstract class AbstractIOSocketConfigurationBuilder<T extends IOSocket, S extends AbstractIOSocketConfiguration<T>> {
    protected final Integer receiveBuffer;
    protected final Integer sendBuffer;
    protected final boolean blocking;
    protected final Integer linger;
    protected final boolean reuseAddress;

    /*package*/ AbstractIOSocketConfigurationBuilder() {
        receiveBuffer = null;
        sendBuffer = null;
        blocking = true;
        linger = null;
        reuseAddress = false;
    }

    /*package*/ AbstractIOSocketConfigurationBuilder(
        Integer receiveBuffer,
        Integer sendBuffer,
        boolean blocking,
        Integer linger,
        boolean reuseAddress) {

        this.receiveBuffer = receiveBuffer;
        this.sendBuffer = sendBuffer;
        this.blocking = blocking;
        this.linger = linger;
        this.reuseAddress = reuseAddress;
    }

    /**
     * Set the socket option for SO_RCVBUF. Defaults to null.
     *
     * @param size The size of the buffer or null to use the system default
     * @return A new factory with the option set
     */
    public final AbstractIOSocketConfigurationBuilder<T, S> receiveBuffer(Integer size) {
        return newBuilder(size, sendBuffer, blocking, linger, reuseAddress);
    }

    /**
     * Set the socket option for SO_SNDBUF. Defaults to null.
     *
     * @param size The size of the buffer or null to use the system default
     * @return A new factory with the option set
     */
    public final AbstractIOSocketConfigurationBuilder<T, S> sendBuffer(Integer size) {
        return newBuilder(receiveBuffer, size, blocking, linger, reuseAddress);
    }

    /**
     * Set the socket to blocking mode. Defaults to false.
     *
     * @param enabled Enable the option
     * @return A new factory with the option set
     */
    public final AbstractIOSocketConfigurationBuilder<T, S> blocking(boolean enabled) {
        return newBuilder(receiveBuffer, sendBuffer, enabled, linger, reuseAddress);
    }

    /**
     * Set the socket option for SO_LINGER. Defaults to null.
     *
     * @param time The linger time or null to use the system default
     * @return A new factory with the option set
     */
    public final AbstractIOSocketConfigurationBuilder<T, S> linger(Integer time) {
        return newBuilder(receiveBuffer, sendBuffer, blocking, time, reuseAddress);
    }

    /**
     * Set the socket option for SO_REUSEADDR. Defaults to false.
     *
     * @param enabled Enable the option
     * @return A new factory with the option set
     */
    public final AbstractIOSocketConfigurationBuilder<T, S> reuseAddress(boolean enabled) {
        return newBuilder(receiveBuffer, sendBuffer, blocking, linger, enabled);
    }

    /**
     * @return A new configuration of type S.
     */
    public final S build() {
        return newConfiguration();
    }

    /**
     * @return The new configuration required by the concrete class.
     */
    protected abstract S newConfiguration();

    /**
     * @param receiveBuffer The receive buffer size
     * @param sendBuffer    The send buffer size
     * @param blocking      Enable blocking
     * @param linger        Set linger
     * @param reuseAddress  Enable reuse address
     * @return A concrete builder
     */
    protected abstract AbstractIOSocketConfigurationBuilder<T, S> newBuilder(
        Integer receiveBuffer,
        Integer sendBuffer,
        boolean blocking,
        Integer linger,
        boolean reuseAddress);
}
