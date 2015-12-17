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

/**
 * The available socket options.
 * @author Matt Champion on 13/03/14.
 */
public final class IOSocketOption<T> {
    /**
     * Socket option for SO_RCVBUF.
     */
    public final static IOSocketOption<Integer> RECEIVE_BUFFER = new IOSocketOption<>("RECEIVE_BUFFER");
    /**
     * Socket option for SO_SNDBUF.
     */
    public final static IOSocketOption<Integer> SEND_BUFFER = new IOSocketOption<>("SEND_BUFFER");
    /**
     * Socket option blocking socket.
     */
    public final static IOSocketOption<Boolean> BLOCKING = new IOSocketOption<>("BLOCKING");
    /**
     * Socket option for SO_KEEP_ALIVE.
     */
    public static final IOSocketOption<Boolean> KEEP_ALIVE = new IOSocketOption<>("KEEP_ALIVE");
    /**
     * Socket option for SO_LINGER.
     */
    public static final IOSocketOption<Integer> LINGER = new IOSocketOption<>("LINGER");
    /**
     * Socket option for SO_REUSEADDR.
     */
    public static final IOSocketOption<Boolean> REUSE_ADDRESS = new IOSocketOption<>("REUSE_ADDRESS");
    /**
     * Socket option for TCP_NODELAY.
     */
    public static final IOSocketOption<Boolean> TCP_NO_DELAY = new IOSocketOption<>("TCP_NO_DELAY");

    private final String name;

    private IOSocketOption(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
