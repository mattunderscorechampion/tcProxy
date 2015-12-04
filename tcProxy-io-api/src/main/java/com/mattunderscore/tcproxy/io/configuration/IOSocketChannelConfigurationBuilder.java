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

import com.mattunderscore.tcproxy.io.IOSocketChannel;

/**
 * A socket configuration builder for {@link IOSocketChannelConfiguration}. Adds the {@link #noDelay} and
 * {@link #keepAlive} settings to the common settings.
 * @author Matt Champion on 03/12/2015
 */
public final class IOSocketChannelConfigurationBuilder extends AbstractIOSocketConfigurationBuilder<IOSocketChannel, IOSocketChannelConfiguration> {
    protected final Boolean keepAlive;
    protected final Boolean noDelay;

    IOSocketChannelConfigurationBuilder() {
        keepAlive = false;
        noDelay = false;
    }

    IOSocketChannelConfigurationBuilder(
        Integer receiveBuffer,
        Integer sendBuffer,
        boolean blocking,
        Integer linger,
        boolean reuseAddress,
        SocketAddress boundSocket, Boolean keepAlive, Boolean noDelay) {

        super(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, boundSocket);

        this.keepAlive = keepAlive;
        this.noDelay = noDelay;
    }

    @Override
    protected IOSocketChannelConfigurationBuilder newBuilder(
        Integer receiveBuffer,
        Integer sendBuffer,
        boolean blocking,
        Integer linger,
        boolean reuseAddress,
        SocketAddress boundSocket) {
        return new IOSocketChannelConfigurationBuilder(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, boundSocket, keepAlive, noDelay);
    }

    /**
     * Set the socket option for TCP_NODELAY. Defaults to false.
     *
     * @param enabled Enable the option
     * @return A new factory with the option set
     */
    public IOSocketChannelConfigurationBuilder noDelay(boolean enabled) {
        return new IOSocketChannelConfigurationBuilder(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, boundSocket, keepAlive, enabled);
    }

    /**
     * Set the socket option for SO_KEEP_ALIVE. Defaults to false.
     *
     * @param enabled Enable the option
     * @return A new factory with the option set
     */
    public IOSocketChannelConfigurationBuilder keepAlive(boolean enabled) {
        return new IOSocketChannelConfigurationBuilder(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, boundSocket, enabled, noDelay);
    }
}
