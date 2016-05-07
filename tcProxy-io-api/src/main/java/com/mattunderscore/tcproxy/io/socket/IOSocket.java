/* Copyright © 2014, 2015 Matthew Champion
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

package com.mattunderscore.tcproxy.io.socket;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.Channel;

/**
 * A generic socket.
 * @author Matt Champion on 13/03/14.
 */
public interface IOSocket extends AutoCloseable, Channel {

    /**
     * @return The address of the local socket.
     * @throws IOException If an I/O error occurs
     */
    SocketAddress getLocalAddress() throws IOException;

    /**
     * Sets an option on the socket.
     * @param option The option.
     * @param value The value of the option.
     * @param <T> The type of the value.
     * @throws IOException If an I/O error occurs
     */
    <T> void set(IOSocketOption<T> option, T value) throws IOException;

    /**
     * Get the value of an option for the socket.
     * @param option The option.
     * @param <T> The type of the value.
     * @return The value of the option.
     * @throws IOException If an I/O error occurs
     */
    <T> T get(IOSocketOption<T> option) throws IOException;
}
