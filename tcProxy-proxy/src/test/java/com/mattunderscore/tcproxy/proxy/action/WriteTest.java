/* Copyright © 2016 Matthew Champion
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
