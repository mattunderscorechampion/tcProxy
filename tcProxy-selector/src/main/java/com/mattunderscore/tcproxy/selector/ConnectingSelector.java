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

package com.mattunderscore.tcproxy.selector;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOSelector;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.selector.server.SocketConfigurator;
import com.mattunderscore.tcproxy.selector.task.AcceptingTask;
import com.mattunderscore.tcproxy.selector.task.ConnectionHandlerFactory;

/**
 * A selector that accepts and completes the connection of new sockets. A task is registered to accept new connections.
 * If the new connection cannot be completed immediately a task to complete the connection is registered.
 * @author Matt Champion on 06/11/2015
 */
public final class ConnectingSelector implements SocketChannelSelector {
    private final GeneralPurposeSelector selector;

    /*package*/ ConnectingSelector(GeneralPurposeSelector selector) {
        this.selector = selector;
    }

    @Override
    public void register(IOSocketChannel channel, IOSelectionKey.Op op, SelectorRunnable<IOSocketChannel> runnable) {
        selector.register(channel, op, runnable);
    }

    @Override
    public void register(IOSocketChannel channel, Set<IOSelectionKey.Op> ops, SelectorRunnable<IOSocketChannel> runnable) {
        selector.register(channel, ops, runnable);
    }

    @Override
    public void start() {
        selector.start();
    }

    @Override
    public void stop() {
        selector.stop();
    }

    @Override
    public void restart() {
        selector.restart();
    }

    @Override
    public void waitForRunning() {
        selector.waitForRunning();
    }

    @Override
    public void waitForStopped() {
        selector.waitForStopped();
    }

    /**
     * Open a new selector that will accept an complete the connection process.
     * @param ioSelector Selector
     * @param serverSocketChannel Server channel to listen for new connections on
     * @param connectionHandlerFactory Factory handler
     * @param socketConfigurator A configurator for accepted sockets
     * @return A connecting selector
     */
    public static ConnectingSelector open(
            IOSelector ioSelector,
            IOServerSocketChannel serverSocketChannel,
            ConnectionHandlerFactory connectionHandlerFactory,
            SocketConfigurator socketConfigurator) {
        return open(ioSelector, Collections.singleton(serverSocketChannel), connectionHandlerFactory, socketConfigurator);
    }

    /**
     * Open a new selector that will accept an complete the connection process.
     * @param ioSelector Selector
     * @param serverSocketChannels A collection of the server channels to listen for new connections on
     * @param connectionHandlerFactory Factory handler
     * @param socketConfigurator A configurator for accepted sockets
     * @return A connecting selector
     */
    public static ConnectingSelector open(
            IOSelector ioSelector,
            Collection<IOServerSocketChannel> serverSocketChannels,
            ConnectionHandlerFactory connectionHandlerFactory,
            SocketConfigurator socketConfigurator) {
        final GeneralPurposeSelector generalPurposeSelector = new GeneralPurposeSelector(ioSelector);
        final ConnectingSelector selector = new ConnectingSelector(generalPurposeSelector);
        for (final IOServerSocketChannel serverSocketChannel : serverSocketChannels) {
            generalPurposeSelector.register(
                serverSocketChannel,
                new AcceptingTask(selector, connectionHandlerFactory.create(selector), socketConfigurator));
        }
        return selector;
    }
}
