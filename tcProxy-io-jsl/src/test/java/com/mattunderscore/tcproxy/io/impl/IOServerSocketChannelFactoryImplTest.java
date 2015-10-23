package com.mattunderscore.tcproxy.io.impl;

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
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketFactory;
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
        final IOSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory);

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
        final IOSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory);

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
        final IOSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory);

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
        final IOSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory);

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
        final IOSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory);

        final IOServerSocketChannel channel = factory.sendBuffer(1024).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openServerSocket();
        verify(socketChannel).set(IOSocketOption.SEND_BUFFER, 1024);
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createWithKeepAlive() throws IOException {
        final IOSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory);

        factory.keepAlive(true);
    }

    @Test
    public void createWithLinger() throws IOException {
        final IOSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory);

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
        final IOSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory);

        final IOServerSocketChannel channel = factory.reuseAddress(true).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openServerSocket();
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, true);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createWithNoDelay() throws IOException {
        final IOSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory);

        factory.noDelay(true);
    }

    @Test
    public void createBound() throws IOException {
        final IOSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory);

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
        final IOSocketFactory<IOServerSocketChannel> factory = new IOServerSocketChannelFactoryImpl(ioFactory);

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
