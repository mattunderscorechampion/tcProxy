package com.mattunderscore.tcproxy.selector.task;

import com.mattunderscore.tcproxy.io.IOSocketChannel;

/**
 * The handler for new connections.
 * @author Matt Champion on 06/11/2015
 */
public interface ConnectionHandler {
    /**
     * Handle the completed connection of the socket.
     * @param socket The socket
     */
    void onConnect(IOSocketChannel socket);
}
