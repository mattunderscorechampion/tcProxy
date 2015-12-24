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

import static java.lang.Math.min;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import com.mattunderscore.tcproxy.io.data.BufferView;

/**
 * A {@link BufferView} for Circular buffers.
 * @author Matt Champion on 24/12/2015
 */
/*package*/ class CircularBufferView implements BufferView {
    protected final ByteBuffer buffer;
    protected int readPos;
    protected int data;

    protected CircularBufferView(ByteBuffer buffer) {
        this.buffer = buffer;
        this.readPos = 0;
        this.data = 0;
    }

    protected CircularBufferView(ByteBuffer buffer, int readPos, int data) {
        this.buffer = buffer;
        this.readPos = readPos;
        this.data = data;
    }

    @Override
    public final byte get() throws BufferUnderflowException {
        if (data > 0) {
            final byte b = buffer.get(readPos);
            readPos = (readPos + 1) % buffer.capacity();
            data = data - 1;
            return b;
        }
        else {
            throw new BufferUnderflowException();
        }
    }

    @Override
    public final int get(ByteBuffer dst) {
        if (data > 0) {
            final ByteBuffer readBuffer = buffer.asReadOnlyBuffer();
            final int initialReadPosition = readPos;
            readBuffer.position(initialReadPosition);
            final int lengthToCopy = min(dst.remaining(), usedCapacity());
            final int maxReadableBeforeWrap = readBuffer.remaining();

            if (lengthToCopy <= maxReadableBeforeWrap) {
                // Copy to destination without wrapping circular buffer
                readBuffer.limit(initialReadPosition + lengthToCopy);
                dst.put(readBuffer);
            }
            else {
                // Copy to destination with wrapping circular buffer
                readBuffer.limit(initialReadPosition + maxReadableBeforeWrap);
                dst.put(readBuffer);
                readBuffer.position(0);
                readBuffer.limit(lengthToCopy - maxReadableBeforeWrap);
                dst.put(readBuffer);
            }
            readPos = (readPos + lengthToCopy) % buffer.capacity();
            data = data - lengthToCopy;
            return lengthToCopy;
        }
        else {
            return 0;
        }
    }

    @Override
    public final byte[] get(int bytes) throws BufferUnderflowException {
        final byte[] byteArray = new byte[bytes];
        get(byteArray);
        return byteArray;
    }

    @Override
    public final void get(byte[] bytes) throws BufferUnderflowException {
        if (bytes.length > data) {
            throw new BufferUnderflowException();
        }
        else if (bytes.length == 0) {
            return;
        }

        final ByteBuffer readBuffer = buffer.asReadOnlyBuffer();
        final int initialReadPosition = readPos;
        readBuffer.position(initialReadPosition);
        final int maxReadableBeforeWrap = readBuffer.remaining();

        if (bytes.length <= maxReadableBeforeWrap) {
            // Copy to destination without wrapping circular buffer
            readBuffer.limit(initialReadPosition + bytes.length);
            readBuffer.get(bytes);
        }
        else {
            // Copy to destination with wrapping circular buffer
            readBuffer.limit(initialReadPosition + maxReadableBeforeWrap);
            readBuffer.get(bytes, 0, maxReadableBeforeWrap);
            readBuffer.position(0);
            readBuffer.limit(bytes.length - maxReadableBeforeWrap);
            readBuffer.get(bytes, maxReadableBeforeWrap, bytes.length - maxReadableBeforeWrap);
        }
        readPos = (readPos + bytes.length) % buffer.capacity();
        data = data - bytes.length;
    }

    @Override
    public final void advance(int bytes) throws BufferUnderflowException {
        if (bytes > data) {
            throw new BufferUnderflowException();
        }
        final ByteBuffer readBuffer = buffer.asReadOnlyBuffer();
        final int initialReadPosition = readPos;
        readBuffer.position(initialReadPosition);
        final int maxReadableBeforeWrap = readBuffer.remaining();
        if (bytes <= maxReadableBeforeWrap) {
            readPos = readPos + bytes;
        }
        else {
            readPos = bytes - maxReadableBeforeWrap;
        }
        data = data - bytes;
    }

    @Override
    public final int usedCapacity() {
        return data;
    }
}
