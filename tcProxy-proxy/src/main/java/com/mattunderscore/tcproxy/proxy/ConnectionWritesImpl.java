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

package com.mattunderscore.tcproxy.proxy;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author matt on 19/02/14.
 */
public class ConnectionWritesImpl implements ConnectionWrites {
    private final SocketChannel target;
    private final Connection connection;
    private final BlockingQueue<Write> writes;

    public ConnectionWritesImpl(final SocketChannel target, final Connection connection) {

        this.target = target;
        this.connection = connection;
        this.writes = new ArrayBlockingQueue<>(10000);
    }
    @Override
    public SocketChannel getTarget() {
        return target;
    }

    public boolean queueFull() {
        return writes.remainingCapacity() == 0;
    }

    @Override
    public void add(final Write write) {
        writes.add(write);
    }

    @Override
    public void close() {
        writes.add(new CloseImpl(target));
    }

    @Override
    public Write current() {
        final Write write = writes.peek();
        if (write == null) {
            return null;
        }
        else if (!write.writeComplete()) {
            return write;
        }
        else {
            writes.remove(write);
            return current();
        }
    }

    @Override
    public boolean hasData() {
        return !writes.isEmpty();
    }

    @Override
    public Connection getConnection() {
        return connection;
    }
}
