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

import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOSelector;
import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.proxy.action.Action;
import com.mattunderscore.tcproxy.proxy.action.queue.ActionQueue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Matt Champion on 22/09/14.
 */
public final class WriteSelectorTest {

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
    @Mock
    private Action action;

    private DirectionAndConnection dc;
    private BlockingQueue<DirectionAndConnection> newWorkQueue;
    private WriteSelector writeSelector;

    @Before
    public void setUp() {
        initMocks(this);

        newWorkQueue = new ArrayBlockingQueue<>(5);
        writeSelector = new WriteSelector(selector, newWorkQueue);

        dc = new DirectionAndConnection(direction, connection);

        when(connection.clientToServer()).thenReturn(direction);
        when(connection.serverToClient()).thenReturn(direction);

        when(direction.getFrom()).thenReturn(channel);
        when(direction.getTo()).thenReturn(channel);
        when(direction.getQueue()).thenReturn(queue);
    }

    @Test
    public void registerKeys0() throws ClosedChannelException {
        writeSelector.registerKeys();

        verify(channel, never()).register(eq(selector), eq(IOSelectionKey.Op.WRITE), isA(DirectionAndConnection.class));
    }

    @Test
    public void registerKeys1() throws ClosedChannelException {
        when(channel.register(selector, IOSelectionKey.Op.WRITE, dc)).thenReturn(key);
        newWorkQueue.add(dc);

        writeSelector.registerKeys();

        verify(channel).register(selector, IOSelectionKey.Op.WRITE, dc);
        assertEquals(0, newWorkQueue.size());
    }

    @Test
    public void writeBytes0() {
        final Set<IOSelectionKey> keys = new HashSet<>();
        keys.add(key);

        when(key.isWritable()).thenReturn(true);
        when(key.isValid()).thenReturn(true);
        when(selector.selectedKeys()).thenReturn(keys);
        when(key.attachment()).thenReturn(dc);

        writeSelector.writeBytes();

        verify(queue).hasData();
        verify(key).cancel();
    }

    @Test
    public void writeBytes1() throws IOException {
        final Set<IOSelectionKey> keys = new HashSet<>();
        keys.add(key);

        when(key.isWritable()).thenReturn(true);
        when(key.isValid()).thenReturn(true);
        when(selector.selectedKeys()).thenReturn(keys);
        when(key.attachment()).thenReturn(dc);
        when(queue.hasData()).thenReturn(true);
        when(queue.current()).thenReturn(action);

        writeSelector.writeBytes();

        verify(action).writeToSocket();
    }
}
