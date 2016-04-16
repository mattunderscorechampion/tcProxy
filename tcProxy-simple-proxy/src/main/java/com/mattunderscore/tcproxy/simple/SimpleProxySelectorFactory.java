/* Copyright © 2016 Matthew Champion
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

package com.mattunderscore.tcproxy.simple;

import static com.mattunderscore.tcproxy.io.impl.StaticIOFactory.openSelector;

import java.io.IOException;
import java.util.Collection;

import com.mattunderscore.tcproxy.io.socket.IOServerSocketChannel;
import com.mattunderscore.tcproxy.selector.SelectorBackoff;
import com.mattunderscore.tcproxy.selector.SelectorFactory;
import com.mattunderscore.tcproxy.selector.SocketChannelSelector;
import com.mattunderscore.tcproxy.selector.connecting.ConnectingSelector;
import com.mattunderscore.tcproxy.selector.connecting.ConnectionHandlerFactory;
import com.mattunderscore.tcproxy.selector.connecting.SharedConnectingSelectorFactory;
import com.mattunderscore.tcproxy.selector.general.GeneralPurposeSelector;
import com.mattunderscore.tcproxy.selector.server.SocketConfigurator;

/**
 * @author Matt Champion on 15/04/2016
 */
/* package */ final class SimpleProxySelectorFactory implements SelectorFactory<SocketChannelSelector> {
    private final Collection<IOServerSocketChannel> listenChannels;
    private final SocketConfigurator socketConfigurator;
    private final ConnectionHandlerFactory connectionHandlerFactory;
    private final SelectorBackoff selectorBackoff;

    public SimpleProxySelectorFactory(
            ConnectionHandlerFactory connectionHandlerFactory,
            SelectorBackoff selectorBackoff,
            Collection<IOServerSocketChannel> listenChannels,
            SocketConfigurator socketConfigurator) {
        this.connectionHandlerFactory = connectionHandlerFactory;
        this.selectorBackoff = selectorBackoff;
        this.listenChannels = listenChannels;
        this.socketConfigurator = socketConfigurator;
    }

    @Override
    public SocketChannelSelector create() throws IOException {
        final GeneralPurposeSelector generalPurposeSelector =
            new GeneralPurposeSelector(openSelector(), selectorBackoff);

        final SelectorFactory<ConnectingSelector> connectingSelectorFactory = new SharedConnectingSelectorFactory(
            generalPurposeSelector,
            listenChannels,
            connectionHandlerFactory,
            socketConfigurator);

        return connectingSelectorFactory.create();
    }
}
