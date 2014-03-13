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

package com.mattunderscore.tcproxy.io;

import java.nio.channels.SelectionKey;
import java.util.Set;

/**
 * Provides a selection key for use with this package.
 * @author matt on 12/03/14.
 */
public interface IOSelectionKey {

    boolean isValid();
    boolean isReadable();
    boolean isWritable();
    void cancel();
    Object attachment();

    /**
     * The available operations that a selection key may be interested in.
     */
    enum Op {
        ACCEPT(SelectionKey.OP_ACCEPT),
        CONNECT(SelectionKey.OP_CONNECT),
        READ(SelectionKey.OP_READ),
        WRITE(SelectionKey.OP_WRITE);

        final int op;

        Op(final int op) {

            this.op = op;
        }

        static int bitSet(Set<Op> ops) {
            int sum = 0;
            for (final Op op : ops) {
                sum = op.op;
            }
            return sum;
        }
    }
}
