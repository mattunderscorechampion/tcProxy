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

package com.mattunderscore.tcproxy.proxy.selector;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.socket.IOOutboundSocketChannel;
import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;
import com.mattunderscore.tcproxy.proxy.AsynchronousOutboundConnectionFactory;
import com.mattunderscore.tcproxy.proxy.ConnectionImpl;
import com.mattunderscore.tcproxy.proxy.action.processor.ActionProcessorFactory;
import com.mattunderscore.tcproxy.proxy.action.processor.DefaultActionProcessorFactory;
import com.mattunderscore.tcproxy.proxy.action.queue.ActionQueue;
import com.mattunderscore.tcproxy.proxy.action.queue.ActionQueueImpl;
import com.mattunderscore.tcproxy.proxy.connection.Connection;
import com.mattunderscore.tcproxy.proxy.connection.ConnectionManager;
import com.mattunderscore.tcproxy.proxy.direction.Direction;
import com.mattunderscore.tcproxy.proxy.direction.DirectionImpl;
import com.mattunderscore.tcproxy.proxy.settings.ConnectionSettings;
import com.mattunderscore.tcproxy.selector.connecting.ConnectionHandler;
import com.mattunderscore.tcproxy.selector.server.Server;

/**
 * Implementation of {@link ConnectionHandler} for the proxy {@link Server}.
 * @author Matt Champion on 18/11/2015
 */
class ProxyConnectionHandler implements ConnectionHandler {
    private static final Logger LOG = LoggerFactory.getLogger("acceptor");
    private final AsynchronousOutboundConnectionFactory factory;
    private final ConnectionSettings settings;
    private final ConnectionManager manager;
    private final Writer writer;

    public ProxyConnectionHandler(
            AsynchronousOutboundConnectionFactory factory,
            ConnectionSettings settings,
            ConnectionManager manager,
            Writer writer) {
        this.factory = factory;
        this.settings = settings;
        this.manager = manager;
        this.writer = writer;
    }

    @Override
    public void onConnect(final IOSocketChannel clientSide) {
        LOG.info("{} : Accepted {}", this, clientSide);
        factory.createConnection(new AsynchronousOutboundConnectionFactory.ConnectionCallback() {
            @Override
            public void onConnected(IOOutboundSocketChannel serverSide) {
                LOG.info("{} : Opened {}", this, serverSide);
                final ActionQueue actionQueue0 = new ActionQueueImpl(settings.getWriteQueueSize(), settings.getBatchSize());
                final ActionQueue actionQueue1 = new ActionQueueImpl(settings.getWriteQueueSize(), settings.getBatchSize());
                final Direction direction0 = new DirectionImpl(clientSide, serverSide, actionQueue0);
                final Direction direction1 = new DirectionImpl(serverSide, clientSide, actionQueue1);
                final Connection conn = new ConnectionImpl(manager, direction0, direction1);
                final ActionProcessorFactory processorFactory = new DefaultActionProcessorFactory(conn, writer);
                manager.register(conn);
                direction0.chainProcessor(processorFactory);
                direction1.chainProcessor(processorFactory);
            }

            @Override
            public void onException(IOException e) {
                LOG.warn("{} : There was an exception attempting to connect an outbound channel", this, e);
            }
        });
    }
}
