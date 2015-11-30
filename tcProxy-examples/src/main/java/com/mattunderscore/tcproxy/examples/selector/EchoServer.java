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

import static com.mattunderscore.tcproxy.io.IOSelectionKey.Op.READ;
import static com.mattunderscore.tcproxy.io.IOSelectionKey.Op.WRITE;
import static com.mattunderscore.tcproxy.io.impl.CircularBufferImpl.allocateDirect;
import static java.util.EnumSet.of;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.CircularBuffer;
import com.mattunderscore.tcproxy.io.IOFactory;
import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.io.impl.JSLIOFactory;
import com.mattunderscore.tcproxy.selector.SelectionRunnable;
import com.mattunderscore.tcproxy.selector.SelectorFactory;
import com.mattunderscore.tcproxy.selector.SocketChannelSelector;
import com.mattunderscore.tcproxy.selector.connecting.ConnectingSelectorFactory;
import com.mattunderscore.tcproxy.selector.connecting.ConnectionHandler;
import com.mattunderscore.tcproxy.selector.connecting.ConnectionHandlerFactory;
import com.mattunderscore.tcproxy.selector.general.GeneralPurposeSelector;
import com.mattunderscore.tcproxy.selector.general.RegistrationHandle;
import com.mattunderscore.tcproxy.selector.server.AbstractServerFactory;
import com.mattunderscore.tcproxy.selector.server.AbstractServerStarter;
import com.mattunderscore.tcproxy.selector.server.AcceptSettings;
import com.mattunderscore.tcproxy.selector.server.Server;
import com.mattunderscore.tcproxy.selector.server.ServerConfig;
import com.mattunderscore.tcproxy.selector.server.ServerStarter;
import com.mattunderscore.tcproxy.selector.server.SocketConfigurator;
import com.mattunderscore.tcproxy.selector.server.SocketSettings;
import com.mattunderscore.tcproxy.selector.threads.RestartableTask;

/**
 * Added example of using a {@link GeneralPurposeSelector} as an echo server. Accepts, reads and writes on the main
 * thread.
 * @author Matt Champion on 24/10/2015
 */
public final class EchoServer {
    private static final Logger LOG = LoggerFactory.getLogger("selector");

    public static void main(String[] args) throws IOException {
        final EchoServerFactory serverFactory = new EchoServerFactory();
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

    private static final class EchoTask implements SelectionRunnable<IOSocketChannel> {
        private final CircularBuffer buffer = allocateDirect(64);
        private final SocketChannelSelector selector;

        private EchoTask(SocketChannelSelector selector) {
            this.selector = selector;
        }

        @Override
        public void run(IOSocketChannel socket, RegistrationHandle handle) {
            final Set<IOSelectionKey.Op> readyOperations = handle.readyOperations();
            LOG.debug("Calling echo task {} {}", socket, readyOperations);
            if (readyOperations.contains(READ)) {

                try {
                    final int read = socket.read(buffer);
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

            if (readyOperations.contains(WRITE)) {
                if (buffer.usedCapacity() > 0) {
                    try {
                        socket.write(buffer);
                    }
                    catch (IOException e) {
                        LOG.warn("Unable to write to socket", e);
                    }
                }
            }

            handle.cancel();
            if (buffer.usedCapacity() > 0 && buffer.freeCapacity() > 0) {
                selector.register(socket, of(READ, WRITE), this);
            }
            else if (buffer.freeCapacity() > 0) {
                selector.register(socket, READ, this);
            }
            else {
                selector.register(socket, WRITE, this);
            }
        }
    }

    private final static class EchoServerStarter extends AbstractServerStarter {
        private final SocketSettings inboundSocketSettings;

        protected EchoServerStarter(IOFactory ioFactory, Iterable<Integer> portsToListenOn, int selectorThreads, SocketSettings inboundSocketSettings) {
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
                            @Override
                            public void onConnect(IOSocketChannel socket) {
                                selector.register(socket, READ, new EchoTask(selector));
                            }
                        };
                    }
                },
                new SocketConfigurator(inboundSocketSettings));
        }
    }

    private final static class EchoServerFactory extends AbstractServerFactory {
        public EchoServerFactory() {
            super(new JSLIOFactory());
        }

        @Override
        protected ServerStarter getServerStarter(ServerConfig serverConfig) {
            return new EchoServerStarter(
                ioFactory,
                serverConfig.getAcceptSettings().getListenOn(),
                serverConfig.getSelectorThreads(),
                serverConfig.getInboundSocketSettings());
        }
    }

    public static Server create(ServerConfig serverConfig) {
        final EchoServerFactory serverFactory = new EchoServerFactory();
        return serverFactory.build(serverConfig);
    }
}
