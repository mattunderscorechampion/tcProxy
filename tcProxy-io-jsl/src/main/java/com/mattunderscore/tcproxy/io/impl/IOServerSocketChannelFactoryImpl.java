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

package com.mattunderscore.tcproxy.io.impl;

import java.io.IOException;
import java.net.SocketAddress;

import com.mattunderscore.tcproxy.io.IOFactory;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOServerSocketChannelFactory;
import com.mattunderscore.tcproxy.io.IOOutboundSocketFactory;

/**
 * A {@link IOOutboundSocketFactory} implementation for {@link IOServerSocketChannel}s.
 * @author Matt Champion on 17/10/2015
 */
final class IOServerSocketChannelFactoryImpl extends AbstractOutboundSocketFactoryImpl<IOServerSocketChannel> implements IOServerSocketChannelFactory {

    IOServerSocketChannelFactoryImpl(IOFactory ioFactory) {
        super(ioFactory, null, null, true, null, false, null);
    }

    IOServerSocketChannelFactoryImpl(
        IOFactory ioFactory,
        Integer receiveBuffer,
        Integer sendBuffer,
        boolean blocking,
        Integer linger,
        boolean reuseAddress,
        SocketAddress boundSocket) {

        super(ioFactory, receiveBuffer, sendBuffer, blocking, linger, reuseAddress, boundSocket);
    }

    @Override
    protected IOOutboundSocketFactory<IOServerSocketChannel> newBuilder(
        Integer receiveBuffer,
        Integer sendBuffer,
        boolean blocking,
        Integer linger,
        boolean reuseAddress,
        SocketAddress boundSocket) {

        return new IOServerSocketChannelFactoryImpl(
            ioFactory,
            receiveBuffer,
            sendBuffer,
            blocking,
            linger,
            reuseAddress,
            boundSocket);
    }

    @Override
    protected IOServerSocketChannel newSocket() throws IOException {
        return ioFactory.openServerSocket();
    }
}
