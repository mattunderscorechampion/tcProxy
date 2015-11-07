package com.mattunderscore.tcproxy.selector;

import java.util.Set;

import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOSelector;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.selector.task.AcceptingTask;
import com.mattunderscore.tcproxy.selector.task.ConnectionHandler;
import com.mattunderscore.tcproxy.selector.task.ConnectionHandlerFactory;

/**
 * A selector that accepts and completes the connection of new sockets.
 * @author Matt Champion on 06/11/2015
 */
public final class ConnectingSelector implements Runnable, SocketChannelSelector, StartStopLifecycle {
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

    public static ConnectingSelector open(IOSelector ioSelector, IOServerSocketChannel serverSocketChannel, ConnectionHandlerFactory connectionHandlerFactory) {
        final MultipurposeSelector multipurposeSelector = new MultipurposeSelector(ioSelector);
        final ConnectingSelector selector = new ConnectingSelector(multipurposeSelector);
        multipurposeSelector.register(serverSocketChannel, new AcceptingTask(selector, connectionHandlerFactory.create(selector)));
        return selector;
    }
}
