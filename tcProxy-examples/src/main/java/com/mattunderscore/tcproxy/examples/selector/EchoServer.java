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
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketOption;
import com.mattunderscore.tcproxy.io.impl.StaticIOFactory;
import com.mattunderscore.tcproxy.selector.MultipurposeSelector;
import com.mattunderscore.tcproxy.selector.SelectorRunnable;
import com.mattunderscore.tcproxy.selector.ServerSelectorRunnable;

/**
 * Added example of using a {@link MultipurposeSelector} as an echo server. Accepts, reads and writes on the main
 * thread.
 * @author Matt Champion on 24/10/2015
 */
public final class EchoServer {
    private static final Logger LOG = LoggerFactory.getLogger("selector");

    public static void main(String[] args) throws IOException {
        final MultipurposeSelector selector = new MultipurposeSelector(
            LOG,
            StaticIOFactory.openSelector());
        final IOServerSocketChannel channel = StaticIOFactory
            .socketFactory(IOServerSocketChannel.class)
            .reuseAddress(true)
            .bind(new InetSocketAddress(34534))
            .blocking(false)
            .create();

        selector.register(channel, new AcceptingTask(selector));
        selector.run();
    }

    private static class AcceptingTask implements ServerSelectorRunnable {
        private final MultipurposeSelector selector;

        public AcceptingTask(MultipurposeSelector selector) {
            this.selector = selector;
        }

        @Override
        public void run(IOServerSocketChannel socket, Set<IOSelectionKey.Op> readyOperations) {
            if (readyOperations.contains(IOSelectionKey.Op.ACCEPT)) {
                final IOSocketChannel channel;
                try {
                    channel = socket.accept();
                }
                catch (IOException e) {
                    LOG.warn("Unable to connect socket", e);
                    return;
                }

                try {
                    if (channel != null) {
                        channel.set(IOSocketOption.BLOCKING, false);
                        if (channel.finishConnect()) {
                            selector.register(channel, IOSelectionKey.Op.READ, new EchoTask(selector));
                        }
                        else {
                            selector.register(channel, IOSelectionKey.Op.CONNECT, new ConnectingTask(selector));
                        }
                    }
                }
                catch (IOException e) {
                    LOG.warn("Unable to connect socket", e);
                }
            }
        }
    }

    private static final class ConnectingTask implements SelectorRunnable {
        private final MultipurposeSelector selector;

        public ConnectingTask(MultipurposeSelector selector) {
            this.selector = selector;
        }

        @Override
        public void run(IOSocketChannel socket, Set<IOSelectionKey.Op> readyOperations) {
            try {
                if (readyOperations.contains(IOSelectionKey.Op.CONNECT)) {
                    if (socket.finishConnect()) {
                        selector.register(socket, IOSelectionKey.Op.READ, new EchoTask(selector));
                    }
                }
            }
            catch (IOException e) {
                LOG.warn("Unable to connect socket", e);
            }
        }
    }

    private static final class EchoTask implements SelectorRunnable {
        private final ByteBuffer buffer = ByteBuffer.allocate(64);
        private final MultipurposeSelector selector;
        private boolean reading = true;

        private EchoTask(MultipurposeSelector selector) {
            this.selector = selector;
        }

        @Override
        public void run(IOSocketChannel socket, Set<IOSelectionKey.Op> readyOperations) {
            final Set<IOSelectionKey.Op> ops = EnumSet.of(IOSelectionKey.Op.READ);
            if (readyOperations.contains(IOSelectionKey.Op.READ)) {
                if (!reading) {
                    buffer.flip();
                    reading = true;
                }

                try {
                    final int read = socket.read(buffer);
                    if (read > 0) {
                        ops.add(IOSelectionKey.Op.WRITE);
                    }
                    else if (read < 0) {
                        socket.close();
                        return;
                    }
                }
                catch (IOException e) {
                    LOG.warn("Unable to read from socket", e);
                }
            }

            if (readyOperations.contains(IOSelectionKey.Op.WRITE)) {
                if (reading) {
                    buffer.flip();
                    reading = false;
                }

                if (buffer.hasRemaining()) {
                    try {
                        socket.write(buffer);
                        if (buffer.hasRemaining()) {
                            ops.add(IOSelectionKey.Op.WRITE);
                        }
                    }
                    catch (IOException e) {
                        LOG.warn("Unable to write to socket", e);
                    }
                }
            }

            selector.register(socket, ops, this);
        }
    }
}
