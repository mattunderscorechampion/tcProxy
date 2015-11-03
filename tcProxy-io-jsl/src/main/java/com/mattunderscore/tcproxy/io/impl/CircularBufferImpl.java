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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.mattunderscore.tcproxy.io.CircularBuffer;

/**
 * Implementation of {@link CircularBuffer}.
 * @author Matt Champion on 31/10/2015
 */
public final class CircularBufferImpl implements CircularBuffer {
    private final ByteBuffer buffer;
    private final int capacity;
    private int readPos = 0;
    private int data = 0;

    private CircularBufferImpl(int capacity, ByteBuffer writableBuffer) {
        this.capacity = capacity;
        buffer = writableBuffer;
    }

    @Override
    public boolean put(byte b) {
        if (hasFreeCapacityFor(1)) {
            buffer.put(b);
            data = data + 1;
            wrapWritableBufferIfNeeded();
            return true;
        }
        return false;
    }

    @Override
    public boolean put(byte[] bytes) {
        if (hasFreeCapacityFor(bytes.length)) {
            final int maxWritableBeforeWrap = buffer.remaining();
            if (bytes.length <= maxWritableBeforeWrap) {
                buffer.put(bytes);
                wrapWritableBufferIfNeeded();
            }
            else {
                buffer.put(bytes, 0, maxWritableBeforeWrap);
                buffer.position(0);
                buffer.put(bytes, maxWritableBeforeWrap, bytes.length - maxWritableBeforeWrap);
            }
            data = data + bytes.length;
            return true;
        }
        return false;
    }

    @Override
    public int put(ByteBuffer src) {
        final int length = Math.min(src.remaining(), capacity - data);
        final int maxWritableBeforeWrap = buffer.remaining();
        if (length <= maxWritableBeforeWrap) {
            final int srcLimit = src.limit();
            src.limit(length);
            buffer.put(src);
            wrapWritableBufferIfNeeded();
            src.limit(srcLimit);
        }
        else {
            final int srcLimit = src.limit();
            src.limit(maxWritableBeforeWrap);
            buffer.put(src);
            buffer.position(0);
            src.limit(length);
            buffer.put(src);
            src.limit(srcLimit);
        }
        data = data + length;
        return length;
    }

    @Override
    public byte get() {
        if (data > 0) {
            final byte b = buffer.get(readPos);
            readPos = (readPos + 1) % capacity;
            data = data - 1;
            return b;
        }
        else {
            throw new IllegalStateException();
        }
    }

    @Override
    public int get(ByteBuffer dst) {
        if (data > 0) {
            final ByteBuffer readBuffer = buffer.asReadOnlyBuffer();
            readBuffer.position(readPos);
            final int lengthToCopy = Math.min(dst.remaining(), data);
            final int maxReadableBeforeWrap = readBuffer.remaining();

            if (lengthToCopy <= maxReadableBeforeWrap) {
                readBuffer.limit(lengthToCopy);
                dst.put(readBuffer);
            }
            else {
                readBuffer.limit(readPos + maxReadableBeforeWrap);
                dst.put(readBuffer);
                readBuffer.position(0);
                readBuffer.limit(lengthToCopy - maxReadableBeforeWrap);
                dst.put(readBuffer);
            }
            readPos = (readPos + lengthToCopy) % capacity;
            data = data - lengthToCopy;
            return lengthToCopy;
        }
        else {
            return 0;
        }
    }

    @Override
    public int freeCapacity() {
        return capacity - data;
    }

    @Override
    public int usedCapacity() {
        return data;
    }

    /*package*/ int doSocketRead(SocketChannel channel) throws IOException {
        final int read;
        if (readPos > buffer.position()) {
            buffer.limit(readPos);
            read = channel.read(buffer);
            buffer.limit(capacity);
        }
        else {
            read = channel.read(buffer);
            wrapWritableBufferIfNeeded();
        }
        data = data + read;
        return read;
    }

    /*package*/ int doSocketWrite(SocketChannel channel) throws IOException {
        final ByteBuffer readableBuffer = buffer.asReadOnlyBuffer();
        readableBuffer.position(readPos);
        final int lengthToCopy = data;
        final int maxReadableBeforeWrap = readableBuffer.remaining();

        int readFromBuffer;
        if (lengthToCopy <= maxReadableBeforeWrap) {
            readableBuffer.limit(readPos + lengthToCopy);
            readFromBuffer = channel.write(readableBuffer);
        }
        else {
            readableBuffer.limit(readPos + maxReadableBeforeWrap);
            readFromBuffer = channel.write(readableBuffer);
            if (!readableBuffer.hasRemaining()) {
                readableBuffer.position(0);
                readableBuffer.limit(lengthToCopy - maxReadableBeforeWrap);
                readFromBuffer = readFromBuffer + channel.write(readableBuffer);
            }
        }
        readPos = (readPos + readFromBuffer) % capacity;
        data = data - readFromBuffer;
        return readFromBuffer;
    }

    private void wrapWritableBufferIfNeeded() {
        if (!buffer.hasRemaining()) {
            buffer.position(0);
        }
    }

    private boolean hasFreeCapacityFor(int numberOfBytes) {
        return data + numberOfBytes <= capacity;
    }

    /**
     * Allocate an array backed circular array.
     * @param capacity The size of the buffer
     * @return The buffer
     */
    public static CircularBuffer allocate(int capacity) {
        return new CircularBufferImpl(capacity, ByteBuffer.allocate(capacity));
    }

    /**
     * Allocate a circular array that uses a contiguous area of memory that can be accessed by native I/O operations.
     * @param capacity The size of the buffer
     * @return The buffer
     */
    public static CircularBuffer allocateDirect(int capacity) {
        return new CircularBufferImpl(capacity, ByteBuffer.allocateDirect(capacity));
    }
}
