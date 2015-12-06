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

import static com.mattunderscore.tcproxy.io.configuration.IOServerSocketChannelConfiguration.defaultConfig;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.net.SocketAddress;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.mattunderscore.tcproxy.io.IOFactory;
import com.mattunderscore.tcproxy.io.IOOutboundSocketFactory;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketOption;

public final class IOServerSocketChannelFactoryImplTest {
    @Mock
    private IOFactory ioFactory;
    @Mock
    private IOServerSocketChannel socketChannel;
    @Mock
    private SocketAddress address;

    @Before
    public void setUp() throws IOException {
        initMocks(this);

        when(ioFactory.openServerSocket()).thenReturn(socketChannel);
    }

    @Test
    public void createDefault() throws IOException {
        final IOOutboundSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory, defaultConfig());

        final IOServerSocketChannel channel = factory.create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openServerSocket();
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createWithReceiveBuffer() throws IOException {
        final IOOutboundSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory, defaultConfig());

        final IOServerSocketChannel channel = factory.receiveBuffer(1024).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openServerSocket();
        verify(socketChannel).set(IOSocketOption.RECEIVE_BUFFER, 1024);
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createNonBlocking() throws IOException {
        final IOOutboundSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory, defaultConfig());

        final IOServerSocketChannel channel = factory.blocking(false).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openServerSocket();
        verify(socketChannel).set(IOSocketOption.BLOCKING, false);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createNonBlockingWithReceiveBuffer() throws IOException {
        final IOOutboundSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory, defaultConfig());

        final IOServerSocketChannel channel = factory.blocking(false).receiveBuffer(1024).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openServerSocket();
        verify(socketChannel).set(IOSocketOption.RECEIVE_BUFFER, 1024);
        verify(socketChannel).set(IOSocketOption.BLOCKING, false);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createWithSendBuffer() throws IOException {
        final IOOutboundSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory, defaultConfig());

        final IOServerSocketChannel channel = factory.sendBuffer(1024).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openServerSocket();
        verify(socketChannel).set(IOSocketOption.SEND_BUFFER, 1024);
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createWithLinger() throws IOException {
        final IOOutboundSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory, defaultConfig());

        final IOServerSocketChannel channel = factory.linger(1024).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openServerSocket();
        verify(socketChannel).set(IOSocketOption.LINGER, 1024);
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createWithReuseAddress() throws IOException {
        final IOOutboundSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory, defaultConfig());

        final IOServerSocketChannel channel = factory.reuseAddress(true).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openServerSocket();
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, true);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createBound() throws IOException {
        final IOOutboundSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory, defaultConfig());

        final IOServerSocketChannel channel = factory.bind(address).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openServerSocket();
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).bind(address);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createWithLatestReceiveBuffer() throws IOException {
        final IOOutboundSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory, defaultConfig());

        final IOServerSocketChannel channel = factory.receiveBuffer(1024).receiveBuffer(2048).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openServerSocket();
        verify(socketChannel).set(IOSocketOption.RECEIVE_BUFFER, 2048);
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }
}
