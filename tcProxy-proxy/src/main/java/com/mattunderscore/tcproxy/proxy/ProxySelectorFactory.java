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

package com.mattunderscore.tcproxy.proxy;

import static com.mattunderscore.tcproxy.io.impl.StaticIOFactory.openSelector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;

import com.mattunderscore.tcproxy.io.configuration.IOSocketConfiguration;
import com.mattunderscore.tcproxy.io.selection.IOSelectionKey;
import com.mattunderscore.tcproxy.io.socket.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;
import com.mattunderscore.tcproxy.proxy.connection.Connection;
import com.mattunderscore.tcproxy.proxy.connection.ConnectionManager;
import com.mattunderscore.tcproxy.proxy.direction.Direction;
import com.mattunderscore.tcproxy.proxy.selector.ReadSelectionRunnable;
import com.mattunderscore.tcproxy.proxy.settings.ReadSelectorSettings;
import com.mattunderscore.tcproxy.selector.SelectorBackoff;
import com.mattunderscore.tcproxy.selector.SelectorFactory;
import com.mattunderscore.tcproxy.selector.SocketChannelSelector;
import com.mattunderscore.tcproxy.selector.connecting.ConnectionHandlerFactory;
import com.mattunderscore.tcproxy.selector.connecting.task.AcceptingTask;
import com.mattunderscore.tcproxy.selector.general.GeneralPurposeSelector;

/**
 * {@link SelectorFactory} for a proxy server.
 * @author Matt Champion on 30/03/2016
 */
final class ProxySelectorFactory implements SelectorFactory<SocketChannelSelector> {
    private final Collection<IOServerSocketChannel> listenChannels;
    private final IOSocketConfiguration<IOSocketChannel, ?> socketSettings;
    private final ConnectionHandlerFactory connectionHandlerFactory;
    private final ConnectionManager manager;
    private final ReadSelectorSettings readSelectorSettings;
    private final SelectorBackoff selectorBackoff;

    public ProxySelectorFactory(
            ConnectionHandlerFactory connectionHandlerFactory,
            ConnectionManager manager,
            ReadSelectorSettings readSelectorSettings,
            SelectorBackoff selectorBackoff,
            Collection<IOServerSocketChannel> listenChannels,
            IOSocketConfiguration<IOSocketChannel, ?> socketSettings) {
        this.listenChannels = listenChannels;
        this.socketSettings = socketSettings;
        this.connectionHandlerFactory = connectionHandlerFactory;
        this.manager = manager;
        this.readSelectorSettings = readSelectorSettings;
        this.selectorBackoff = selectorBackoff;
    }

    @Override
    public SocketChannelSelector create() throws IOException {
        final GeneralPurposeSelector selector =
            new GeneralPurposeSelector(openSelector(), selectorBackoff);

        for (final IOServerSocketChannel serverSocketChannel : listenChannels) {
            selector.register(
                serverSocketChannel,
                new AcceptingTask(selector, connectionHandlerFactory.create(selector), socketSettings));
        }

        final ByteBuffer readBuffer = ByteBuffer.allocateDirect(readSelectorSettings.getReadBufferSize());
        manager.addListener(new ConnectionManager.Listener() {
            @Override
            public void newConnection(final Connection connection) {
                final Direction cTs = connection.clientToServer();
                final IOSocketChannel channel0 = cTs.getFrom();
                selector.register(channel0, IOSelectionKey.Op.READ, new ReadSelectionRunnable(cTs, connection, readBuffer));

                final Direction sTc = connection.serverToClient();
                final IOSocketChannel channel1 = sTc.getFrom();
                selector.register(channel1, IOSelectionKey.Op.READ, new ReadSelectionRunnable(sTc, connection, readBuffer));
            }

            @Override
            public void closedConnection(final Connection connection) {
            }
        });

        return selector;
    }
}
