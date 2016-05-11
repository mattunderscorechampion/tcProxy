package com.mattunderscore.tcproxy.io.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;

import com.mattunderscore.tcproxy.io.configuration.SocketConfiguration;
import com.mattunderscore.tcproxy.io.factory.IOOutboundSocketChannelFactory;
import com.mattunderscore.tcproxy.io.factory.IOServerSocketChannelFactory;

/**
 * Unit tests for {@link JSLIOFactory}.
 *
 * @author Matt Champion on 11/05/2016
 */
public final class JSLIOFactoryTest {

    private static final JSLIOFactory factory = new JSLIOFactory();

    @Before
    public void setUp() {
        initMocks(this);
    }


    @Test
    public void socketFactoryServerSocketChannelClass() {
        assertNotNull(factory.socketFactory(IOServerSocketChannelFactory.class));
    }

    @Test
    public void socketFactoryOutboundSocketChannelClass() {
        assertNotNull(factory.socketFactory(IOOutboundSocketChannelFactory.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void socketFactoryAbstractOutboundSocketClass() {
        factory.socketFactory(AbstractOutboundSocketFactoryImpl.class);
    }

    @Test
    public void socketFactoryServerSocketChannelConfiguration() {
        assertNotNull(factory.socketFactory(SocketConfiguration.serverSocketChannel()));
    }

    @Test
    public void socketFactoryOutboundSocketChannelConfiguration() {
        assertNotNull(factory.socketFactory(SocketConfiguration.outboundSocketChannel()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void socketFactorSocketChannelClass() {
        factory.socketFactory(SocketConfiguration.socketChannel());
    }
}
