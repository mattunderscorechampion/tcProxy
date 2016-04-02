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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;
import com.mattunderscore.tcproxy.proxy.action.Action;
import com.mattunderscore.tcproxy.proxy.action.queue.ActionQueue;
import com.mattunderscore.tcproxy.proxy.connection.Connection;
import com.mattunderscore.tcproxy.proxy.direction.Direction;
import com.mattunderscore.tcproxy.selector.SelectionRunnable;
import com.mattunderscore.tcproxy.selector.general.RegistrationHandle;

/**
 * Proxy write task.
 * @author Matt Champion on 22/11/2015
 */
public final class WriteSelectionRunnable implements SelectionRunnable<IOSocketChannel> {
    public static final Logger LOG = LoggerFactory.getLogger("writer");
    private final Direction direction;
    private final Connection connection;

    public WriteSelectionRunnable(Direction direction, Connection connection) {
        this.direction = direction;
        this.connection = connection;
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
        else if (handle.isWritable()) {
            final ActionQueue write = direction.getQueue();

            synchronized (write) {
                try {
                    if (write.hasData()) {
                        if (handle.isValid()) {
                            final Action data = write.head();
                            data.writeToSocket();
                        }
                        else {
                            LOG.debug("{} : Selected key no longer valid, closing connection", this);
                            try {
                                connection.close();
                            }
                            catch (IOException e) {
                                LOG.warn("{} : Error closing connection", this, e);
                            }
                        }
                    }
                    else {
                        LOG.debug("{} : Finished queued actions, cancel key", this);
                        handle.cancel();
                    }
                }
                catch (final IOException e) {
                    LOG.warn("{} : Error writing", this, e);
                    handle.cancel();
                    try {
                        connection.close();
                    }
                    catch (IOException closeError) {
                        LOG.warn("{} : Error closing connection", this, closeError);
                    }
                }
            }
        }
        else {
            LOG.warn("{} : Unexpected key state {}", this, handle);
        }
    }
}
