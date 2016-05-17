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

package com.mattunderscore.tcproxy.io.serialisation;

import com.mattunderscore.tcproxy.io.data.CircularBuffer;

import java.nio.BufferOverflowException;

/**
 * An abstract implementation of {@link Serialiser} for a {@link CircularBuffer}.
 *
 * @author Matt Champion on 16/05/16
 */
public abstract class AbstractCircularBufferSerialiser<T> implements Serialiser<T, CircularBuffer> {
    @Override
    public final HasCapacity hasCapacity(T object, CircularBuffer buffer) {
        final int requiredCapacity = calculateMaximumRequiredCapacity(object);
        final int freeCapacity = buffer.freeCapacity();
        if (requiredCapacity <= freeCapacity) {
            return HasCapacity.HAS_CAPACITY;
        }
        else if (requiredCapacity <= freeCapacity + buffer.usedCapacity()) {
            return HasCapacity.LACKS_FREE_CAPACITY;
        }
        else {
            return HasCapacity.LACKS_TOTAL_CAPACITY;
        }
    }

    @Override
    public final void write(T object, CircularBuffer buffer) throws BufferOverflowException {
        if (hasCapacity(object, buffer) == HasCapacity.HAS_CAPACITY) {
            doWrite(object, buffer);
        }
        else {
            throw new BufferOverflowException();
        }
    }

    /**
     * Perform the write to the buffer after size checks have validated free capacity
     * @param object The object being serialised
     * @param buffer The buffer to write to
     */
    protected abstract void doWrite(T object, CircularBuffer buffer);

    /**
     * @param object The object being serialised
     * @return The maximum required capacity to serialise the object, may be greater than the actual required capacity
     */
    protected abstract int calculateMaximumRequiredCapacity(T object);
}
