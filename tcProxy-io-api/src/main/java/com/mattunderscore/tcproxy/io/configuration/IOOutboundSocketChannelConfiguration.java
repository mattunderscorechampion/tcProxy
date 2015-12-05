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

import static com.mattunderscore.tcproxy.io.IOSocketOption.KEEP_ALIVE;
import static com.mattunderscore.tcproxy.io.IOSocketOption.TCP_NO_DELAY;

import java.io.IOException;
import java.net.SocketAddress;

import com.mattunderscore.tcproxy.io.IOOutboundSocketChannel;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;

/**
 * The configuration for {@link IOServerSocketChannel}s.
 * @author Matt Champion on 05/12/2015
 */
public final class IOOutboundSocketChannelConfiguration extends AbstractIOSocketConfiguration<IOOutboundSocketChannel> {
    protected final Boolean keepAlive;
    protected final Boolean noDelay;
    protected final SocketAddress boundSocket;

    IOOutboundSocketChannelConfiguration(Integer receiveBuffer, Integer sendBuffer, boolean blocking, Integer linger, boolean reuseAddress, SocketAddress boundSocket, Boolean keepAlive, Boolean noDelay) {
        super(receiveBuffer, sendBuffer, blocking, linger, reuseAddress);

        this.keepAlive = keepAlive;
        this.noDelay = noDelay;
        this.boundSocket = boundSocket;
    }

    /**
     * @return Instance of {@link IOOutboundSocketChannelConfigurationBuilder}
     */
    public static IOOutboundSocketChannelConfigurationBuilder builder() {
        return new IOOutboundSocketChannelConfigurationBuilder();
    }

    public void apply(IOOutboundSocketChannel ioSocketChannel) throws IOException {
        super.apply(ioSocketChannel);

        if (keepAlive != null) {
            ioSocketChannel.set(KEEP_ALIVE, keepAlive);
        }
        if (noDelay != null) {
            ioSocketChannel.set(TCP_NO_DELAY, noDelay);
        }
        if (boundSocket != null) {
            ioSocketChannel.bind(boundSocket);
        }
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

        final IOOutboundSocketChannelConfiguration that = (IOOutboundSocketChannelConfiguration) o;

        return !(keepAlive != null ? !keepAlive.equals(that.keepAlive) : that.keepAlive != null) &&
            !(noDelay != null ? !noDelay.equals(that.noDelay) : that.noDelay != null) &&
            !(boundSocket != null ? !boundSocket.equals(that.boundSocket) : that.boundSocket != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (keepAlive != null ? keepAlive.hashCode() : 0);
        result = 31 * result + (noDelay != null ? noDelay.hashCode() : 0);
        result = 31 * result + (boundSocket != null ? boundSocket.hashCode() : 0);
        return result;
    }
}
