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

import java.io.IOException;
import java.nio.ByteBuffer;

import com.mattunderscore.tcproxy.proxy.direction.Direction;

/**
 * Batched write action.
 * @author matt on 20/04/14.
 */
public final class BatchedWrite implements Action {
    private final ByteBuffer data;
    private volatile boolean flipped;
    private volatile Direction direction;

    public BatchedWrite(final int batchCapacity) {
        flipped = false;
        data = ByteBuffer.allocate(batchCapacity);
    }

    @Override
    public int writeToSocket() throws IOException {
        if (!flipped) {
            flipped = true;
            data.flip();
        }
        return direction.write(data);
    }

    @Override
    public boolean writeComplete() {
        return data.remaining() == 0;
    }

    @Override
    public boolean isBatchable() {
        return false;
    }

    /**
     * Add the action to the batch. The action must be batchable.
     * @param action The action
     * @return {@code true} if the action fits completely into the batch
     */
    public boolean batch(WriteAction action) {
        if (flipped) {
            return false;
        }
        else {
            assert action.isBatchable() : "The action should already have been checked to be batchable";
            final ByteBuffer batchData = action.getData();
            if (batchData.remaining() < data.remaining()) {
                data.put(batchData);
                direction = action.getDirection();
                return true;
            }
            else {
                return false;
            }
        }
    }
}
