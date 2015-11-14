package com.mattunderscore.tcproxy.selector;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.nio.channels.ClosedChannelException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.mattunderscore.tcproxy.io.IOSelector;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;

/**
 * Unit tests for {@link IOServerSocketChannelRegistrationRequest}.
 *
 * @author Matt Champion on 14/11/2015
 */
public final class IOServerSocketChannelRegistrationRequestTest {
    @Mock
    private IOSelector selector;
    @Mock
    private IOServerSocketChannel channel;
    @Mock
    private SelectorRunnable<IOServerSocketChannel> task;

    @Before
    public void setUp() {
        initMocks(this);
    }


    @Test
    public void register() throws ClosedChannelException {
        final RegistrationRequest registrationRequest = new IOServerSocketChannelRegistrationRequest(channel, task);

        registrationRequest.register(selector);

        verify(channel).register(eq(selector), isA(Registration.class));
    }
}
