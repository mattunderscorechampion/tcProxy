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

package com.mattunderscore.tcproxy.io.factory;

import java.io.IOException;

import com.mattunderscore.tcproxy.io.socket.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;
import com.mattunderscore.tcproxy.io.configuration.IOSocketChannelConfiguration;

/**
 * @author Matt Champion on 05/12/2015
 */
public final class IOSocketChannelAcceptor {
    private final IOServerSocketChannel serverSocketChannel;
    private final IOSocketChannelConfiguration socketChannelConfiguration;

    /**
     * Constructor.
     * @param serverSocketChannel The {@link IOServerSocketChannel} to accept from
     * @param socketChannelConfiguration The configuration for the accepted socket
     */
    public IOSocketChannelAcceptor(IOServerSocketChannel serverSocketChannel, IOSocketChannelConfiguration socketChannelConfiguration) {
        this.serverSocketChannel = serverSocketChannel;
        this.socketChannelConfiguration = socketChannelConfiguration;
    }

    /**
     * Accept a new {@link IOSocketChannel} from the {@link IOServerSocketChannel}.
     * @return An {@link IOSocketChannel} if the {@link IOServerSocketChannel} is in non-blocking mode it may return
     * null or a socket that has not finished connecting.
     * @throws IOException If the accept or configuration operations failed
     */
    public IOSocketChannel accept() throws IOException {
        final IOSocketChannel ioSocketChannel = serverSocketChannel.accept();
        if (ioSocketChannel != null) {
            socketChannelConfiguration.apply(ioSocketChannel);
        }
        return ioSocketChannel;
    }
}
