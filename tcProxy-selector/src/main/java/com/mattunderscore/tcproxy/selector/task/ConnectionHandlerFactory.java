package com.mattunderscore.tcproxy.selector.task;

import com.mattunderscore.tcproxy.selector.SocketChannelSelector;

/**
 * Factory for connection handlers.
 * @author Matt Champion on 07/11/2015
 */
public interface ConnectionHandlerFactory {
    /**
     * Creates a connection handler for a selector.
     * @param selector the selector.
     * @return The connection handler
     */
    ConnectionHandler create(SocketChannelSelector selector);
}
