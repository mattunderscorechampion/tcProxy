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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.isA;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.mattunderscore.tcproxy.io.data.BufferView;
import com.mattunderscore.tcproxy.io.data.CircularBuffer;

public final class CircularBufferImplTest {
    @Mock
    private SocketChannel socketChannel;

    @Before
    public void setUp() {
        initMocks(this);
    }

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

    @Test
    public void advance() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        buffer.put(new byte[] { 0x0, 0x1, 0x2 });
        buffer.advance(1);
        assertEquals(2, buffer.usedCapacity());
        assertEquals(0x1, buffer.get());
    }

    @Test
    public void advancePastWrap() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        buffer.put(new byte[] { 0x0, 0x1, 0x2 });
        buffer.get();
        buffer.get();
        buffer.put(new byte[] {0x3, 0x4});
        buffer.advance(2);
        assertEquals(1, buffer.usedCapacity());
        assertEquals(0x4, buffer.get());
    }

    @Test(expected = BufferUnderflowException.class)
    public void advanceTooFar() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        buffer.put(new byte[] { 0x0, 0x1, 0x2 });
        buffer.advance(4);
    }

    @Test
    public void getNewArray() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        buffer.put(new byte[] { 0x0, 0x1, 0x2 });
        final byte[] bytes = buffer.get(2);
        assertArrayEquals(new byte[] {0x0, 0x1}, bytes);
        assertEquals(1, buffer.usedCapacity());
    }

    @Test
    public void getNewArrayOverWrap() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        buffer.put(new byte[] { 0x0, 0x1, 0x2 });
        buffer.get();
        buffer.get();
        buffer.put(new byte[] {0x3, 0x4});
        final byte[] bytes = buffer.get(2);
        assertArrayEquals(new byte[] {0x2, 0x3}, bytes);
        assertEquals(1, buffer.usedCapacity());
    }

    @Test(expected = BufferUnderflowException.class)
    public void getNewArrayTooMany() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        buffer.put(new byte[] { 0x0, 0x1, 0x2 });
        buffer.get(4);
    }

    @Test
    public void getIntoArray() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        buffer.put(new byte[] { 0x0, 0x1, 0x2 });
        final byte[] bytes = new byte[2];
        buffer.get(bytes);
        assertArrayEquals(new byte[] {0x0, 0x1}, bytes);
        assertEquals(1, buffer.usedCapacity());
    }

    @Test
    public void getIntoArrayOverWrap() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        buffer.put(new byte[] { 0x0, 0x1, 0x2 });
        buffer.get();
        buffer.get();
        buffer.put(new byte[] {0x3, 0x4});
        assertEquals(3, buffer.usedCapacity());
        final byte[] bytes = new byte[2];
        buffer.get(bytes);
        assertArrayEquals(new byte[] {0x2, 0x3}, bytes);
        assertEquals(1, buffer.usedCapacity());
    }

    @Test(expected = BufferUnderflowException.class)
    public void getIntoArrayTooMany() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        buffer.put(new byte[] { 0x0, 0x1, 0x2 });
        final byte[] bytes = new byte[4];
        buffer.get(bytes);
    }

    @Test
    public void view() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        buffer.put(new byte[] { 0x0, 0x1, 0x2 });
        assertEquals(3, buffer.usedCapacity());
        final BufferView view0 = buffer.view();
        assertArrayEquals(new byte[] {0x0, 0x1, 0x2}, view0.get(3));
        assertEquals(0, view0.usedCapacity());
        assertEquals(3, buffer.usedCapacity());
        final BufferView view1 = buffer.view();
        assertArrayEquals(new byte[] {0x0, 0x1, 0x2}, view1.get(3));
        assertEquals(0, view1.usedCapacity());
        assertEquals(3, buffer.usedCapacity());
        assertArrayEquals(new byte[] {0x0, 0x1, 0x2}, buffer.get(3));
        assertEquals(0, buffer.usedCapacity());
    }

    @Test
    public void doSocketRead0() throws IOException {
        when(socketChannel.read(isA(ByteBuffer.class))).then(new WriteToBufferArgument(0x1));
        final CircularBufferImpl buffer = (CircularBufferImpl) CircularBufferImpl.allocate(3);
        final int read0 = buffer.doSocketRead(socketChannel);
        assertEquals(1, read0);
        assertEquals(1, buffer.usedCapacity());
        assertEquals(2, buffer.freeCapacity());

        when(socketChannel.read(isA(ByteBuffer.class))).then(new WriteToBufferArgument(new byte[] {0x2, 0x3}));
        final int read1 = buffer.doSocketRead(socketChannel);
        assertEquals(2, read1);
        assertEquals(3, buffer.usedCapacity());
        assertEquals(0, buffer.freeCapacity());

        assertEquals(0x1, buffer.get());
        assertEquals(2, buffer.usedCapacity());
        assertEquals(1, buffer.freeCapacity());

        when(socketChannel.read(isA(ByteBuffer.class))).then(new WriteToBufferArgument(0x4));
        final int read2 = buffer.doSocketRead(socketChannel);
        assertEquals(1, read2);

        assertEquals(0x2, buffer.get());
        assertEquals(0x3, buffer.get());
        assertEquals(0x4, buffer.get());
        assertEquals(0, buffer.usedCapacity());
        assertEquals(3, buffer.freeCapacity());
    }

    @Test
    public void doSocketRead1() throws IOException {
        when(socketChannel.read(isA(ByteBuffer.class))).then(new WriteToBufferArgument(new byte[] {0x1, 0x2}));
        final CircularBufferImpl buffer = (CircularBufferImpl) CircularBufferImpl.allocate(3);
        final int read0 = buffer.doSocketRead(socketChannel);
        assertEquals(2, read0);
        assertEquals(2, buffer.usedCapacity());
        assertEquals(1, buffer.freeCapacity());

        assertEquals(0x1, buffer.get());
        assertEquals(1, buffer.usedCapacity());
        assertEquals(2, buffer.freeCapacity());

        when(socketChannel.read(isA(ByteBuffer.class))).then(new WriteToBufferArgument(0x3));
        final int read1 = buffer.doSocketRead(socketChannel);
        when(socketChannel.read(isA(ByteBuffer.class))).then(new WriteToBufferArgument(0x4));
        final int read2 = buffer.doSocketRead(socketChannel);
        assertEquals(1, read1);
        assertEquals(1, read2);
        assertEquals(3, buffer.usedCapacity());
        assertEquals(0, buffer.freeCapacity());

        assertEquals(0x2, buffer.get());
        assertEquals(0x3, buffer.get());
        assertEquals(0x4, buffer.get());
        assertEquals(0, buffer.usedCapacity());
        assertEquals(3, buffer.freeCapacity());
    }

    @Test
    public void doSocketWrite0() throws IOException {
        when(socketChannel.write(isA(ByteBuffer.class))).then(new AssertReadFromBufferArgument(new byte[] {0x1, 0x2, 0x3}));
        final CircularBufferImpl buffer = (CircularBufferImpl) CircularBufferImpl.allocate(3);
        buffer.put(new byte[] {0x1, 0x2, 0x3});
        final int written = buffer.doSocketWrite(socketChannel);
        assertEquals(written, 3);
    }

    @Test
    public void doSocketWrite1() throws IOException {
        when(socketChannel.write(isA(ByteBuffer.class))).then(
            new AssertReadFromBufferArgument(new byte[] {0x2, 0x3}, 0x4));
        final CircularBufferImpl buffer = (CircularBufferImpl) CircularBufferImpl.allocate(3);
        buffer.put(new byte[] {0x1, 0x2, 0x3});
        buffer.get();
        buffer.put((byte) 0x4);
        final int written = buffer.doSocketWrite(socketChannel);
        assertEquals(written, 3);
    }

    @Test
    public void doSocketWrite2() throws IOException {
        final CircularBufferImpl buffer = (CircularBufferImpl) CircularBufferImpl.allocate(3);
        final int written = buffer.doSocketWrite(socketChannel);
        assertEquals(written, 0);
    }

    private static final class WriteToBufferArgument implements Answer<Integer> {
        private final byte[] bytes;

        public WriteToBufferArgument(int aByte) {
            this.bytes = new byte[1];
            bytes[0] = (byte) aByte;
        }

        public WriteToBufferArgument(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
            final ByteBuffer buffer = (ByteBuffer) invocationOnMock.getArguments()[0];
            buffer.put(bytes);
            return bytes.length;
        }
    }

    private static final class AssertReadFromBufferArgument implements Answer<Integer> {
        private final Queue<byte[]> allBytes = new ArrayDeque<>();

        public AssertReadFromBufferArgument(int aByte) {
            final byte[] bytes = new byte[1];
            bytes[0] = (byte) aByte;
            allBytes.add(bytes);
        }

        public AssertReadFromBufferArgument(byte[] bytes) {
            allBytes.add(bytes);
        }

        public AssertReadFromBufferArgument(byte[] bytes, int aByte) {
            allBytes.add(bytes);
            final byte[] moreBytes = new byte[1];
            moreBytes[0] = (byte) aByte;
            allBytes.add(moreBytes);
        }

        @Override
        public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
            final byte[] bytes = allBytes.poll();
            final ByteBuffer buffer = (ByteBuffer) invocationOnMock.getArguments()[0];
            final byte[] readBytes = new byte[bytes.length];
            buffer.get(readBytes);
            Assert.assertArrayEquals(bytes, readBytes);
            return bytes.length;
        }
    }
}
