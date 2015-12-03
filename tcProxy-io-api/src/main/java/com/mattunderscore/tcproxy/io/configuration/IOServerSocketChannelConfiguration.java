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

import com.mattunderscore.tcproxy.io.IOServerSocketChannel;

/**
 * The configuration for a {@link IOServerSocketChannel}s.
 * @author Matt Champion on 02/12/2015
 */
public final class IOServerSocketChannelConfiguration extends AbstractIOSocketConfiguration<IOServerSocketChannel> {
    protected IOServerSocketChannelConfiguration(Integer receiveBuffer, Integer sendBuffer, boolean blocking, Integer linger, boolean reuseAddress, SocketAddress boundSocket) {
        super(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, boundSocket);
    }

    public static final class Builder extends AbstractIOSocketConfiguration.AbstractIOSocketConfigurationBuilder<IOServerSocketChannel, IOServerSocketChannelConfiguration> {
        Builder() {
            super();
        }

        Builder(
            Integer receiveBuffer,
            Integer sendBuffer,
            boolean blocking,
            Integer linger,
            boolean reuseAddress,
            SocketAddress boundSocket) {
            super(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, boundSocket);
        }

        @Override
        protected Builder newBuilder(Integer receiveBuffer, Integer sendBuffer, boolean blocking, Integer linger, boolean reuseAddress, SocketAddress boundSocket) {
            return new Builder(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, boundSocket);
        }
    }

    /**
     * @return Instance of {@link IOServerSocketChannelConfiguration.Builder}
     */
    public static IOServerSocketChannelConfiguration.Builder builder() {
        return new Builder();
    }
}
