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

package com.mattunderscore.tcproxy.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedByInterruptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketOption;
import com.mattunderscore.tcproxy.io.impl.StaticIOFactory;
import com.mattunderscore.tcproxy.proxy.connection.ConnectionFactory;
import com.mattunderscore.tcproxy.proxy.settings.AcceptorSettings;
import com.mattunderscore.tcproxy.proxy.settings.InboundSocketSettings;

/**
 * The acceptor.
 * @author Matt Champion on 18/02/14.
 */
public final class Acceptor implements Runnable {
    public static final Logger LOG = LoggerFactory.getLogger("acceptor");
    private final AcceptorSettings settings;
    private final InboundSocketSettings inboundSettings;
    private final ConnectionFactory connectionFactory;
    private final OutboundSocketFactory factory;
    private volatile boolean running = false;
    private volatile Thread acceptorThread;

    /**
     * Constructor.
     * @param settings The acceptor settings.
     * @param inboundSettings The inbound socket settings.
     * @param connectionFactory The connection factory.
     * @param factory The outbound socket factory.
     */
    public Acceptor(final AcceptorSettings settings,
                    final InboundSocketSettings inboundSettings,
                    final ConnectionFactory connectionFactory,
                    final OutboundSocketFactory factory) {
        this.settings = settings;
        this.inboundSettings = inboundSettings;
        this.connectionFactory = connectionFactory;
        this.factory = factory;
    }

    public void run() {
        LOG.info("{} : Started", this);
        running = true;
        try {
            final IOServerSocketChannel channel = openServerSocket();
            mainLoop(channel);
        }
        catch (final Exception e) {
            running = false;
            LOG.error("{} : Threw an exception it is not able to handle", this, e);
        }
    }

    /**
     * Open a server socket that listens for new connections.
     * @return Bound server socket
     * @throws IOException
     */
    IOServerSocketChannel openServerSocket() throws IOException {
        final IOServerSocketChannel serverSocket = StaticIOFactory.socketFactory(IOServerSocketChannel.class)
            .setSocketOption(IOSocketOption.RECEIVE_BUFFER, inboundSettings.getReceiveBufferSize())
            .create();
        serverSocket.bind(new InetSocketAddress(settings.getPort()));
        return serverSocket;
    }

    /**
     * Loop that accepts new inbound connections.
     * @param channel Server socket
     */
    void mainLoop(final IOServerSocketChannel channel) {
        acceptorThread = Thread.currentThread();
        while (running) {
            try {
                final IOSocketChannel clientSide = channel.accept();
                clientSide.setOption(IOSocketOption.SEND_BUFFER, inboundSettings.getReceiveBufferSize());
                clientSide.setOption(IOSocketOption.BLOCKING, false);
                LOG.info("{} : Accepted {}", this, clientSide);
                final IOSocketChannel serverSide = factory.createSocket();
                LOG.info("{} : Opened {}", this, serverSide);
                connectionFactory.create(clientSide, serverSide);
            }
            catch (final ClosedByInterruptException e) {
                running = false;
            }
            catch (final IOException e) {
                LOG.warn("{} : There was an unhandled exception in the main loop - continuing", this, e);
            }
        }
    }

    @Override
    public String toString() {
        return "Acceptor - " + settings.getPort();
    }

    public void stop() {
        running = false;
        acceptorThread.interrupt();
    }
}
