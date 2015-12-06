/* Copyright © 2014 Matthew Champion
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
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.mattunderscore.tcproxy.io.IOFactory;
import com.mattunderscore.tcproxy.io.IOOutboundSocket;
import com.mattunderscore.tcproxy.io.IOOutboundSocketChannel;
import com.mattunderscore.tcproxy.io.IOOutboundSocketChannelFactory;
import com.mattunderscore.tcproxy.io.IOOutboundSocketFactory;
import com.mattunderscore.tcproxy.io.IOSelector;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOServerSocketChannelFactory;
import com.mattunderscore.tcproxy.io.configuration.IOOutboundSocketChannelConfiguration;
import com.mattunderscore.tcproxy.io.configuration.IOServerSocketChannelConfiguration;
import com.mattunderscore.tcproxy.io.configuration.IOSocketConfiguration;

/**
 * Factory implementation for sockets and selectors that uses the Java Standard Libary.
 * @author matt on 30/06/14.
 */
public final class JSLIOFactory implements IOFactory {
    @Override
    public IOSelector openSelector() throws IOException {
        return new IOSelectorImpl(Selector.open());
    }

    @Override
    public IOOutboundSocketChannel openSocket() throws IOException {
        return new IOSocketChannelImpl(SocketChannel.open());
    }

    @Override
    public IOServerSocketChannel openServerSocket() throws IOException {
        return new IOServerSocketChannelImpl(ServerSocketChannel.open());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IOOutboundSocketFactory<?>> T socketFactory(Class<T> type) {
        if (IOServerSocketChannelFactory.class.equals(type)) {
            return (T) new IOServerSocketChannelFactoryImpl(this, IOServerSocketChannelConfiguration.defaultConfig());
        }
        else if (IOOutboundSocketChannelFactory.class.equals(type)) {
            return (T) new IOOutboundSocketChannelFactoryImpl(this, IOOutboundSocketChannelConfiguration.defaultConfig());
        }
        else {
            throw new IllegalArgumentException("No factory available for " + type.getCanonicalName());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IOOutboundSocket, S extends IOOutboundSocketFactory<T>> S socketFactory(IOSocketConfiguration<T> configuration) {
        if (IOServerSocketChannelConfiguration.class.equals(configuration.getClass())) {
            return (S) new IOServerSocketChannelFactoryImpl(this, (IOServerSocketChannelConfiguration) configuration);
        }
        else if (IOOutboundSocketChannelConfiguration.class.equals(configuration.getClass())) {
            return (S) new IOOutboundSocketChannelFactoryImpl(this, (IOOutboundSocketChannelConfiguration) configuration);
        }
        else {
            throw new IllegalArgumentException("No factory available for " + configuration.getClass().getCanonicalName());
        }
    }
}
