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

package com.mattunderscore.tcproxy.io.configuration;

import static com.mattunderscore.tcproxy.io.IOSocketOption.BLOCKING;
import static com.mattunderscore.tcproxy.io.IOSocketOption.LINGER;
import static com.mattunderscore.tcproxy.io.IOSocketOption.RECEIVE_BUFFER;
import static com.mattunderscore.tcproxy.io.IOSocketOption.REUSE_ADDRESS;
import static com.mattunderscore.tcproxy.io.IOSocketOption.SEND_BUFFER;

import java.io.IOException;

import com.mattunderscore.tcproxy.io.IOSocket;

/**
 * Abstract implementation of socket configuration that contains the common properties of all sockets.
 * @author Matt Champion on 02/12/2015
 */
public abstract class AbstractIOSocketConfiguration<T extends IOSocket> {
    protected final Integer receiveBuffer;
    protected final Integer sendBuffer;
    protected final boolean blocking;
    protected final Integer linger;
    protected final boolean reuseAddress;

    /**
     * Constructor.
     * @param receiveBuffer The receive buffer size
     * @param sendBuffer The send buffer
     * @param blocking If the socket should be blocking
     * @param linger The linger time
     * @param reuseAddress If the address can be reused without waiting
     */
    protected AbstractIOSocketConfiguration(Integer receiveBuffer, Integer sendBuffer, boolean blocking, Integer linger, boolean reuseAddress) {
        this.receiveBuffer = receiveBuffer;
        this.sendBuffer = sendBuffer;
        this.blocking = blocking;
        this.linger = linger;
        this.reuseAddress = reuseAddress;
    }

    /**
     * Apply the configuration to a socket.
     * @param ioSocket The socket to apply the configuration to
     * @throws IOException If the configuration could not be applied
     */
    public void apply(T ioSocket) throws IOException {
        ioSocket.set(BLOCKING, blocking);
        ioSocket.set(REUSE_ADDRESS, reuseAddress);
        if (receiveBuffer != null) {
            ioSocket.set(RECEIVE_BUFFER, receiveBuffer);
        }
        if (sendBuffer != null) {
            ioSocket.set(SEND_BUFFER, sendBuffer);
        }
        if (linger != null) {
            ioSocket.set(LINGER, linger);
        }
    }
}
