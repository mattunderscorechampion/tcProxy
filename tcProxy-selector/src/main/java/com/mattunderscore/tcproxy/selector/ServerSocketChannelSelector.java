package com.mattunderscore.tcproxy.selector;

import com.mattunderscore.tcproxy.io.IOServerSocketChannel;

/**
 * @author Matt Champion on 06/11/2015
 */
public interface ServerSocketChannelSelector extends StartStopLifecycle {
    /**
     * Register accept with the selector.
     * @param channel The channel
     * @param runnable The runnable
     */
    void register(IOServerSocketChannel channel, SelectorRunnable<IOServerSocketChannel> runnable);
}
