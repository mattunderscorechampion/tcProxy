package com.mattunderscore.tcproxy.selector;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOSelector;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketChannel;
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
    public void run() {
        selector.run();
    }

    @Override
    public void stop() {
        selector.stop();
    }

    @Override
    public void waitForRunning() throws InterruptedException {
        selector.waitForRunning();
    }

    @Override
    public void waitForStopped() throws InterruptedException {
        selector.waitForStopped();
    }

    /**
     * Open a new selector that will accept an complete the connection process.
     * @param ioSelector Selector
     * @param serverSocketChannel Server channel to listen for new connections on
     * @param connectionHandlerFactory Factory handler
     * @return A connecting selector
     */
    public static ConnectingSelector open(
            IOSelector ioSelector,
            IOServerSocketChannel serverSocketChannel,
            ConnectionHandlerFactory connectionHandlerFactory) {
        return open(ioSelector, Collections.singleton(serverSocketChannel), connectionHandlerFactory);
    }

    /**
     * Open a new selector that will accept an complete the connection process.
     * @param ioSelector Selector
     * @param serverSocketChannels A collection of the server channels to listen for new connections on
     * @param connectionHandlerFactory Factory handler
     * @return A connecting selector
     */
    public static ConnectingSelector open(
        IOSelector ioSelector,
        Collection<IOServerSocketChannel> serverSocketChannels,
        ConnectionHandlerFactory connectionHandlerFactory) {
        final GeneralPurposeSelector generalPurposeSelector = new GeneralPurposeSelector(ioSelector);
        final ConnectingSelector selector = new ConnectingSelector(generalPurposeSelector);
        for (final IOServerSocketChannel serverSocketChannel : serverSocketChannels) {
            generalPurposeSelector
                .register(serverSocketChannel, new AcceptingTask(selector, connectionHandlerFactory.create(selector)));
        }
        return selector;
    }
}
