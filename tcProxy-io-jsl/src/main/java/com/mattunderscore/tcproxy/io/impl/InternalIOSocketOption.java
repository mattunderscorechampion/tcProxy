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

package com.mattunderscore.tcproxy.io.impl;

import static java.net.StandardSocketOptions.SO_KEEPALIVE;
import static java.net.StandardSocketOptions.SO_LINGER;
import static java.net.StandardSocketOptions.SO_RCVBUF;
import static java.net.StandardSocketOptions.SO_REUSEADDR;
import static java.net.StandardSocketOptions.SO_SNDBUF;
import static java.net.StandardSocketOptions.TCP_NODELAY;

import java.io.IOException;
import java.net.SocketOption;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Socket options that can be applied to {@link SocketChannel} and {@link ServerSocketChannel}.
 * @author Matt Champion on 08/10/2015
 */
interface InternalIOSocketOption<T> {
    /**
     * Socket option for SO_RCVBUF.
     */
    InternalIOSocketOption<Integer> RECEIVE_BUFFER = new BasicOption<>(SO_RCVBUF);
    /**
     * Socket option for SO_SNDBUF.
     */
    InternalIOSocketOption<Integer> SEND_BUFFER = new BasicOption<>(SO_SNDBUF);
    /**
     * Socket option blocking socket.
     */
    InternalIOSocketOption<Boolean> BLOCKING = new InternalIOSocketOption<Boolean>() {
        @Override
        public void apply(Object channel, Boolean value) throws IOException {
            ((SelectableChannel)channel).configureBlocking(value);
        }

        @Override
        public Boolean lookup(Object channel) throws IOException {
            return ((SelectableChannel)channel).isBlocking();
        }

        @Override
        public String toString() {
            return "BLOCKING";
        }
    };
    /**
     * Socket option for SO_KEEP_ALIVE.
     */
    InternalIOSocketOption<Boolean> KEEP_ALIVE = new BasicOption<>(SO_KEEPALIVE);
    /**
     * Socket option for SO_LINGER.
     */
    InternalIOSocketOption<Integer> LINGER = new BasicOption<>(SO_LINGER);
    /**
     * Socket option for SO_REUSEADDR.
     */
    InternalIOSocketOption<Boolean> REUSE_ADDRESS = new BasicOption<>(SO_REUSEADDR);
    /**
     * Socket option for TCP_NODELAY.
     */
    InternalIOSocketOption<Boolean> TCP_NO_DELAY = new BasicOption<>(TCP_NODELAY);

    /**
     * Apply an {@link InternalIOSocketOption} to a channel.
     */
    void apply(Object channel, T value) throws IOException;

    /**
     * Lookup the current socket option of a channel.
     */
    T lookup(Object channel) throws IOException;

    /**
     * Apply an {@link InternalIOSocketOption} as a {@link SocketOption}.
     * @param <T> The type of value the option takes
     */
    final class BasicOption<T> implements InternalIOSocketOption<T> {
        private final SocketOption<T> option;

        private BasicOption(SocketOption<T> option) {
            this.option = option;
        }

        @Override
        public void apply(Object channel, T value) throws IOException {
            ((NetworkChannel)channel).setOption(option, value);
        }

        @Override
        public T lookup(Object channel) throws IOException {
            return ((NetworkChannel)channel).getOption(option);
        }

        @Override
        public String toString() {
            return option.name();
        }
    }
}
