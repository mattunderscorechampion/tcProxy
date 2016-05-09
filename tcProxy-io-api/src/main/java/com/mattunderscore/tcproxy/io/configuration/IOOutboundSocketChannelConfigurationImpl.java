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

import static com.mattunderscore.tcproxy.io.socket.IOSocketOption.KEEP_ALIVE;
import static com.mattunderscore.tcproxy.io.socket.IOSocketOption.TCP_NO_DELAY;

import java.io.IOException;
import java.net.SocketAddress;

import com.mattunderscore.tcproxy.io.socket.IOOutboundSocketChannel;

import net.jcip.annotations.Immutable;

/**
 * Implementation of {@link IOOutboundSocketConfiguration}.
 * @author Matt Champion on 05/12/2015
 */
@Immutable
/*package*/ final class IOOutboundSocketChannelConfigurationImpl extends AbstractIOSocketConfiguration<IOOutboundSocketChannel, IOOutboundSocketChannelConfiguration> implements IOOutboundSocketChannelConfiguration {
    protected final boolean keepAlive;
    protected final boolean noDelay;
    protected final SocketAddress boundSocket;

    IOOutboundSocketChannelConfigurationImpl() {
        super();
        keepAlive = false;
        noDelay = false;
        boundSocket = null;
    }

    IOOutboundSocketChannelConfigurationImpl(Integer receiveBuffer, Integer sendBuffer, boolean blocking, Integer linger, boolean reuseAddress, SocketAddress boundSocket, Boolean keepAlive, Boolean noDelay) {
        super(receiveBuffer, sendBuffer, blocking, linger, reuseAddress);

        this.keepAlive = keepAlive;
        this.noDelay = noDelay;
        this.boundSocket = boundSocket;
    }

    @Override
    public IOOutboundSocketChannel apply(IOOutboundSocketChannel ioSocketChannel) throws IOException {
        super.apply(ioSocketChannel);

        ioSocketChannel.set(KEEP_ALIVE, keepAlive);
        ioSocketChannel.set(TCP_NO_DELAY, noDelay);
        ioSocketChannel.bind(boundSocket);

        return ioSocketChannel;
    }

    @Override
    protected IOOutboundSocketChannelConfigurationImpl newConfiguration(Integer receiveBuffer, Integer sendBuffer, boolean blocking, Integer linger, boolean reuseAddress) {
        return new IOOutboundSocketChannelConfigurationImpl(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, boundSocket, keepAlive, noDelay);
    }

    @Override
    public IOOutboundSocketChannelConfigurationImpl noDelay(boolean enabled) {
        return new IOOutboundSocketChannelConfigurationImpl(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, boundSocket, keepAlive, enabled);
    }

    @Override
    public IOOutboundSocketChannelConfigurationImpl keepAlive(boolean enabled) {
        return new IOOutboundSocketChannelConfigurationImpl(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, boundSocket, enabled, noDelay);
    }

    @Override
    public IOOutboundSocketChannelConfigurationImpl bind(SocketAddress localAddress) {
        return new IOOutboundSocketChannelConfigurationImpl(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, localAddress, keepAlive, noDelay);
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

        IOOutboundSocketChannelConfigurationImpl that = (IOOutboundSocketChannelConfigurationImpl) o;

        return keepAlive == that.keepAlive &&
            noDelay == that.noDelay &&
            !(boundSocket != null ? !boundSocket.equals(that.boundSocket) : that.boundSocket != null);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (keepAlive ? 1 : 0);
        result = 31 * result + (noDelay ? 1 : 0);
        result = 31 * result + (boundSocket != null ? boundSocket.hashCode() : 0);
        return result;
    }

    /**
     * @return The default configuration
     */
    public static IOOutboundSocketChannelConfigurationImpl defaultConfig() {
        return new IOOutboundSocketChannelConfigurationImpl();
    }
}
