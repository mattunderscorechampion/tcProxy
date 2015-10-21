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
import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketFactory;
import com.mattunderscore.tcproxy.io.IOSocketOption;

public final class IOSocketFactoryImplTest {
    @Mock
    private IOFactory ioFactory;
    @Mock
    private IOSocketChannel socketChannel;
    @Mock
    private SocketAddress address;

    @Before
    public void setUp() throws IOException {
        initMocks(this);

        when(ioFactory.openSocket()).thenReturn(socketChannel);
    }

    @Test
    public void createDefault() throws IOException {
        final IOSocketFactory<IOSocketChannel> factory = new IOSocketFactoryImpl(ioFactory);

        final IOSocketChannel channel = factory.create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openSocket();
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).set(IOSocketOption.KEEP_ALIVE, false);
        verify(socketChannel).set(IOSocketOption.TCP_NO_DELAY, false);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createWithReceiveBuffer() throws IOException {
        final IOSocketFactory<IOSocketChannel> factory = new IOSocketFactoryImpl(ioFactory);

        final IOSocketChannel channel = factory.receiveBuffer(1024).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openSocket();
        verify(socketChannel).set(IOSocketOption.RECEIVE_BUFFER, 1024);
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).set(IOSocketOption.KEEP_ALIVE, false);
        verify(socketChannel).set(IOSocketOption.TCP_NO_DELAY, false);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createNonBlocking() throws IOException {
        final IOSocketFactory<IOSocketChannel> factory = new IOSocketFactoryImpl(ioFactory);

        final IOSocketChannel channel = factory.blocking(false).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openSocket();
        verify(socketChannel).set(IOSocketOption.BLOCKING, false);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).set(IOSocketOption.KEEP_ALIVE, false);
        verify(socketChannel).set(IOSocketOption.TCP_NO_DELAY, false);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createNonBlockingWithReceiveBuffer() throws IOException {
        final IOSocketFactory<IOSocketChannel> factory = new IOSocketFactoryImpl(ioFactory);

        final IOSocketChannel channel = factory.blocking(false).receiveBuffer(1024).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openSocket();
        verify(socketChannel).set(IOSocketOption.RECEIVE_BUFFER, 1024);
        verify(socketChannel).set(IOSocketOption.BLOCKING, false);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).set(IOSocketOption.KEEP_ALIVE, false);
        verify(socketChannel).set(IOSocketOption.TCP_NO_DELAY, false);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createWithSendBuffer() throws IOException {
        final IOSocketFactory<IOSocketChannel> factory = new IOSocketFactoryImpl(ioFactory);

        final IOSocketChannel channel = factory.sendBuffer(1024).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openSocket();
        verify(socketChannel).set(IOSocketOption.SEND_BUFFER, 1024);
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).set(IOSocketOption.KEEP_ALIVE, false);
        verify(socketChannel).set(IOSocketOption.TCP_NO_DELAY, false);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createWithKeepAlive() throws IOException {
        final IOSocketFactory<IOSocketChannel> factory = new IOSocketFactoryImpl(ioFactory);

        final IOSocketChannel channel = factory.keepAlive(true).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openSocket();
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).set(IOSocketOption.KEEP_ALIVE, true);
        verify(socketChannel).set(IOSocketOption.TCP_NO_DELAY, false);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createWithLinger() throws IOException {
        final IOSocketFactory<IOSocketChannel> factory = new IOSocketFactoryImpl(ioFactory);

        final IOSocketChannel channel = factory.linger(1024).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openSocket();
        verify(socketChannel).set(IOSocketOption.LINGER, 1024);
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).set(IOSocketOption.KEEP_ALIVE, false);
        verify(socketChannel).set(IOSocketOption.TCP_NO_DELAY, false);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createWithReuseAddress() throws IOException {
        final IOSocketFactory<IOSocketChannel> factory = new IOSocketFactoryImpl(ioFactory);

        final IOSocketChannel channel = factory.reuseAddress(true).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openSocket();
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, true);
        verify(socketChannel).set(IOSocketOption.KEEP_ALIVE, false);
        verify(socketChannel).set(IOSocketOption.TCP_NO_DELAY, false);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createWithNoDelay() throws IOException {
        final IOSocketFactory<IOSocketChannel> factory = new IOSocketFactoryImpl(ioFactory);

        final IOSocketChannel channel = factory.noDelay(true).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openSocket();
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).set(IOSocketOption.KEEP_ALIVE, false);
        verify(socketChannel).set(IOSocketOption.TCP_NO_DELAY, true);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createBound() throws IOException {
        final IOSocketFactory<IOSocketChannel> factory = new IOSocketFactoryImpl(ioFactory);

        final IOSocketChannel channel = factory.bind(address).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openSocket();
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).set(IOSocketOption.KEEP_ALIVE, false);
        verify(socketChannel).set(IOSocketOption.TCP_NO_DELAY, false);
        verify(socketChannel).bind(address);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }

    @Test
    public void createWithLatestReceiveBuffer() throws IOException {
        final IOSocketFactory<IOSocketChannel> factory = new IOSocketFactoryImpl(ioFactory);

        final IOSocketChannel channel = factory.receiveBuffer(1024).receiveBuffer(2048).create();

        assertEquals(socketChannel, channel);
        verify(ioFactory).openSocket();
        verify(socketChannel).set(IOSocketOption.RECEIVE_BUFFER, 2048);
        verify(socketChannel).set(IOSocketOption.BLOCKING, true);
        verify(socketChannel).set(IOSocketOption.REUSE_ADDRESS, false);
        verify(socketChannel).set(IOSocketOption.KEEP_ALIVE, false);
        verify(socketChannel).set(IOSocketOption.TCP_NO_DELAY, false);
        verify(socketChannel).bind(null);
        verifyNoMoreInteractions(socketChannel, ioFactory);
    }
}
