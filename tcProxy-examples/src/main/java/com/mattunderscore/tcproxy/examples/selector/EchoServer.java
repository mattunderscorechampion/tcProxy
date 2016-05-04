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

import static com.mattunderscore.tcproxy.io.impl.CircularBufferImpl.allocateDirect;
import static com.mattunderscore.tcproxy.io.selection.IOSelectionKey.Op.READ;
import static com.mattunderscore.tcproxy.io.selection.IOSelectionKey.Op.WRITE;
import static java.util.EnumSet.of;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import com.mattunderscore.tcproxy.selector.server.AbstractServerBuilder;
import com.mattunderscore.tcproxy.selector.server.ServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.configuration.IOSocketChannelConfiguration;
import com.mattunderscore.tcproxy.io.configuration.IOSocketConfiguration;
import com.mattunderscore.tcproxy.io.data.CircularBuffer;
import com.mattunderscore.tcproxy.io.factory.IOFactory;
import com.mattunderscore.tcproxy.io.impl.JSLIOFactory;
import com.mattunderscore.tcproxy.io.selection.IOSelectionKey;
import com.mattunderscore.tcproxy.io.socket.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;
import com.mattunderscore.tcproxy.selector.NoBackoff;
import com.mattunderscore.tcproxy.selector.SelectionRunnable;
import com.mattunderscore.tcproxy.selector.SelectorFactory;
import com.mattunderscore.tcproxy.selector.SocketChannelSelector;
import com.mattunderscore.tcproxy.selector.connecting.ConnectionHandler;
import com.mattunderscore.tcproxy.selector.connecting.task.AcceptingTask;
import com.mattunderscore.tcproxy.selector.general.GeneralPurposeSelector;
import com.mattunderscore.tcproxy.selector.general.RegistrationHandle;
import com.mattunderscore.tcproxy.selector.server.AbstractServerStarter;
import com.mattunderscore.tcproxy.selector.server.AcceptSettings;
import com.mattunderscore.tcproxy.selector.server.Server;
import com.mattunderscore.tcproxy.workers.WorkerRunnable;

/**
 * Added example of using a {@link GeneralPurposeSelector} as an echo server. Accepts, reads and writes on the main
 * thread.
 * @author Matt Champion on 24/10/2015
 */
public final class EchoServer {
    private static final Logger LOG = LoggerFactory.getLogger("selector");

    public static void main(String[] args) throws IOException {
        final Server server = EchoServer
            .builder()
            .selectorThreads(2)
            .socketSettings(IOSocketChannelConfiguration
                .defaultConfig()
                .receiveBuffer(1024)
                .sendBuffer(1024))
            .acceptSettings(AcceptSettings
                .builder()
                .listenOn(34534)
                .build())
            .build();

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
                try {
                    socket.write(buffer);
                }
                catch (IOException e) {
                    LOG.warn("Unable to write to socket", e);
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
        private final IOSocketConfiguration<IOSocketChannel> inboundSocketSettings;

        protected EchoServerStarter(
                IOFactory ioFactory,
                Iterable<Integer> portsToListenOn,
                int selectorThreads,
                IOSocketConfiguration<IOSocketChannel> inboundSocketSettings) {
            super(ioFactory, portsToListenOn, selectorThreads);
            this.inboundSocketSettings = inboundSocketSettings;
        }

        @Override
        protected SelectorFactory<? extends WorkerRunnable> getSelectorFactory(final Collection<IOServerSocketChannel> listenChannels) {
            return new SelectorFactory<SocketChannelSelector>() {
                @Override
                public SocketChannelSelector create() throws IOException {
                    final GeneralPurposeSelector selector =
                        new GeneralPurposeSelector(ioFactory.openSelector(), NoBackoff.get());

                    for (final IOServerSocketChannel serverSocketChannel : listenChannels) {
                        selector.register(
                            serverSocketChannel,
                            new AcceptingTask(
                                selector,
                                new ConnectionHandler() {
                                    @Override
                                    public void onConnect(IOSocketChannel socket) {
                                        selector.register(socket, READ, new EchoTask(selector));
                                    }
                                },
                                inboundSocketSettings));
                    }

                    return selector;
                }
            };
        }
    }

    public final static class EchoServerBuilder extends AbstractServerBuilder<EchoServerBuilder> {
        private final int selectorThreads;

        private EchoServerBuilder(AcceptSettings acceptSettings, IOSocketChannelConfiguration socketSettings, int selectorThreads) {
            super(acceptSettings, socketSettings);
            this.selectorThreads = selectorThreads;
        }

        @Override
        public Server build() {
            return new ServerImpl(
                    new EchoServerStarter(
                            new JSLIOFactory(),
                            acceptSettings.getListenOn(),
                            1,
                            socketSettings));
        }

        public EchoServerBuilder selectorThreads(int selectorThreads) {
            return new EchoServerBuilder(acceptSettings, socketSettings, selectorThreads);
        }

        @Override
        protected EchoServerBuilder newServerBuilder(AcceptSettings acceptSettings, IOSocketChannelConfiguration socketSettings) {
            return new EchoServerBuilder(acceptSettings, socketSettings, selectorThreads);
        }
    }

    public static final EchoServerBuilder builder() {
        return new EchoServerBuilder(
            AcceptSettings.builder().build(),
            IOSocketChannelConfiguration.defaultConfig(),
            1);
    }
}
