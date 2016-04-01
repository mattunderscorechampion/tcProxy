package com.mattunderscore.tcproxy.proxy.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.mattunderscore.tcproxy.proxy.direction.Direction;

/**
 * Unit tests for {@link Close}.
 *
 * @author Matt Champion on 01/04/2016
 */
public final class CloseTest {
    @Mock
    private Direction direction;

    @Before
    public void setUp() {
        initMocks(this);
    }


    @Test
    public void writeToSocket() throws IOException {
        final Action action = new Close(direction);

        final int result = action.writeToSocket();

        assertEquals(-1, result);
        verify(direction).close();
        assertTrue(action.writeComplete());
    }

    @Test
    public void writeToSocketTwice() throws IOException {
        final Action action = new Close(direction);

        action.writeToSocket();
        final int result = action.writeToSocket();
        assertEquals(0, result);
    }
}
