package com.mattunderscore.tcproxy.selector;

import java.util.Set;

import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketChannel;

/**
 * Selector that allows {@link SelectorRunnable}s to be registered to run when a channel is ready for some operation.
 * @author Matt Champion on 31/10/2015
 */
public interface EnhancedSelector {
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

    /**
     * Register accept with the selector.
     * @param channel The channel
     * @param runnable The runnable
     */
    void register(IOServerSocketChannel channel, SelectorRunnable<IOServerSocketChannel> runnable);
}
