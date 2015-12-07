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

package com.mattunderscore.tcproxy.examples.sockets;

import static com.mattunderscore.tcproxy.io.impl.StaticIOFactory.socketFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.mattunderscore.tcproxy.io.CircularBuffer;
import com.mattunderscore.tcproxy.io.IOOutboundSocketChannel;
import com.mattunderscore.tcproxy.io.IOOutboundSocketChannelFactory;
import com.mattunderscore.tcproxy.io.IOOutboundSocketFactory;
import com.mattunderscore.tcproxy.io.IOServerSocketChannelFactory;
import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketChannelAcceptor;
import com.mattunderscore.tcproxy.io.configuration.IOOutboundSocketChannelConfiguration;
import com.mattunderscore.tcproxy.io.configuration.IOServerSocketChannelConfiguration;
import com.mattunderscore.tcproxy.io.configuration.IOSocketChannelConfiguration;
import com.mattunderscore.tcproxy.io.impl.CircularBufferImpl;
import com.mattunderscore.tcproxy.io.impl.StaticIOFactory;

/**
 * Example repeatedly connects a socket to a server. The string "hello" is sent back to the client and the connection is
 * closed. The client prints what it receives to the standard input, waits briefly and opens a new connection.
 * @author Matt Champion on 05/12/2015
 */
public final class HelloExample {
    public static void main(String[] args) throws IOException {
        final Thread acceptingThread = new Thread(
            new AcceptingTask(
                new IOSocketChannelAcceptor(
                    socketFactory(
                        IOServerSocketChannelConfiguration
                            .defaultConfig()
                            .reuseAddress(true)
                            .bind(new InetSocketAddress(8080)))
                        .create(),
                    IOSocketChannelConfiguration.defaultConfig())));
        final Thread connectingThread = new Thread(
            new ConnectingTask(socketFactory(IOOutboundSocketChannelFactory.class)));
        acceptingThread.start();
        connectingThread.start();
    }

    private static final class AcceptingTask implements Runnable {
        private final ByteBuffer hello = ByteBuffer.wrap("hello".getBytes());
        private final IOSocketChannelAcceptor acceptor;
        private volatile boolean running = false;

        private AcceptingTask(IOSocketChannelAcceptor acceptor) {
            this.acceptor = acceptor;
        }

        @Override
        public void run() {
            running = true;
            while (running) {
                try {
                    final IOSocketChannel channel = acceptor.accept();
                    channel.write(hello);
                    channel.close();
                    hello.rewind();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static final class ConnectingTask implements Runnable {
        private final IOOutboundSocketFactory<IOOutboundSocketChannel> factory;
        private final CircularBuffer buffer = CircularBufferImpl.allocateDirect(32);
        private volatile boolean running = false;

        private ConnectingTask(IOOutboundSocketFactory<IOOutboundSocketChannel> factory) {
            this.factory = factory;
        }

        @Override
        public void run() {
            running = true;
            while (running) try {
                final IOOutboundSocketChannel channel = factory.create();
                channel.connect(new InetSocketAddress("localhost", 8080));
                channel.read(buffer);
                if (buffer.usedCapacity() != 5) {
                    channel.read(buffer);
                }
                final byte[] bytes = new byte[buffer.usedCapacity()];
                for (int i = 0; i < bytes.length; i++) {
                    bytes[i] = buffer.get();
                }
                System.out.println(new String(bytes));
                channel.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
