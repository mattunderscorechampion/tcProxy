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
import java.net.ConnectException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.data.CircularBuffer;
import com.mattunderscore.tcproxy.io.impl.CircularBufferImpl;
import com.mattunderscore.tcproxy.io.socket.IOOutboundSocketChannel;
import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;
import com.mattunderscore.tcproxy.selector.SocketChannelSelector;
import com.mattunderscore.tcproxy.selector.connecting.ConnectionHandler;

/**
 * @author Matt Champion on 15/04/2016
 */
/*package*/ final class SimpleProxyConnectionHandler implements ConnectionHandler {
    private static final Logger LOG = LoggerFactory.getLogger("proxy");
    private final AsynchronousOutboundConnectionFactory factory;
    private final SocketChannelSelector selector;

    public SimpleProxyConnectionHandler(
            AsynchronousOutboundConnectionFactory factory,
            SocketChannelSelector selector) {
        this.factory = factory;
        this.selector = selector;
    }

    @Override
    public void onConnect(final IOSocketChannel clientSide) {
        LOG.info("Accepted {}", this, clientSide);
        factory.createConnection(new AsynchronousOutboundConnectionFactory.ConnectionCallback() {
            @Override
            public void onConnected(IOOutboundSocketChannel serverSide) {
                LOG.info("Opened {}", this, serverSide);

                final CircularBuffer clientToServerBuffer = CircularBufferImpl.allocateDirect(4096);
                final CircularBuffer serverToClientBuffer = CircularBufferImpl.allocateDirect(4096);

                final Direction clientToServer = new Direction(selector, clientToServerBuffer, clientSide, serverSide);
                final Direction serverToClient = new Direction(selector, serverToClientBuffer, serverSide, clientSide);

                clientToServer.registerForRead();
                serverToClient.registerForRead();
            }

            @Override
            public void onException(IOException e) {
                if (e instanceof ConnectException) {
                    LOG.warn("The target server did not accept the outbound connection");
                    try {
                        clientSide.abort();
                    }
                    catch (IOException e1) {
                        LOG.warn("There was an exception attempting to close the inbound connection", e1);
                    }
                }
                else {
                    LOG.warn("There was an exception attempting to connect an outbound channel", e);
                }
            }
        });
    }
}
