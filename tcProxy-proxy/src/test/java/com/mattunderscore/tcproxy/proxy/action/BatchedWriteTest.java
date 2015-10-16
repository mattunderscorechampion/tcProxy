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

package com.mattunderscore.tcproxy.proxy.action;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.mattunderscore.tcproxy.proxy.direction.Direction;

/**
 * Unit tests for {@link BatchedWrite}.
 * @author Matt Champion on 21/04/14.
 */
public final class BatchedWriteTest {
    @Mock
    private IWrite write0;
    @Mock
    private IWrite write1;
    @Mock
    private Direction direction;
    @Captor
    private ArgumentCaptor<ByteBuffer> outBufferCaptor;

    @Before
    public void setUp() {
        initMocks(this);
        when(write0.getDirection()).thenReturn(direction);
        when(write1.getDirection()).thenReturn(direction);
        final ByteBuffer buffer0 = ByteBuffer.allocate(64);
        buffer0.put("Hello ".getBytes());
        buffer0.flip();
        when(write0.getData()).thenReturn(buffer0);
        when(write0.isBatchable()).thenReturn(Boolean.TRUE);
        final ByteBuffer buffer1 = ByteBuffer.allocate(64);
        buffer1.put("world".getBytes());
        buffer1.flip();
        when(write1.getData()).thenReturn(buffer1);
        when(write1.isBatchable()).thenReturn(Boolean.TRUE);
    }

    @Test
    public void batch() throws IOException {
        final BatchedWrite batchWrite = new BatchedWrite(64);
        batchWrite.batch(write0);
        batchWrite.batch(write1);
        batchWrite.writeToSocket();
        verify(direction).write(outBufferCaptor.capture());
        final ByteBuffer outBuffer = outBufferCaptor.getValue();
        assertEquals(11, outBuffer.limit());
        assertEquals('H', outBuffer.get(0));
        assertEquals('e', outBuffer.get(1));
        assertEquals('l', outBuffer.get(2));
        assertEquals('l', outBuffer.get(3));
        assertEquals('o', outBuffer.get(4));
        assertEquals(' ', outBuffer.get(5));
        assertEquals('w', outBuffer.get(6));
        assertEquals('o', outBuffer.get(7));
        assertEquals('r', outBuffer.get(8));
        assertEquals('l', outBuffer.get(9));
        assertEquals('d', outBuffer.get(10));
    }
}
