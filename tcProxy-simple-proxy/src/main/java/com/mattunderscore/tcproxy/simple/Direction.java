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

package com.mattunderscore.tcproxy.simple;

import java.io.IOException;

import com.mattunderscore.tcproxy.io.data.CircularBuffer;
import com.mattunderscore.tcproxy.io.selection.IOSelectionKey;
import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;
import com.mattunderscore.tcproxy.selector.SelectionRunnable;
import com.mattunderscore.tcproxy.selector.SocketChannelSelector;
import com.mattunderscore.tcproxy.selector.general.RegistrationHandle;

/**
 * @author Matt Champion on 15/04/2016
 */
/* package */ final class Direction {
    private final SocketChannelSelector selector;
    private final CircularBuffer buffer;
    private final IOSocketChannel source;
    private final IOSocketChannel destination;

    public Direction(
            SocketChannelSelector selector,
            CircularBuffer buffer,
            IOSocketChannel source,
            IOSocketChannel destination) {
        this.selector = selector;
        this.buffer = buffer;
        this.source = source;
        this.destination = destination;
    }

    public void registerForRead() {

        selector.register(source, IOSelectionKey.Op.READ, new SelectionRunnable<IOSocketChannel>() {
            @Override
            public void run(IOSocketChannel socket, RegistrationHandle handle) {
                final int read;
                try {
                    read = socket.read(buffer);
                }
                catch (IOException e) {
                    // Perform close
                    return;
                }

                if (read < 0) {
                    // Handle close
                }
                else {
                    if (buffer.usedCapacity() > 0) {
                        registerForWrite();
                    }
                }
            }
        });
    }

    public void registerForWrite() {

        selector.register(destination, IOSelectionKey.Op.WRITE, new SelectionRunnable<IOSocketChannel>() {
            @Override
            public void run(IOSocketChannel socket, RegistrationHandle handle) {
                if (buffer.usedCapacity() == 0) {
                    handle.cancel();
                }

                try {
                    socket.write(buffer);
                }
                catch (IOException e) {
                    // Perform close
                }
            }
        });
    }
}
