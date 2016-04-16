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

import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;

import net.jcip.annotations.Immutable;

/**
 * A configuration for {@link IOSocketChannel}s.
 * @author Matt Champion on 02/12/2015
 */
@Immutable
public final class IOSocketChannelConfiguration extends AbstractIOSocketConfiguration<IOSocketChannel, IOSocketChannelConfiguration> {
    protected final Boolean keepAlive;
    protected final Boolean noDelay;

    IOSocketChannelConfiguration() {
        super();
        keepAlive = null;
        noDelay = null;
    }

    /*package*/ IOSocketChannelConfiguration(
            Integer receiveBuffer,
            Integer sendBuffer,
            boolean blocking,
            Integer linger,
            boolean reuseAddress,
            Boolean keepAlive,
            Boolean noDelay) {
        super(receiveBuffer, sendBuffer, blocking, linger, reuseAddress);
        this.keepAlive = keepAlive;
        this.noDelay = noDelay;
    }

    @Override
    public IOSocketChannel apply(IOSocketChannel ioSocketChannel) throws IOException {
        super.apply(ioSocketChannel);

        if (keepAlive != null) {
            ioSocketChannel.set(KEEP_ALIVE, keepAlive);
        }
        if (noDelay != null) {
            ioSocketChannel.set(TCP_NO_DELAY, noDelay);
        }

        return ioSocketChannel;
    }

    /**
     * Set the socket option for TCP_NODELAY. Defaults to false.
     *
     * @param enabled Enable the option
     * @return A new factory with the option set
     */
    public IOSocketChannelConfiguration noDelay(boolean enabled) {
        return new IOSocketChannelConfiguration(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, keepAlive, enabled);
    }

    /**
     * Set the socket option for SO_KEEP_ALIVE. Defaults to false.
     *
     * @param enabled Enable the option
     * @return A new factory with the option set
     */
    public IOSocketChannelConfiguration keepAlive(boolean enabled) {
        return new IOSocketChannelConfiguration(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, enabled, noDelay);
    }

    @Override
    protected IOSocketChannelConfiguration newConfiguration(Integer receiveBuffer, Integer sendBuffer, boolean blocking, Integer linger, boolean reuseAddress) {
        return new IOSocketChannelConfiguration(receiveBuffer, sendBuffer, blocking, linger, reuseAddress, keepAlive, noDelay);
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

        final IOSocketChannelConfiguration that = (IOSocketChannelConfiguration) o;

        return !(keepAlive != null ? !keepAlive.equals(that.keepAlive) : that.keepAlive != null) &&
            !(noDelay != null ? !noDelay.equals(that.noDelay) : that.noDelay != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (keepAlive != null ? keepAlive.hashCode() : 0);
        result = 31 * result + (noDelay != null ? noDelay.hashCode() : 0);
        return result;
    }

    /**
     * @return The default configuration
     */
    public static IOSocketChannelConfiguration defaultConfig() {
        return new IOSocketChannelConfiguration();
    }
}
