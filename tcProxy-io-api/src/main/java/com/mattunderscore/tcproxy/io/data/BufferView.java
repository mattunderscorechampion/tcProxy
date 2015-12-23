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

package com.mattunderscore.tcproxy.io.data;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * A view of a buffer.
 * @author Matt Champion on 23/12/2015
 */
public interface BufferView {
    /**
     * @return A single byte from the buffer
     * @throws BufferUnderflowException if there is no data to read from the buffer
     */
    byte get() throws BufferUnderflowException;

    /**
     * @param dst A {@link ByteBuffer} to copy data into
     * @return A number of bytes copied to the destination
     */
    int get(ByteBuffer dst);

    /**
     * @param bytes The number of bytes to read
     * @return An array containing the bytes read
     * @throws BufferUnderflowException if attempting to read more than the used capacity
     */
    byte[] get(int bytes) throws BufferUnderflowException;

    /**
     * @param bytes The byte array to copy data into
     * @throws BufferUnderflowException if the array contains more room than there is data available.
     */
    void get(byte[] bytes) throws BufferUnderflowException;

    /**
     * Advance the pointer by some number of bytes.
     * @param bytes The number of bytes to advance
     * @throws BufferUnderflowException if attempting to advance more than the used capacity
     */
    void advance(int bytes) throws BufferUnderflowException;

    /**
     * @return The number of bytes that can be read from the buffer
     */
    int usedCapacity();
}
