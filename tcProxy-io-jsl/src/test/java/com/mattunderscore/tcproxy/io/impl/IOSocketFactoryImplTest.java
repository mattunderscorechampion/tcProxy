package com.mattunderscore.tcproxy.io.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

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
}
