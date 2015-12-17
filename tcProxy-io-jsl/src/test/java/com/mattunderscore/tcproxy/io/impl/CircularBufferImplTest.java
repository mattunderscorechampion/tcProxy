/* Copyright © 2015 Matthew Champion
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

package com.mattunderscore.tcproxy.io.impl;

import static org.junit.Assert.assertEquals;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.mattunderscore.tcproxy.io.data.CircularBuffer;

public final class CircularBufferImplTest {

    @Test
    public void empty() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        assertEquals(0, buffer.usedCapacity());
        assertEquals(3, buffer.freeCapacity());
    }

    @Test(expected = BufferOverflowException.class)
    public void put() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        buffer.put((byte) 4);
        buffer.put((byte) 4);
        buffer.put((byte) 4);
        assertEquals(3, buffer.usedCapacity());
        assertEquals(0, buffer.freeCapacity());
        buffer.put((byte) 4);
    }

    @Test
    public void putMany() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        final byte[] bytes = { 0x0, 0x1, 0x2 };
        buffer.put(bytes);
        assertEquals(3, buffer.usedCapacity());
        assertEquals(0, buffer.freeCapacity());
        assertEquals(0, buffer.get());
        assertEquals(1, buffer.get());
        assertEquals(2, buffer.get());
        assertEquals(0, buffer.usedCapacity());
        assertEquals(3, buffer.freeCapacity());
    }

    @Test
    public void putManyBuffer() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        final byte[] bytes = { 0x0, 0x1, 0x2 };
        assertEquals(3, buffer.put(ByteBuffer.wrap(bytes)));
        assertEquals(3, buffer.usedCapacity());
        assertEquals(0, buffer.freeCapacity());
        assertEquals(0, buffer.get());
        assertEquals(1, buffer.get());
        assertEquals(2, buffer.get());
        assertEquals(0, buffer.usedCapacity());
        assertEquals(3, buffer.freeCapacity());
    }

    @Test(expected = BufferOverflowException.class)
    public void putTooMany() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(2);
        final byte[] bytes = { 0x0, 0x1, 0x2 };
        buffer.put(bytes);
    }

    @Test
    public void putTooManyBuffer() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(2);
        final byte[] bytes = { 0x0, 0x1, 0x2 };
        assertEquals(2, buffer.put(ByteBuffer.wrap(bytes)));
    }

    @Test
    public void putTooManyBufferFull() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(2);
        final byte[] bytes = { 0x0, 0x1, 0x2 };
        buffer.put((byte) 0);
        buffer.put((byte) 0);
        assertEquals(0, buffer.put(ByteBuffer.wrap(bytes)));
    }

    @Test(expected = BufferUnderflowException.class)
    public void get() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        buffer.put((byte) 4);
        buffer.put((byte) 5);
        buffer.put((byte) 3);

        assertEquals(4, buffer.get());
        assertEquals(5, buffer.get());
        assertEquals(3, buffer.get());
        buffer.get();
    }

    @Test
    public void getBuffer() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        buffer.put((byte) 4);
        buffer.put((byte) 5);
        buffer.put((byte) 3);

        final ByteBuffer byteBuffer = ByteBuffer.allocate(5);
        assertEquals(3, buffer.get(byteBuffer));
        assertEquals(0, buffer.usedCapacity());

        byteBuffer.flip();
        assertEquals(3, byteBuffer.remaining());
        assertEquals(4, byteBuffer.get());
        assertEquals(5, byteBuffer.get());
        assertEquals(3, byteBuffer.get());
    }

    @Test
    public void getBufferWrap() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        buffer.put((byte) 4);
        buffer.put((byte) 5);
        buffer.put((byte) 3);
        buffer.get();
        buffer.put((byte) 4);

        final ByteBuffer byteBuffer = ByteBuffer.allocate(5);
        assertEquals(3, buffer.get(byteBuffer));
        assertEquals(0, buffer.usedCapacity());

        byteBuffer.flip();
        assertEquals(3, byteBuffer.remaining());
        assertEquals(5, byteBuffer.get());
        assertEquals(3, byteBuffer.get());
        assertEquals(4, byteBuffer.get());
    }

    @Test
    public void getBufferSome() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        buffer.put((byte) 4);
        buffer.put((byte) 5);
        buffer.put((byte) 3);

        final ByteBuffer byteBuffer = ByteBuffer.allocate(2);
        assertEquals(2, buffer.get(byteBuffer));
        assertEquals(1, buffer.usedCapacity());

        byteBuffer.flip();
        assertEquals(2, byteBuffer.remaining());
        assertEquals(4, byteBuffer.get());
        assertEquals(5, byteBuffer.get());
    }

    @Test
    public void wrap() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        final byte[] bytes = { 0x0, 0x1, 0x2};
        buffer.put(bytes);
        assertEquals(3, buffer.usedCapacity());
        assertEquals(0, buffer.freeCapacity());
        assertEquals(0, buffer.get());
        assertEquals(1, buffer.freeCapacity());
        buffer.put((byte) 5);
        assertEquals(1, buffer.get());
        assertEquals(2, buffer.get());
        buffer.put((byte) 7);
        assertEquals(5, buffer.get());
        assertEquals(7, buffer.get());
    }

    @Test
    public void wrapPut() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        final byte[] bytes = { 0x0, 0x1 };
        buffer.put((byte) 4);
        buffer.put((byte) 4);
        buffer.get();
        buffer.put(bytes);
        assertEquals(4, buffer.get());
        assertEquals(0, buffer.get());
        assertEquals(1, buffer.get());
    }

    @Test
    public void wrapPutBuffer() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        final byte[] bytes = { 0x0, 0x1 };
        buffer.put((byte) 4);
        buffer.put((byte) 4);
        buffer.get();
        assertEquals(2, buffer.put(ByteBuffer.wrap(bytes)));
        assertEquals(4, buffer.get());
        assertEquals(0, buffer.get());
        assertEquals(1, buffer.get());
    }
}
