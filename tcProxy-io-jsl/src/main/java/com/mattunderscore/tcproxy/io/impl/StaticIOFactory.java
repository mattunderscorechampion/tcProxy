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

package com.mattunderscore.tcproxy.io.impl;

import java.io.IOException;

import com.mattunderscore.tcproxy.io.IOSelector;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocket;
import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketFactory;

/**
 * Static implementation of IO factory for convenience.
 * @author Matt Champion on 13/03/14.
 */
public final class StaticIOFactory {
    private static final IOFactoryImpl FACTORY = new IOFactoryImpl();

    private StaticIOFactory() {
    }

    /**
     * @return A new selector.
     * @throws IOException
     */
    public static IOSelector openSelector() throws IOException {
        return FACTORY.openSelector();
    }

    /**
     * @return A new unbound socket.
     * @throws IOException
     */
    public static IOSocketChannel openSocket() throws IOException {
        return FACTORY.openSocket();
    }

    /**
     * @return A new server socket.
     * @throws IOException
     */
    public static IOServerSocketChannel openServerSocket() throws IOException {
        return FACTORY.openServerSocket();
    }

    /**
     * @param type The class of the type of socket
     * @param <T> The type of socket
     * @return A socket factory
     * @throws IllegalArgumentException If no builder is available for the socket type
     */
    public static <T extends IOSocket> IOSocketFactory<T> socketFactory(Class<T> type) {
        return FACTORY.socketFactory(type);
    }
}
