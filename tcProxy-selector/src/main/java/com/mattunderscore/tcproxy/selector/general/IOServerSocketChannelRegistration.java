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

package com.mattunderscore.tcproxy.selector.general;

import com.mattunderscore.tcproxy.io.selection.IOSelectionKey;
import com.mattunderscore.tcproxy.io.selection.IOSelectionKey.Op;
import com.mattunderscore.tcproxy.io.socket.IOServerSocketChannel;
import com.mattunderscore.tcproxy.selector.SelectionRunnable;
import net.jcip.annotations.NotThreadSafe;

import java.util.Set;

import static com.mattunderscore.tcproxy.io.selection.IOSelectionKey.Op.ACCEPT;

/**
 * Attachment for sockets that links the {@link SelectionRunnable} to an operation for a {@link IOServerSocketChannel}.
 *
 * @author Matt Champion on 06/05/16
 */
@NotThreadSafe
public final class IOServerSocketChannelRegistration implements Registration<IOServerSocketChannel> {
    private SelectionRunnable<IOServerSocketChannel> acceptOperation;

    @Override
    public void addRegistration(Op op, SelectionRunnable<IOServerSocketChannel> registration) {
        switch (op) {
            case ACCEPT:
                acceptOperation = registration;
                break;
            default:
                throw new IllegalArgumentException("Operation " + op + " not supported");
        }
    }

    @Override
    public void run(IOServerSocketChannel socket, IOSelectionKey key) {
        if (!key.isValid()) {
            return;
        }

        final Set<Op> interestedOperations = key.interestedOperations();

        if (key.isAcceptable() && interestedOperations.contains(ACCEPT)) {
            acceptOperation.run(socket, new RegistrationHandleImpl(key));
        }
    }

    public final class RegistrationHandleImpl implements RegistrationHandle {
        private final IOSelectionKey key;

        public RegistrationHandleImpl(IOSelectionKey key) {
            this.key = key;
        }

        @Override
        public boolean isValid() {
            return key.isValid();
        }

        @Override
        public boolean isAcceptable() {
            return key.isAcceptable();
        }

        @Override
        public boolean isConnectable() {
            return false;
        }

        @Override
        public boolean isReadable() {
            return false;
        }

        @Override
        public boolean isWritable() {
            return false;
        }

        @Override
        public void cancel() {
            key.clearInterestedOperation(ACCEPT);
            acceptOperation = null;
            key.cancel();
        }

        @Override
        public Set<Op> readyOperations() {
            return key.readyOperations();
        }
    }
}
