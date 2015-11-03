package com.mattunderscore.tcproxy.io.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.mattunderscore.tcproxy.io.CircularBuffer;

public final class CircularBufferImplTest {

    @Test
    public void empty() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        assertEquals(0, buffer.usedCapacity());
        assertEquals(3, buffer.freeCapacity());
    }

    @Test
    public void put() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        assertTrue(buffer.put((byte) 4));
        assertTrue(buffer.put((byte) 4));
        assertTrue(buffer.put((byte) 4));
        assertFalse(buffer.put((byte) 4));
        assertEquals(3, buffer.usedCapacity());
        assertEquals(0, buffer.freeCapacity());
    }

    @Test
    public void putMany() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        final byte[] bytes = { 0x0, 0x1, 0x2 };
        assertTrue(buffer.put(bytes));
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

    @Test
    public void putTooMany() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(2);
        final byte[] bytes = { 0x0, 0x1, 0x2 };
        assertFalse(buffer.put(bytes));
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

    @Test(expected = IllegalStateException.class)
    public void get() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        assertTrue(buffer.put((byte) 4));
        assertTrue(buffer.put((byte) 5));
        assertTrue(buffer.put((byte) 3));

        assertEquals(4, buffer.get());
        assertEquals(5, buffer.get());
        assertEquals(3, buffer.get());
        buffer.get();
    }

    @Test
    public void getBuffer() {
        final CircularBuffer buffer = CircularBufferImpl.allocate(3);
        assertTrue(buffer.put((byte) 4));
        assertTrue(buffer.put((byte) 5));
        assertTrue(buffer.put((byte) 3));

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
        assertTrue(buffer.put((byte) 4));
        assertTrue(buffer.put((byte) 5));
        assertTrue(buffer.put((byte) 3));
        buffer.get();
        assertTrue(buffer.put((byte) 4));

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
        assertTrue(buffer.put((byte) 4));
        assertTrue(buffer.put((byte) 5));
        assertTrue(buffer.put((byte) 3));

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
        final byte[] bytes = { 0x0, 0x1, 0x2 };
        assertTrue(buffer.put(bytes));
        assertEquals(3, buffer.usedCapacity());
        assertEquals(0, buffer.freeCapacity());
        assertEquals(0, buffer.get());
        assertEquals(1, buffer.freeCapacity());
        assertTrue(buffer.put((byte) 5));
        assertEquals(1, buffer.get());
        assertEquals(2, buffer.get());
        assertTrue(buffer.put((byte) 7));
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
        assertTrue(buffer.put(bytes));
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
