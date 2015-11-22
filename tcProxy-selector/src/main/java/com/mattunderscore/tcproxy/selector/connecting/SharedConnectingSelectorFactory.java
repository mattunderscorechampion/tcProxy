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

package com.mattunderscore.tcproxy.selector.connecting;

import static java.util.Collections.singleton;

import java.io.IOException;
import java.util.Collection;

import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.selector.SelectorFactory;
import com.mattunderscore.tcproxy.selector.connecting.task.AcceptingTask;
import com.mattunderscore.tcproxy.selector.general.GeneralPurposeSelector;
import com.mattunderscore.tcproxy.selector.server.SocketConfigurator;

/**
 * @author Matt Champion on 22/11/2015
 */
public final class SharedConnectingSelectorFactory implements SelectorFactory<ConnectingSelector> {
    private final GeneralPurposeSelector generalPurposeSelector;
    private final Collection<IOServerSocketChannel> serverSocketChannels;
    private final ConnectionHandlerFactory connectionHandlerFactory;
    private final SocketConfigurator socketConfigurator;

    public SharedConnectingSelectorFactory(
        GeneralPurposeSelector generalPurposeSelector,
        IOServerSocketChannel serverSocketChannel,
        ConnectionHandlerFactory connectionHandlerFactory,
        SocketConfigurator socketConfigurator) {

        this(generalPurposeSelector, singleton(serverSocketChannel), connectionHandlerFactory, socketConfigurator);
    }

    public SharedConnectingSelectorFactory(
        GeneralPurposeSelector generalPurposeSelector,
        Collection<IOServerSocketChannel> serverSocketChannels,
        ConnectionHandlerFactory connectionHandlerFactory,
        SocketConfigurator socketConfigurator) {

        this.generalPurposeSelector = generalPurposeSelector;
        this.serverSocketChannels = serverSocketChannels;
        this.connectionHandlerFactory = connectionHandlerFactory;
        this.socketConfigurator = socketConfigurator;
    }

    @Override
    public ConnectingSelector create() throws IOException {
        final ConnectingSelector selector = new ConnectingSelector(generalPurposeSelector);
        for (final IOServerSocketChannel serverSocketChannel : serverSocketChannels) {
            generalPurposeSelector.register(
                serverSocketChannel,
                new AcceptingTask(selector, connectionHandlerFactory.create(selector), socketConfigurator));
        }
        return selector;
    }
}
