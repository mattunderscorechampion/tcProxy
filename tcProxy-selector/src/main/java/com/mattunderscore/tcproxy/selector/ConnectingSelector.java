package com.mattunderscore.tcproxy.selector;

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
    private final MultipurposeSelector selector;

    private ConnectingSelector(MultipurposeSelector selector) {
        this.selector = selector;;
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
        final MultipurposeSelector multipurposeSelector = new MultipurposeSelector(ioSelector);
        final ConnectingSelector selector = new ConnectingSelector(multipurposeSelector);
        multipurposeSelector.register(serverSocketChannel, new AcceptingTask(selector, connectionHandlerFactory.create(selector)));
        return selector;
    }
}
