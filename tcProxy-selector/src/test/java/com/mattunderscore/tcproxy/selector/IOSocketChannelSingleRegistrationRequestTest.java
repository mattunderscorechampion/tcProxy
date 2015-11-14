package com.mattunderscore.tcproxy.selector;

import static com.mattunderscore.tcproxy.io.IOSelectionKey.Op.READ;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.nio.channels.ClosedChannelException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.mattunderscore.tcproxy.io.IOSelector;
import com.mattunderscore.tcproxy.io.IOSocketChannel;

/**
 * Unit tests for {@link IOSocketChannelSingleRegistrationRequest}.
 *
 * @author Matt Champion on 14/11/2015
 */
public final class IOSocketChannelSingleRegistrationRequestTest {
    @Mock
    private IOSelector selector;
    @Mock
    private IOSocketChannel channel;
    @Mock
    private SelectorRunnable<IOSocketChannel> task;

    @Before
    public void setUp() {
        initMocks(this);
    }


    @Test
    public void register() throws ClosedChannelException {
        final RegistrationRequest registrationRequest =
            new IOSocketChannelSingleRegistrationRequest(channel, READ, task);

        registrationRequest.register(selector);

        verify(channel).register(eq(selector), eq(READ), isA(Registration.class));
    }
}
