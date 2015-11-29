/* Copyright © 2014 Matthew Champion
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

package com.mattunderscore.tcproxy.io;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;
import java.util.Set;

/**
 * Provides a selectable {@link java.nio.channels.ByteChannel} for network operations.
 * @author matt on 12/03/14.
 */
public interface IOSocketChannel extends ByteChannel, IOSocket {

    /**
     * Connect the socket to a remote address.
     * @param remoteAddress The remote address to connect to
     * @return If the connect has been established
     * @throws IOException
     */
    boolean connect(SocketAddress remoteAddress) throws IOException;

    /**
     * Finish the connection.
     * @return If the connect was completed
     * @throws IOException
     */
    boolean finishConnect() throws IOException;

    /**
     * @return The address of the remote socket.
     * @throws IOException
     */
    SocketAddress getRemoteAddress() throws IOException;

    /**
     * Register the channel with a selector.
     * @param selector The selector
     * @param op The operation
     * @param att A attachment
     * @return The selection key
     * @throws ClosedChannelException
     */
    IOSelectionKey register(IOSelector selector, IOSelectionKey.Op op, Object att) throws ClosedChannelException;

    /**
     * Register the channel with a selector.
     * @param selector The selector
     * @param ops The operations
     * @param att A attachment
     * @return The selection key
     * @throws ClosedChannelException
     */
    IOSelectionKey register(IOSelector selector, Set<IOSelectionKey.Op> ops, Object att) throws ClosedChannelException;

    /**
     * Lookup the key for a channel/selector pair.
     * @param selector The selector
     * @return The key
     */
    IOSelectionKey keyFor(IOSelector selector);

    /**
     * Reads data from the socket into a circular buffer
     * @param dst The buffer
     * @return The number of bytes read
     * @throws IOException
     */
    int read(CircularBuffer dst) throws IOException;

    /**
     * Writes data from the circular buffer to the socket.
     * @param src The buffer
     * @return The number of bytes written
     * @throws IOException
     */
    int write(CircularBuffer src) throws IOException;
}
