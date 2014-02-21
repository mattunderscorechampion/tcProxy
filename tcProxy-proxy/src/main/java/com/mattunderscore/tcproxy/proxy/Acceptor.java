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

import com.mattunderscore.tcproxy.proxy.com.mattunderscore.tcproxy.settings.AcceptorSettings;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;

/**
 * @author matt on 18/02/14.
 */
public class Acceptor implements Runnable {
    private final AcceptorSettings settings;
    private final ConnectionFactory connectionFactory;
    private final OutboundSocketFactory factory;
    private final Queue<Connection> newConnections;
    private volatile boolean running = false;

    public Acceptor(final AcceptorSettings settings, final ConnectionFactory connectionFactory,
                    final OutboundSocketFactory factory, final Queue<Connection> newConnections) {
        this.settings = settings;
        this.connectionFactory = connectionFactory;
        this.factory = factory;
        this.newConnections = newConnections;
    }

    public ServerSocketChannel openServerSocket() throws IOException {
        final ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(settings.getPort()));
        return serverSocket;
    }

    public void mainLoop(final ServerSocketChannel channel) {
        while (running) {
            try {
                final SocketChannel clientSide = channel.accept();
                clientSide.configureBlocking(false);
                System.out.println("Accepted " + clientSide);
                final SocketChannel serverSide = factory.createSocket();
                System.out.println("Opened " + serverSide);
                newConnections.add(connectionFactory.create(clientSide, serverSide));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        try {
            running = true;
            final ServerSocketChannel channel = openServerSocket();
            mainLoop(channel);
        }
        catch (final Throwable e) {
            e.printStackTrace();
            running = false;
        }
    }
}
