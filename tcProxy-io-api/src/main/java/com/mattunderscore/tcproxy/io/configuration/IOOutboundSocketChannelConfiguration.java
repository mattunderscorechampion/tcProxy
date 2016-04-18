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
import com.mattunderscore.tcproxy.io.socket.IOServerSocketChannel;

import net.jcip.annotations.Immutable;

/**
 * The configuration for {@link IOServerSocketChannel}s.
 * @author Matt Champion on 05/12/2015
 */
@Immutable
public final class IOOutboundSocketChannelConfiguration extends AbstractIOSocketConfiguration<IOOutboundSocketChannel, IOOutboundSocketChannelConfiguration> {
    protected final boolean keepAlive;
    protected final boolean noDelay;
    protected final SocketAddress boundSocket;

    IOOutboundSocketChannelConfiguration() {
        super();
        keepAlive = false;
        noDelay = false;
        boundSocket = null;
    }

    IOOutboundSocketChannelConfiguration(Integer receiveBuffer, Integer sendBuffer, boolean blocking, Integer linger, boolean reuseAddress, SocketAddress boundSocket, Boolean keepAlive, Boolean noDelay) {
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
    protected IOOutboundSocketChannelConfiguration newConfiguration(Integer receiveBuffer, Integer sendBuffer, boolean blocking, Integer linger, boolean reuseAddress) {
        return new IOOutboundSocketChannelConfiguration(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, boundSocket, keepAlive, noDelay);
    }

    /**
     * Set the socket option for TCP_NODELAY. Defaults to false.
     *
     * @param enabled Enable the option
     * @return A new factory with the option set
     */
    public IOOutboundSocketChannelConfiguration noDelay(boolean enabled) {
        return new IOOutboundSocketChannelConfiguration(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, boundSocket, keepAlive, enabled);
    }

    /**
     * Set the socket option for SO_KEEP_ALIVE. Defaults to false.
     *
     * @param enabled Enable the option
     * @return A new factory with the option set
     */
    public IOOutboundSocketChannelConfiguration keepAlive(boolean enabled) {
        return new IOOutboundSocketChannelConfiguration(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, boundSocket, enabled, noDelay);
    }

    /**
     * Binds the socket to a local address.
     *
     * @param localAddress The local address
     * @return A new factory with the option set
     */
    public IOOutboundSocketChannelConfiguration bind(SocketAddress localAddress) {
        return new IOOutboundSocketChannelConfiguration(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, localAddress, keepAlive, noDelay);
    }

    /**
     * Merge in {@link IOSocketChannelConfiguration}.
     * @param channelConfiguration The configuration
     * @return A new configuration with the provided configuration applied
     */
    public IOOutboundSocketChannelConfiguration configure(IOSocketChannelConfiguration channelConfiguration) {
        return new IOOutboundSocketChannelConfiguration(
            channelConfiguration.receiveBuffer != null ? channelConfiguration.receiveBuffer : receiveBuffer,
            channelConfiguration.sendBuffer != null ? channelConfiguration.sendBuffer : sendBuffer,
            channelConfiguration.blocking != null ? channelConfiguration.blocking : blocking,
            channelConfiguration.linger != null ? channelConfiguration.linger : linger,
            channelConfiguration.reuseAddress != null ? channelConfiguration.reuseAddress : reuseAddress,
            boundSocket,
            channelConfiguration.keepAlive != null ? channelConfiguration.keepAlive : keepAlive,
            channelConfiguration.noDelay != null ? channelConfiguration.noDelay : noDelay);
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

        IOOutboundSocketChannelConfiguration that = (IOOutboundSocketChannelConfiguration) o;

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
    public static IOOutboundSocketChannelConfiguration defaultConfig() {
        return new IOOutboundSocketChannelConfiguration();
    }
}
