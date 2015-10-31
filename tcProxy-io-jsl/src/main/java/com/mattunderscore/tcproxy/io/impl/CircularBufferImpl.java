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

import static java.lang.System.arraycopy;

import com.mattunderscore.tcproxy.io.CircularBuffer;

/**
 * @author Matt Champion on 31/10/2015
 */
public final class CircularBufferImpl implements CircularBuffer {
    final byte[] buffer;
    final int capacity;
    int writePos = 0;
    int readPos = 0;
    int data = 0;

    public CircularBufferImpl(int capacity) {
        this.capacity = capacity;
        buffer = new byte[capacity];
    }

    @Override
    public boolean put(byte b) {
        if (data < capacity) {
            buffer[writePos] = b;
            writePos = (writePos + 1) % capacity;
            data = data + 1;
            return true;
        }
        return false;
    }

    @Override
    public boolean put(byte[] bytes) {
        if (data + bytes.length <= capacity) {
            final int maxWritableBeforeWrap = capacity - writePos;
            if (bytes.length <= maxWritableBeforeWrap) {
                arraycopy(bytes, 0, buffer, writePos, bytes.length);
            }
            else {
                arraycopy(bytes, 0, buffer, writePos, maxWritableBeforeWrap);
                arraycopy(bytes, maxWritableBeforeWrap, buffer, 0, bytes.length - maxWritableBeforeWrap);
            }
            writePos = (writePos + bytes.length) % capacity;
            data = data + bytes.length;
            return true;
        }
        return false;
    }

    @Override
    public byte get() {
        if (data > 0) {
            final byte b = buffer[readPos];
            readPos = (readPos + 1) % capacity;
            data = data - 1;
            return b;
        }
        else {
            throw new IllegalStateException();
        }
    }

    @Override
    public int freeCapacity() {
        return capacity - data;
    }

    @Override
    public int occupied() {
        return data;
    }
}
