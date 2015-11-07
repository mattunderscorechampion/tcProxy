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

package com.mattunderscore.tcproxy.examples.selector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.EnumSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.CircularBuffer;
import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.io.impl.CircularBufferImpl;
import com.mattunderscore.tcproxy.io.impl.StaticIOFactory;
import com.mattunderscore.tcproxy.selector.ConnectingSelector;
import com.mattunderscore.tcproxy.selector.MultipurposeSelector;
import com.mattunderscore.tcproxy.selector.SelectorRunnable;
import com.mattunderscore.tcproxy.selector.SocketChannelSelector;
import com.mattunderscore.tcproxy.selector.task.ConnectionHandler;
import com.mattunderscore.tcproxy.selector.task.ConnectionHandlerFactory;

/**
 * Added example of using a {@link MultipurposeSelector} as an echo server. Accepts, reads and writes on the main
 * thread.
 * @author Matt Champion on 24/10/2015
 */
public final class EchoServer {
    private static final Logger LOG = LoggerFactory.getLogger("selector");

    public static void main(String[] args) throws IOException {
        final IOServerSocketChannel channel = StaticIOFactory
            .socketFactory(IOServerSocketChannel.class)
            .reuseAddress(true)
            .bind(new InetSocketAddress(34534))
            .blocking(false)
            .create();

        final ConnectionHandlerFactory connectionHandlerFactory = new ConnectionHandlerFactory() {
            @Override
            public ConnectionHandler create(final SocketChannelSelector selector) {
                return new ConnectionHandler() {
                    @Override
                    public void onConnect(IOSocketChannel socket) {
                        selector.register(socket, IOSelectionKey.Op.READ, new EchoTask(selector));
                    }
                };
            }
        };
        final SocketChannelSelector selector = ConnectingSelector.open(StaticIOFactory.openSelector(), channel, connectionHandlerFactory);
        selector.run();
    }

    private static final class EchoTask implements SelectorRunnable<IOSocketChannel> {
        private final CircularBuffer buffer = CircularBufferImpl.allocateDirect(64);
        private final SocketChannelSelector selector;

        private EchoTask(SocketChannelSelector selector) {
            this.selector = selector;
        }

        @Override
        public void run(IOSocketChannel socket, IOSelectionKey selectionKey) {
            final Set<IOSelectionKey.Op> readyOperations = selectionKey.readyOperations();
            LOG.debug("Calling echo task {} {}", socket, readyOperations);
            if (readyOperations.contains(IOSelectionKey.Op.READ)) {

                try {
                    final int read = socket.read(buffer);
                    if (read < 0) {
                        selectionKey.cancel();
                        socket.close();
                        return;
                    }
                }
                catch (IOException e) {
                    LOG.warn("Unable to read from socket", e);
                }
            }

            if (readyOperations.contains(IOSelectionKey.Op.WRITE)) {
                if (buffer.usedCapacity() > 0) {
                    try {
                        socket.write(buffer);
                    }
                    catch (IOException e) {
                        LOG.warn("Unable to write to socket", e);
                    }
                }
            }

            selectionKey.cancel();
            if (buffer.usedCapacity() > 0 && buffer.freeCapacity() > 0) {
                selector.register(socket, EnumSet.of(IOSelectionKey.Op.READ, IOSelectionKey.Op.WRITE), this);
            }
            else if (buffer.freeCapacity() > 0) {
                selector.register(socket, IOSelectionKey.Op.READ, this);
            }
            else {
                selector.register(socket, IOSelectionKey.Op.WRITE, this);
            }
        }
    }
}
