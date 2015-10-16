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

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOSelector;
import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.proxy.action.Action;
import com.mattunderscore.tcproxy.proxy.action.queue.ActionQueue;
import com.mattunderscore.tcproxy.proxy.connection.Connection;
import com.mattunderscore.tcproxy.proxy.direction.Direction;
import com.mattunderscore.tcproxy.proxy.direction.DirectionAndConnection;

/**
 * The write selector for the proxy.
 * @author Matt Champion on 19/02/14.
 */
public final class WriteSelector extends AbstractSelector {
    public static final Logger LOG = LoggerFactory.getLogger("writer");
    private final BlockingQueue<DirectionAndConnection> newDirections;
    private final Set<DirectionAndConnection> directions = new HashSet<>();

    public WriteSelector(final IOSelector selector, final BlockingQueue<DirectionAndConnection> newDirections) {
        super(IOSelectionKey.Op.WRITE, selector, new BinaryBackoff(1L));
        this.newDirections = newDirections;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    protected void registerKeys() {
        newDirections.drainTo(directions);
        for (final DirectionAndConnection dc : directions) {
            final Direction direction = dc.getDirection();
            final IOSocketChannel channel = direction.getTo();
            try {
                register(channel, dc);
            }
            catch (final ClosedChannelException e) {
                LOG.debug("{} : The destination of {} is already closed", this, direction);
            }
        }
        directions.clear();
    }

    @Override
    protected void processKeys(Set<IOSelectionKey> keys) {
        for (final IOSelectionKey key : keys) {
            final DirectionAndConnection dc = (DirectionAndConnection) key.attachment();
            if (!key.isValid()) {
                LOG.debug("{} : Selected key no longer valid, closing connection", this);
                closeConnection(dc.getConnection());
            }
            else if (key.isWritable()) {
                final Direction direction = dc.getDirection();
                final ActionQueue write = direction.getQueue();

                synchronized (write) {
                    try {
                        if (write.hasData()) {
                            final Action data = write.current();
                            if (key.isValid()) {
                                data.writeToSocket();
                            }
                            else {
                                LOG.debug("{} : Selected key no longer valid, closing connection", this);
                                closeConnection(dc.getConnection());
                            }
                        }
                        else {
                            LOG.debug("{} : Finished queued actions, cancel key", this);
                            key.cancel();
                        }
                    }
                    catch (final IOException e) {
                        LOG.warn("{} : Error writing", this, e);
                        key.cancel();
                        closeConnection(dc.getConnection());
                    }
                }
            }
            else {
                LOG.debug("{} : Unexpected key state {}", this, key);
            }
        }
    }

    /**
     * Close the connection and log any exception.
     * @param conn The connection to close
     */
    private void closeConnection(Connection conn) {
        try {
            conn.close();
        }
        catch (IOException e) {
            LOG.warn("{} : Error closing connection", this, e);
        }
    }

    @Override
    public String toString() {
        return "Write Selector";
    }
}
