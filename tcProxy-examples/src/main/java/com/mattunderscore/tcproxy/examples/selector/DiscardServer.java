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

import com.mattunderscore.tcproxy.io.factory.IOFactory;
import com.mattunderscore.tcproxy.io.impl.JSLIOFactory;
import com.mattunderscore.tcproxy.io.selection.IOSelectionKey;
import com.mattunderscore.tcproxy.io.socket.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;
import com.mattunderscore.tcproxy.selector.SelectionRunnable;
import com.mattunderscore.tcproxy.selector.SelectorFactory;
import com.mattunderscore.tcproxy.selector.SocketChannelSelector;
import com.mattunderscore.tcproxy.selector.connecting.ConnectingSelectorFactory;
import com.mattunderscore.tcproxy.selector.connecting.ConnectionHandler;
import com.mattunderscore.tcproxy.selector.connecting.ConnectionHandlerFactory;
import com.mattunderscore.tcproxy.selector.general.RegistrationHandle;
import com.mattunderscore.tcproxy.selector.server.*;
import com.mattunderscore.tcproxy.selector.threads.RestartableTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Set;

import static com.mattunderscore.tcproxy.io.selection.IOSelectionKey.Op.READ;

/**
 * Discard server. Reads and then discards all bytes sent to it.
 * @author Matt Champion on 19/12/15
 */
public final class DiscardServer {
    private static final Logger LOG = LoggerFactory.getLogger("selector");

    public static void main(String[] args) throws IOException {
        final DiscardServerFactory serverFactory = new DiscardServerFactory();
        final Server server = serverFactory.build(
                ServerConfig
                        .builder()
                        .selectorThreads(2)
                        .inboundSocketSettings(
                                SocketSettings
                                        .builder()
                                        .receiveBuffer(1024)
                                        .sendBuffer(1024)
                                        .build())
                        .acceptSettings(
                                AcceptSettings
                                        .builder()
                                        .listenOn(34534)
                                        .build())
                        .build());

        server.start();
        server.waitForStopped();
    }

    private static final class DiscardTask implements SelectionRunnable<IOSocketChannel> {
        private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

        @Override
        public void run(IOSocketChannel socket, RegistrationHandle handle) {
            final Set<IOSelectionKey.Op> readyOperations = handle.readyOperations();
            LOG.debug("Calling echo task {} {}", socket, readyOperations);
            if (readyOperations.contains(READ)) {

                try {
                    final int read = socket.read(buffer);
                    buffer.flip();
                    final byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    System.out.write(bytes);
                    buffer.clear();
                    if (read < 0) {
                        handle.cancel();
                        socket.close();
                        return;
                    }
                }
                catch (IOException e) {
                    LOG.warn("Unable to read from socket", e);
                }
            }
        }
    }

    private final static class DiscardServerStarter extends AbstractServerStarter {
        private final SocketSettings inboundSocketSettings;

        protected DiscardServerStarter(IOFactory ioFactory, Iterable<Integer> portsToListenOn, int selectorThreads, SocketSettings inboundSocketSettings) {
            super(ioFactory, portsToListenOn, selectorThreads);
            this.inboundSocketSettings = inboundSocketSettings;
        }

        @Override
        protected SelectorFactory<? extends RestartableTask> getSelectorFactory(Collection<IOServerSocketChannel> listenChannels) {
            return new ConnectingSelectorFactory(
                    ioFactory,
                    listenChannels,
                    new ConnectionHandlerFactory() {
                        @Override
                        public ConnectionHandler create(final SocketChannelSelector selector) {
                            return new ConnectionHandler() {
                                private final DiscardTask discard = new DiscardTask();

                                @Override
                                public void onConnect(IOSocketChannel socket) {
                                    selector.register(socket, READ, discard);
                                }
                            };
                        }
                    },
                    new SocketConfigurator(inboundSocketSettings));
        }
    }

    private final static class DiscardServerFactory extends AbstractServerFactory {
        public DiscardServerFactory() {
            super(new JSLIOFactory());
        }

        @Override
        protected ServerStarter getServerStarter(ServerConfig serverConfig) {
            return new DiscardServerStarter(
                    ioFactory,
                    serverConfig.getAcceptSettings().getListenOn(),
                    serverConfig.getSelectorThreads(),
                    serverConfig.getInboundSocketSettings());
        }
    }

    public static Server create(ServerConfig serverConfig) {
        final DiscardServerFactory serverFactory = new DiscardServerFactory();
        return serverFactory.build(serverConfig);
    }
}
