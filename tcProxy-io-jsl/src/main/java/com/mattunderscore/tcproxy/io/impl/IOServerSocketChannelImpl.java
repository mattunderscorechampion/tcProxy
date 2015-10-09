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
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOSelector;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketOption;

/**
 * @author Matt Champion on 13/03/14.
 */
final class IOServerSocketChannelImpl implements IOServerSocketChannel {
    private final ServerSocketChannel socketDelegate;

    IOServerSocketChannelImpl(final ServerSocketChannel socketDelegate) {
        this.socketDelegate = socketDelegate;
    }

    @Override
    public IOSocketChannel accept() throws IOException {
        return new IOSocketChannelImpl(socketDelegate.accept());
    }

    @Override
    public void bind(final SocketAddress localAddress) throws IOException {
        socketDelegate.bind(localAddress);
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        return socketDelegate.getLocalAddress();
    }

    @Override
    public boolean isOpen() {
        return socketDelegate.isOpen();
    }

    @Override
    public void close() throws IOException {
        socketDelegate.close();
    }

    @Override
    public <T> void setOption(final IOSocketOption<T> option, final T value) throws IOException {
        final InternalIOSocketOption<T> optionImpl = IOUtils.convertSocketOption(option);
        optionImpl.apply(socketDelegate, value);
    }

    @Override
    public IOSelectionKey register(IOSelector selector, Object att) throws ClosedChannelException {
        final IOSelectorImpl selectorImpl = (IOSelectorImpl)selector;
        return new IOSelectionKeyImpl(socketDelegate.register(selectorImpl.selectorDelegate, SelectionKey.OP_ACCEPT, att));
    }
}
