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

package com.mattunderscore.tcproxy.proxy;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.mattunderscore.tcproxy.io.CircularBuffer;
import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOSelector;
import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.io.impl.CircularBufferImpl;
import com.mattunderscore.tcproxy.proxy.action.processor.ActionProcessor;
import com.mattunderscore.tcproxy.proxy.action.processor.DefaultActionProcessor;
import com.mattunderscore.tcproxy.proxy.action.queue.ActionQueue;
import com.mattunderscore.tcproxy.proxy.connection.Connection;
import com.mattunderscore.tcproxy.proxy.direction.Direction;
import com.mattunderscore.tcproxy.proxy.direction.DirectionAndConnection;

/**
 * Unit tests for {@link ReadSelector}.
 * @author Matt Champion on 12/03/14.
 */
public final class ReadSelectorTest {
    private final CircularBuffer buffer = CircularBufferImpl.allocate(16);

    @Mock
    private IOSelectionKey key;
    @Mock
    private IOSocketChannel channel;
    @Mock
    private Direction direction;
    @Mock
    private Connection connection;
    @Mock
    private IOSelector selector;
    @Mock
    private ActionQueue queue;

    private DirectionAndConnection dc;
    private BlockingQueue<Connection> newConnections;
    private BlockingQueue<DirectionAndConnection> newDirections;
    private ReadSelector readSelector;

    @Before
    public void setUp() {
        initMocks(this);
        newConnections = new ArrayBlockingQueue<>(5);
        newDirections = new ArrayBlockingQueue<>(5);
        readSelector = new ReadSelector(selector, newConnections, buffer);

        dc = new DirectionAndConnection(direction, connection);

        when(connection.clientToServer()).thenReturn(direction);
        when(connection.serverToClient()).thenReturn(direction);

        when(direction.getFrom()).thenReturn(channel);
        when(direction.getTo()).thenReturn(channel);
        when(direction.getQueue()).thenReturn(queue);
        final ActionProcessor processor = new DefaultActionProcessor(dc, newDirections);
        when(direction.getProcessor()).thenReturn(processor);

        when(key.attachment()).thenReturn(dc);
    }

    @Test
    public void registerKeys0() throws ClosedChannelException {
        readSelector.registerKeys();

        verify(channel, never()).register(selector, IOSelectionKey.Op.READ, dc);
    }

    @Test
    public void registerKeys1() throws ClosedChannelException {
        when(channel.register(selector, IOSelectionKey.Op.READ, dc)).thenReturn(key);
        newConnections.add(connection);

        readSelector.registerKeys();

        verify(channel, times(2)).register(selector, IOSelectionKey.Op.READ, dc);
        assertEquals(0, newConnections.size());
    }

    @Test
    public void readBytes() throws IOException {
        when(key.isReadable()).thenReturn(true);
        when(key.isValid()).thenReturn(true);
        when(queue.queueFull()).thenReturn(false);
        when(queue.hasData()).thenReturn(false);
        when(direction.read(buffer)).then(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
                buffer.put(new byte[8]);
                return 8;
            }
        });

        readSelector.readBytes(key);

        assertEquals(1, newDirections.size());
    }
}
