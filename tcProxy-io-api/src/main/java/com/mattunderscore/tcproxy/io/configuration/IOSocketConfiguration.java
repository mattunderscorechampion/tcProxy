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

import java.io.IOException;

import com.mattunderscore.tcproxy.io.socket.IOSocket;

/**
 * Represents configuration for a socket.
 * @author Matt Champion on 05/12/2015
 */
public interface IOSocketConfiguration<T extends IOSocket, S extends IOSocketConfiguration<T, S>> {
    /**
     * Apply the configuration to a socket.
     * @param ioSocket The socket to apply the configuration to
     * @return The socket with the configuration applied
     * @throws IOException If the configuration could not be applied
     */
    T apply(T ioSocket) throws IOException;

    /**
     * Set the socket option for SO_RCVBUF. Defaults to null.
     *
     * @param size The size of the buffer or null to use the system default
     * @return A new factory with the option set
     */
    S receiveBuffer(Integer size);

    /**
     * Set the socket option for SO_SNDBUF. Defaults to null.
     *
     * @param size The size of the buffer or null to use the system default
     * @return A new factory with the option set
     */
    S sendBuffer(Integer size);

    /**
     * Set the socket to blocking mode. Defaults to false.
     *
     * @param enabled Enable the option
     * @return A new factory with the option set
     */
    S blocking(boolean enabled);

    /**
     * Set the socket option for SO_LINGER. Defaults to null.
     *
     * @param time The linger time or null to use the system default
     * @return A new factory with the option set
     */
    S linger(Integer time);

    /**
     * Set the socket option for SO_REUSEADDR. Defaults to false.
     *
     * @param enabled Enable the option
     * @return A new factory with the option set
     */
    S reuseAddress(boolean enabled);
}
