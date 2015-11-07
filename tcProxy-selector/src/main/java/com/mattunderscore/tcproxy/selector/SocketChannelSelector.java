package com.mattunderscore.tcproxy.selector;

import java.util.Set;

import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOSocketChannel;

/**
 * @author Matt Champion on 06/11/2015
 */
public interface SocketChannelSelector extends StartStopLifecycle {
    /**
     * Register operations with the selector.
     * @param channel The channel
     * @param op The operation
     * @param runnable The runnable
     */
    void register(IOSocketChannel channel, IOSelectionKey.Op op, SelectorRunnable<IOSocketChannel> runnable);

    /**
     * Register operations with the selector.
     * @param channel The channel
     * @param ops The operations
     * @param runnable The runnable
     */
    void register(IOSocketChannel channel, Set<IOSelectionKey.Op> ops, SelectorRunnable<IOSocketChannel> runnable);
}
