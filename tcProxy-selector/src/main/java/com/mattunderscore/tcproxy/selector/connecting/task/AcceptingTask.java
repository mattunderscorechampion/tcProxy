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

package com.mattunderscore.tcproxy.selector.connecting.task;

import static com.mattunderscore.tcproxy.io.selection.IOSelectionKey.Op.CONNECT;
import static com.mattunderscore.tcproxy.io.socket.IOSocketOption.BLOCKING;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.socket.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;
import com.mattunderscore.tcproxy.selector.SelectionRunnable;
import com.mattunderscore.tcproxy.selector.SocketChannelSelector;
import com.mattunderscore.tcproxy.selector.connecting.ConnectionHandler;
import com.mattunderscore.tcproxy.selector.general.RegistrationHandle;
import com.mattunderscore.tcproxy.selector.server.SocketConfigurator;

/**
 * A task that accepts and completes socket connections.
 * @author Matt Champion on 06/11/2015
 */
public final class AcceptingTask implements SelectionRunnable<IOServerSocketChannel> {
    private static final Logger LOG = LoggerFactory.getLogger("accept");
    private final SocketChannelSelector selector;
    private final ConnectionHandler connectionHandler;
    private final SocketConfigurator socketConfigurator;

    public AcceptingTask(SocketChannelSelector selector, ConnectionHandler connectionHandler, SocketConfigurator socketConfigurator) {
        this.selector = selector;
        this.connectionHandler = connectionHandler;
        this.socketConfigurator = socketConfigurator;
    }

    @Override
    public void run(IOServerSocketChannel socket, RegistrationHandle handle) {
        LOG.debug("Calling accepting task {} {}", socket, handle.readyOperations());
        if (handle.isAcceptable()) {
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
                    socketConfigurator
                        .apply(channel)
                        .set(BLOCKING, false);
                    if (channel.finishConnect()) {
                        connectionHandler.onConnect(channel);
                    }
                    else {
                        selector.register(channel, CONNECT, new ConnectingTask(connectionHandler));
                    }
                }
            }
            catch (IOException e) {
                LOG.warn("Unable to connect socket", e);
            }
        }
    }
}
