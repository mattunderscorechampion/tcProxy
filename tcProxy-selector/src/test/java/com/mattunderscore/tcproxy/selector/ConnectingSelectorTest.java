package com.mattunderscore.tcproxy.selector;

import static com.mattunderscore.tcproxy.selector.ConnectingSelector.open;
import static java.util.Collections.addAll;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.nio.channels.ClosedChannelException;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.mattunderscore.tcproxy.io.IOSelector;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.selector.task.ConnectionHandlerFactory;

/**
 * Unit tests for {@link ConnectingSelector}.
 * @author Matt Champion
 */
public final class ConnectingSelectorTest {
    @Mock
    private IOSelector ioSelector;
    @Mock
    private IOServerSocketChannel serverSocketChannel0;
    @Mock
    private IOServerSocketChannel serverSocketChannel1;
    @Mock
    private ConnectionHandlerFactory connectionHandlerFactory;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void creationManyChannels() throws ClosedChannelException {
        final Collection<IOServerSocketChannel> channels = new HashSet<>();
        addAll(channels, serverSocketChannel0, serverSocketChannel1);
        open(ioSelector, channels, connectionHandlerFactory);

        verify(serverSocketChannel0).register(ioSelector, isA(SelectorRunnable.class));
        verify(serverSocketChannel1).register(ioSelector, isA(SelectorRunnable.class));
    }

    @Test
    public void creationSingleChannel() throws ClosedChannelException {
        open(ioSelector, serverSocketChannel0, connectionHandlerFactory);

        verify(serverSocketChannel0).register(ioSelector, isA(SelectorRunnable.class));
    }
}
