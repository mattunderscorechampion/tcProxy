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
import com.mattunderscore.tcproxy.proxy.connection.Connection;
import com.mattunderscore.tcproxy.proxy.direction.Direction;
import com.mattunderscore.tcproxy.selector.SelectionRunnable;
import com.mattunderscore.tcproxy.selector.general.RegistrationHandle;

/**
 * Proxy read task.
 * @author Matt Champion on 18/11/2015
 */
public final class ReadSelectionRunnable implements SelectionRunnable<IOSocketChannel> {
    private static final Logger DATA_LOG = LoggerFactory.getLogger("proxy-data-read");
    private static final Logger LOG = LoggerFactory.getLogger("reader");
    private final Direction direction;
    private final Connection connection;
    private final CircularBuffer readBuffer;

    public ReadSelectionRunnable(Direction direction, Connection connection, CircularBuffer readBuffer) {
        this.direction = direction;
        this.connection = connection;
        this.readBuffer = readBuffer;
    }

    @Override
    public void run(IOSocketChannel socket, RegistrationHandle handle) {
        if (!handle.isValid()) {
            LOG.warn("{} : Selected key no longer valid, closing connection", this);
            try {
                connection.close();
            }
            catch (IOException e) {
                LOG.warn("{} : Error closing connection", this, e);
            }
        }
        else if (handle.isReadable()) {
            final ActionQueue queue = direction.getQueue();
            if (!queue.queueFull()) {
                final ByteChannel channel = direction.getFrom();
                try {
                    assert readBuffer.usedCapacity() == 0 : "The read buffer should be empty";

                    // Read data in
                    final int bytes = direction.read(readBuffer);

                    if (bytes > 0) {
                        // Copy the data read to a write buffer and prepare for the next read
                        final ByteBuffer writeBuffer = ByteBuffer.allocate(readBuffer.usedCapacity());
                        readBuffer.get(writeBuffer);
                        writeBuffer.flip();

                        if (DATA_LOG.isInfoEnabled()) {
                            final int position = writeBuffer.position();
                            writeBuffer.position(0);

                            // Read data into byte array
                            final byte[] readBytes = new byte[writeBuffer.remaining()];
                            writeBuffer.get(readBytes);

                            // Log read data
                            DATA_LOG.trace("{} data: {}", direction, new String(readBytes));

                            // Return to initial position
                            writeBuffer.position(position);
                        }

                        direction.getProcessor().process(new Write(direction, writeBuffer));

                        assert readBuffer.usedCapacity() == 0 : "The read buffer should have been completely drained";
                    }
                    else if (bytes == -1) {
                        // Close the connection
                        handle.cancel();
                        direction.getProcessor().process(new Close(direction));
                        final ConnectionImpl conn = (ConnectionImpl) connection;
                        final Direction otherDirection = conn.otherDirection(direction);
                        LOG.debug("{} : Closed {} ", this, otherDirection);
                        otherDirection.close();

                        assert readBuffer.usedCapacity() == 0 : "The read buffer should be empty";
                    }

                    assert bytes != 0 || readBuffer.usedCapacity() == 0 : "The read buffer should be empty";
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
            LOG.warn("{} : Unexpected key state {}", this, handle);
        }
    }
}
