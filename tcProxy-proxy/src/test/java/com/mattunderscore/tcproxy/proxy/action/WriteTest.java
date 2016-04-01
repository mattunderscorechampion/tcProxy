package com.mattunderscore.tcproxy.proxy.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.mattunderscore.tcproxy.proxy.direction.Direction;

/**
 * Unit tests for {@link Write}.
 *
 * @author Matt Champion on 01/04/2016
 */
public final class WriteTest {
    @Mock
    private Direction direction;

    private ByteBuffer buffer;

    @Before
    public void setUp() {
        initMocks(this);
        buffer = ByteBuffer.allocate(1);
    }


    @Test
    public void writeToSocket() throws IOException {
        final Action action = new Write(direction, buffer);

        final int result = action.writeToSocket();

        verify(direction).write(buffer);
    }

    @Test
    public void writeComplete() {
        buffer.position(1);
        final Action action = new Write(direction, buffer);
        assertTrue(action.writeComplete());
    }

    @Test
    public void writeIncomplete() {
        final Action action = new Write(direction, buffer);
        assertFalse(action.writeComplete());
    }

    @Test
    public void getData() {
        final Write action = new Write(direction, buffer);
        assertEquals(buffer, action.getData());
    }

    @Test
    public void getDirection() {
        final Write action = new Write(direction, buffer);
        assertEquals(direction, action.getDirection());
    }
}
