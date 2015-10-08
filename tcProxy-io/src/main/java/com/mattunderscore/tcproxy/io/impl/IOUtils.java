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

package com.mattunderscore.tcproxy.io.impl;

import java.nio.channels.SelectionKey;
import java.util.EnumSet;
import java.util.Set;

import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOSocketOption;

/**
 * @author Matt Champion on 13/03/14.
 */
final class IOUtils {
    private IOUtils() {
    }

    /**
     * Map from op enum to int.
     * @param op The op.
     * @return The int value.
     */
    static int mapToIntFromOp(IOSelectionKey.Op op) {
        switch (op) {
            case ACCEPT: return SelectionKey.OP_ACCEPT;
            case CONNECT: return SelectionKey.OP_CONNECT;
            case READ: return SelectionKey.OP_READ;
            case WRITE: return SelectionKey.OP_WRITE;
            default: throw new IllegalStateException("Unknown op");
        }
    }

    /**
     * Map from int to enum set.
     * @param ops The int value.
     * @return The operation set.
     */
    static Set<IOSelectionKey.Op> mapToIntFromOps(int ops) {
        final EnumSet<IOSelectionKey.Op> set = EnumSet.noneOf(IOSelectionKey.Op.class);
        if ((ops & SelectionKey.OP_READ) > 0) {
            set.add(IOSelectionKey.Op.READ);
        }
        if ((ops & SelectionKey.OP_WRITE) > 0) {
            set.add(IOSelectionKey.Op.WRITE);
        }
        if ((ops & SelectionKey.OP_ACCEPT) > 0) {
            set.add(IOSelectionKey.Op.ACCEPT);
        }
        if ((ops & SelectionKey.OP_CONNECT) > 0) {
            set.add(IOSelectionKey.Op.CONNECT);
        }
        return set;
    }

    /**
     * Convert a set of ops to a bitmask.
     * @param ops The set of ops.
     * @return The bitmask,
     */
    static int convertToBitSet(final Set<IOSelectionKey.Op> ops) {
        int sum = 0;
        for (final IOSelectionKey.Op op : ops) {
            sum |= mapToIntFromOp(op);
        }
        return sum;
    }

    @SuppressWarnings("unchecked")
    static <T> IOSocketOptionImpl<T> convertSocketOption(IOSocketOption<T> option) {
        if (option == IOSocketOption.RECEIVE_BUFFER) {
            return (IOSocketOptionImpl<T>) IOSocketOptionImpl.RECEIVE_BUFFER;
        }
        else if (option == IOSocketOption.SEND_BUFFER) {
            return (IOSocketOptionImpl<T>) IOSocketOptionImpl.SEND_BUFFER;
        }
        else if (option == IOSocketOption.BLOCKING) {
            return (IOSocketOptionImpl<T>) IOSocketOptionImpl.BLOCKING;
        }
        else if (option == IOSocketOption.KEEP_ALIVE) {
            return (IOSocketOptionImpl<T>) IOSocketOptionImpl.KEEP_ALIVE;
        }
        else if (option == IOSocketOption.LINGER) {
            return (IOSocketOptionImpl<T>) IOSocketOptionImpl.LINGER;
        }
        else if (option == IOSocketOption.REUSE_ADDRESS) {
            return (IOSocketOptionImpl<T>) IOSocketOptionImpl.REUSE_ADDRESS;
        }
        else if (option == IOSocketOption.TCP_NO_DELAY) {
            return (IOSocketOptionImpl<T>) IOSocketOptionImpl.TCP_NO_DELAY;
        }
        else {
            throw new IllegalStateException("Unknown socket option");
        }
    }
}
