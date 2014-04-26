/* Copyright © 2014 Matthew Champion
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

package com.mattunderscore.tcproxy.proxy.action.queue;

import com.mattunderscore.tcproxy.proxy.Connection;
import com.mattunderscore.tcproxy.proxy.Direction;
import com.mattunderscore.tcproxy.proxy.action.Action;
import com.mattunderscore.tcproxy.proxy.action.IWrite;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author matt on 21/04/14.
 */
public final class ActionQueueImplTest {
    @Mock
    private Direction direction;
    @Mock
    private Connection connection;
    @Mock
    private IWrite write0;
    @Mock
    private IWrite write1;
    @Mock
    private IWrite write2;
    @Captor
    private ArgumentCaptor<ByteBuffer> outBufferCaptor;

    @Before
    public void setUp() {
        initMocks(this);
        when(write0.getDirection()).thenReturn(direction);
        when(write1.getDirection()).thenReturn(direction);
        when(write2.getDirection()).thenReturn(direction);
        when(write0.isBatchable()).thenReturn(Boolean.TRUE);
        when(write1.isBatchable()).thenReturn(Boolean.TRUE);
        when(write2.isBatchable()).thenReturn(Boolean.TRUE);
        final ByteBuffer buffer0 = ByteBuffer.allocate(64);
        buffer0.put("Hello ".getBytes());
        buffer0.flip();
        when(write0.getData()).thenReturn(buffer0);
        final ByteBuffer buffer1 = ByteBuffer.allocate(64);
        buffer1.put("world".getBytes());
        buffer1.flip();
        when(write1.getData()).thenReturn(buffer1);
        final ByteBuffer buffer2 = ByteBuffer.allocate(64);
        buffer2.put(", baby".getBytes());
        buffer2.flip();
        when(write2.getData()).thenReturn(buffer2);
    }

    @Test
    public void batch() throws IOException {
        final ActionQueueImpl queue = new ActionQueueImpl(direction, connection, 5, 64);
        queue.add(write0);
        queue.add(write1);
        queue.add(write2);
        final Action action = queue.current();
        final Action duplicateAction = queue.current();
        assertSame(action, duplicateAction);
        assertTrue(queue.hasData());
        action.writeToSocket();
        verify(direction).write(outBufferCaptor.capture());
        final ByteBuffer outBuffer = outBufferCaptor.getValue();
        assertEquals(17, outBuffer.limit());
        assertEquals('H', outBuffer.get());
        assertEquals('e', outBuffer.get());
        assertEquals('l', outBuffer.get());
        assertEquals('l', outBuffer.get());
        assertEquals('o', outBuffer.get());
        assertEquals(' ', outBuffer.get());
        assertEquals('w', outBuffer.get());
        assertEquals('o', outBuffer.get());
        assertEquals('r', outBuffer.get());
        assertEquals('l', outBuffer.get());
        assertEquals('d', outBuffer.get());
        assertEquals(',', outBuffer.get());
        assertEquals(' ', outBuffer.get());
        assertEquals('b', outBuffer.get());
        assertEquals('a', outBuffer.get());
        assertEquals('b', outBuffer.get());
        assertEquals('y', outBuffer.get());
        assertFalse(queue.hasData());
        assertNull(queue.current());
    }

    @Test
    public void full() throws IOException {
        final ActionQueueImpl queue = new ActionQueueImpl(direction, connection, 3, 64);
        queue.add(write0);
        queue.add(write1);
        queue.add(write2);
        final Action action = queue.current();
        final Action duplicateAction = queue.current();
        assertSame(action, duplicateAction);
        assertTrue(queue.hasData());
        action.writeToSocket();
        verify(direction).write(outBufferCaptor.capture());
        final ByteBuffer outBuffer = outBufferCaptor.getValue();
        assertEquals(17, outBuffer.limit());
        assertEquals('H', outBuffer.get());
        assertEquals('e', outBuffer.get());
        assertEquals('l', outBuffer.get());
        assertEquals('l', outBuffer.get());
        assertEquals('o', outBuffer.get());
        assertEquals(' ', outBuffer.get());
        assertEquals('w', outBuffer.get());
        assertEquals('o', outBuffer.get());
        assertEquals('r', outBuffer.get());
        assertEquals('l', outBuffer.get());
        assertEquals('d', outBuffer.get());
        assertEquals(',', outBuffer.get());
        assertEquals(' ', outBuffer.get());
        assertEquals('b', outBuffer.get());
        assertEquals('a', outBuffer.get());
        assertEquals('b', outBuffer.get());
        assertEquals('y', outBuffer.get());
        assertFalse(queue.hasData());
        assertNull(queue.current());
    }

    @Test
    public void write() throws IOException {
        final ActionQueueImpl queue = new ActionQueueImpl(direction, connection, 3, 64);
        queue.add(write0);
        final Action action = queue.current();
        final Action duplicateAction = queue.current();
        assertSame(action, duplicateAction);
        assertTrue(queue.hasData());
        action.writeToSocket();
        verify(direction).write(outBufferCaptor.capture());
        final ByteBuffer outBuffer = outBufferCaptor.getValue();
        assertEquals(6, outBuffer.limit());
        assertEquals('H', outBuffer.get());
        assertEquals('e', outBuffer.get());
        assertEquals('l', outBuffer.get());
        assertEquals('l', outBuffer.get());
        assertEquals('o', outBuffer.get());
        assertTrue(queue.hasData());
        assertNotNull(queue.current());
        assertEquals(' ', outBuffer.get());
        assertFalse(queue.hasData());
        assertNull(queue.current());
    }
}
