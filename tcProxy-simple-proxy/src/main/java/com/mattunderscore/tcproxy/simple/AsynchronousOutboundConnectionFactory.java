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

import java.io.IOException;
import java.net.InetSocketAddress;

import com.mattunderscore.tcproxy.io.configuration.IOOutboundSocketChannelConfiguration;
import com.mattunderscore.tcproxy.io.configuration.IOSocketChannelConfiguration;
import com.mattunderscore.tcproxy.io.factory.IOOutboundSocketFactory;
import com.mattunderscore.tcproxy.io.impl.StaticIOFactory;
import com.mattunderscore.tcproxy.io.selection.IOSelectionKey;
import com.mattunderscore.tcproxy.io.socket.IOOutboundSocketChannel;
import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;
import com.mattunderscore.tcproxy.selector.SelectionRunnable;
import com.mattunderscore.tcproxy.selector.SocketChannelSelector;
import com.mattunderscore.tcproxy.selector.general.RegistrationHandle;

/**
 * @author Matt Champion on 15/04/2016
 */
/* package */ final class AsynchronousOutboundConnectionFactory {
    private final IOOutboundSocketFactory<IOOutboundSocketChannel> factory;
    private final InetSocketAddress remote;
    private final SocketChannelSelector selector;

    /**
     * Constructor.
     * @param remote The target address
     * @param selector The selector to use to connect the socket
     * @param socketSettings The socket configuration
     */
    public AsynchronousOutboundConnectionFactory(
            InetSocketAddress remote,
            SocketChannelSelector selector,
            IOSocketChannelConfiguration socketSettings) {
        this.remote = remote;
        this.selector = selector;
        factory = StaticIOFactory
            .socketFactory(IOOutboundSocketChannelConfiguration
                .defaultConfig()
                .configure(socketSettings)
                .blocking(true));
    }

    /**
     * Asynchronously create a connection.
     * @param connectionCallback The callback for when the connection has completed
     */
    public void createConnection(final ConnectionCallback connectionCallback) {
        final IOOutboundSocketChannel channel;
        try {
            channel = factory.create();
            channel.connect(remote);
        }
        catch (IOException e) {
            connectionCallback.onException(e);
            return;
        }

        selector.register(channel, IOSelectionKey.Op.CONNECT, new SelectionRunnable<IOSocketChannel>() {
            @Override
            public void run(IOSocketChannel socket, RegistrationHandle handle) {
                if (!handle.isConnectable()) {
                    return;
                }

                try {
                    if (channel.finishConnect()) {
                        handle.cancel();
                        connectionCallback.onConnected(channel);
                    }
                }
                catch (IOException e) {
                    handle.cancel();
                    connectionCallback.onException(e);
                }
            }
        });
    }

    /**
     * The callback for the creation of the connection.
     */
    public interface ConnectionCallback {
        /**
         * Called when the channel has been connected.
         * @param channel Channel
         */
        void onConnected(IOOutboundSocketChannel channel);

        /**
         * Called if an exception was thrown when trying to connect.
         * @param e The exception
         */
        void onException(IOException e);
    }
}
