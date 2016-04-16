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

import java.io.IOException;
import java.net.SocketAddress;

import net.jcip.annotations.Immutable;

import com.mattunderscore.tcproxy.io.socket.IOServerSocketChannel;

/**
 * The configuration for {@link IOServerSocketChannel}s.
 * @author Matt Champion on 02/12/2015
 */
@Immutable
public final class IOServerSocketChannelConfiguration extends AbstractIOSocketConfiguration<IOServerSocketChannel, IOServerSocketChannelConfiguration> {
    protected final SocketAddress boundSocket;

    /*package*/ IOServerSocketChannelConfiguration() {
        super();
        boundSocket = null;
    }

    /*package*/ IOServerSocketChannelConfiguration(Integer receiveBuffer, Integer sendBuffer, boolean blocking, Integer linger, boolean reuseAddress, SocketAddress boundSocket) {
        super(receiveBuffer, sendBuffer, blocking, linger, reuseAddress);

        this.boundSocket = boundSocket;
    }

    /**
     * Binds the socket to a local address.
     *
     * @param localAddress The local address
     * @return A new factory with the option set
     */
    public final IOServerSocketChannelConfiguration bind(SocketAddress localAddress) {
        return new IOServerSocketChannelConfiguration(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, localAddress);
    }

    @Override
    public IOServerSocketChannel apply(IOServerSocketChannel ioSocket) throws IOException {
        super.apply(ioSocket);
        ioSocket.bind(boundSocket);
        return ioSocket;
    }

    @Override
    protected IOServerSocketChannelConfiguration newConfiguration(Integer receiveBuffer, Integer sendBuffer, boolean blocking, Integer linger, boolean reuseAddress) {
        return new IOServerSocketChannelConfiguration(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, boundSocket);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final IOServerSocketChannelConfiguration that = (IOServerSocketChannelConfiguration) o;

        return !(boundSocket != null ? !boundSocket.equals(that.boundSocket) : that.boundSocket != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (boundSocket != null ? boundSocket.hashCode() : 0);
        return result;
    }

    /**
     * @return The default configuration
     */
    public static IOServerSocketChannelConfiguration defaultConfig() {
        return new IOServerSocketChannelConfiguration();
    }
}
