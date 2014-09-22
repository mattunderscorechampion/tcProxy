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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.proxy.action.queue.ActionQueue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * @author Matt Champion on 22/09/14.
 */
public final class ConnectionManagerTest {
    @Mock
    private SocketAddress remoteAddress;
    @Mock
    private SocketAddress localAddress;
    @Mock
    private IOSocketChannel channel0;
    @Mock
    private IOSocketChannel channel1;
    @Mock
    private ActionQueue actionQueue;
    @Mock
    private ConnectionManager.Listener listener;
    @Mock
    private Direction.Listener directionListener;

    @Before
    public void setUp() throws IOException {
        initMocks(this);

        when(remoteAddress.toString()).thenReturn("remoteAddress");
        when(localAddress.toString()).thenReturn("localAddress");

        when(channel0.getRemoteAddress()).thenReturn(remoteAddress);
        when(channel1.getRemoteAddress()).thenReturn(remoteAddress);
        when(channel0.getLocalAddress()).thenReturn(localAddress);
        when(channel1.getLocalAddress()).thenReturn(localAddress);
    }

    @Test
    public void listenerTest0() throws IOException {
        final ConnectionManager manager = new ConnectionManager();

        final Direction dir0 = new DirectionImpl(channel0, channel1, actionQueue);
        final Direction dir1 = new DirectionImpl(channel1, channel0, actionQueue);
        final Connection conn = new ConnectionImpl(manager, dir0, dir1);
        manager.addListener(listener);
        dir0.addListener(directionListener);
        dir1.addListener(directionListener);

        manager.register(conn);
        verify(listener).newConnection(conn);

        dir0.close();
        dir1.close();
        verify(listener).closedConnection(conn);
        verify(directionListener).closed(dir0);
        verify(directionListener).closed(dir1);
    }

    @Test
    public void listenerTest1() throws IOException {
        final ConnectionManager manager = new ConnectionManager();

        final Direction dir0 = new DirectionImpl(channel0, channel1, actionQueue);
        final Direction dir1 = new DirectionImpl(channel1, channel0, actionQueue);
        final Connection conn = new ConnectionImpl(manager, dir0, dir1);
        manager.addListener(listener);
        dir0.addListener(directionListener);
        dir1.addListener(directionListener);

        manager.register(conn);
        verify(listener).newConnection(conn);

        conn.close();
        verify(listener).closedConnection(conn);
        verify(directionListener).closed(dir0);
        verify(directionListener).closed(dir1);
    }

    @Test
    public void connectionsTest() throws IOException {
        final ConnectionManager manager = new ConnectionManager();

        final Direction dir0 = new DirectionImpl(channel0, channel1, actionQueue);
        final Direction dir1 = new DirectionImpl(channel1, channel0, actionQueue);
        final Connection conn = new ConnectionImpl(manager, dir0, dir1);

        manager.register(conn);

        assertTrue(manager.getConnections().contains(conn));
        assertEquals(1, manager.getConnections().size());

        conn.close();
        assertEquals(0, manager.getConnections().size());
    }
}
