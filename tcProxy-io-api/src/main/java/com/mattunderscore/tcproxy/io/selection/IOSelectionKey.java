/* Copyright © 2014, 2015 Matthew Champion
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

package com.mattunderscore.tcproxy.io.selection;

import java.nio.channels.SelectionKey;
import java.util.Set;

/**
 * Provides a selection key for use with this package.
 * @author matt on 12/03/14.
 */
public interface IOSelectionKey {

    /**
     * @return {@code true} if the key is valid.
     */
    boolean isValid();

    /**
     * @return {@code true} if the keys channel is ready for accepting.
     */
    boolean isAcceptable();

    /**
     * @return {@code true} if the keys channel is ready for connecting.
     */
    boolean isConnectable();

    /**
     * @return {@code true} if the keys channel is ready for reading.
     */
    boolean isReadable();

    /**
     * @return {@code true} if the keys channel is ready for writing.
     */
    boolean isWritable();

    /**
     * Request the keys channel is deregistered from its selector. The key set will be updated at the next selection
     * operation.
     */
    void cancel();

    /**
     * @return The object attached to the key when it was registered.
     */
    Object attachment();

    /**
     * @return The set of interested operations.
     */
    Set<Op> interestedOperations();

    /**
     * @return The set of ready operations.
     */
    Set<Op> readyOperations();

    /**
     * Set the operations the key is interested in.
     * @param ops The set of interested operations.
     */
    void interestedOperations(Set<Op> ops);

    /**
     * Set the operation as an interested one. Appends to the operations already there.
     * @param op The interested operation.
     */
    void setInterestedOperation(Op op);

    /**
     * Set the operation as an uninterested one. Removes from the operations already there.
     * @param op The uninterested operation.
     */
    void clearInterestedOperation(Op op);

    /**
     * The available operations that a selection key may be interested in.
     */
    enum Op {
        /**
         * Selection operation for accepting new connections.
         */
        ACCEPT(SelectionKey.OP_ACCEPT),
        /**
         * Selection operation for connecting.
         */
        CONNECT(SelectionKey.OP_CONNECT),
        /**
         * Selection operation for reading.
         */
        READ(SelectionKey.OP_READ),
        /**
         * Selection operation for writing.
         */
        WRITE(SelectionKey.OP_WRITE);

        final int op;

        Op(final int op) {
            this.op = op;
        }
    }
}
