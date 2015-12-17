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

package com.mattunderscore.tcproxy.proxy.selector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedChannelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.data.CircularBuffer;
import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;
import com.mattunderscore.tcproxy.proxy.ConnectionImpl;
import com.mattunderscore.tcproxy.proxy.action.Close;
import com.mattunderscore.tcproxy.proxy.action.Write;
import com.mattunderscore.tcproxy.proxy.action.queue.ActionQueue;
import com.mattunderscore.tcproxy.proxy.direction.Direction;
import com.mattunderscore.tcproxy.proxy.direction.DirectionAndConnection;
import com.mattunderscore.tcproxy.selector.SelectionRunnable;
import com.mattunderscore.tcproxy.selector.general.RegistrationHandle;

/**
 * Proxy read task.
 * @author Matt Champion on 18/11/2015
 */
public final class ReadSelectionRunnable implements SelectionRunnable<IOSocketChannel> {
    private static final Logger LOG = LoggerFactory.getLogger("reader");
    private final DirectionAndConnection dc;
    private final CircularBuffer readBuffer;

    public ReadSelectionRunnable(DirectionAndConnection dc, CircularBuffer readBuffer) {
        this.dc = dc;
        this.readBuffer = readBuffer;
    }

    @Override
    public void run(IOSocketChannel socket, RegistrationHandle handle) {
        if (!handle.isValid()) {
            LOG.debug("{} : Selected key no longer valid, closing connection", this);
            try {
                dc.getConnection().close();
            }
            catch (IOException e) {
                LOG.warn("{} : Error closing connection", this, e);
            }
        }
        else if (handle.isReadable()) {
            final Direction direction = dc.getDirection();
            final ActionQueue queue = direction.getQueue();
            if (!queue.queueFull()) {
                final ByteChannel channel = direction.getFrom();
                try {
                    // Read data in
                    final int bytes = direction.read(readBuffer);

                    if (bytes > 0) {
                        // Copy the data read to a write buffer and prepare for the next read
                        final ByteBuffer writeBuffer = ByteBuffer.allocate(readBuffer.usedCapacity());
                        readBuffer.get(writeBuffer);
                        writeBuffer.flip();

                        direction.getProcessor().process(new Write(direction, writeBuffer));
                    }
                    else if (bytes == -1) {
                        // Close the connection
                        handle.cancel();
                        direction.getProcessor().process(new Close(direction));
                        final ConnectionImpl conn = (ConnectionImpl) dc.getConnection();
                        final Direction otherDirection = conn.otherDirection(direction);
                        LOG.debug("{} : Closed {} ", this, otherDirection);
                        otherDirection.close();
                    }
                }
                catch (final ClosedChannelException e) {
                    LOG.debug("{} : Channel {} already closed", this, channel);
                    handle.cancel();
                }
                catch (final IOException e) {
                    LOG.debug("{} : Error on channel {}, {}", this, channel, handle, e);
                }
            }
        }
        else {
            LOG.debug("{} : Unexpected key state {}", this, handle);
        }
    }
}
