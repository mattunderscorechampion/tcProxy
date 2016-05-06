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

package com.mattunderscore.tcproxy.selector.general;

import com.mattunderscore.tcproxy.io.selection.IOSelectionKey;
import com.mattunderscore.tcproxy.io.selection.IOSelectionKey.Op;
import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;
import com.mattunderscore.tcproxy.selector.SelectionRunnable;
import net.jcip.annotations.NotThreadSafe;

import java.util.Set;

import static com.mattunderscore.tcproxy.io.selection.IOSelectionKey.Op.CONNECT;
import static com.mattunderscore.tcproxy.io.selection.IOSelectionKey.Op.READ;
import static com.mattunderscore.tcproxy.io.selection.IOSelectionKey.Op.WRITE;

/**
 * Attachment for sockets that links the {@link SelectionRunnable} to an operation for a {@link IOSocketChannel}.
 *
 * @author Matt Champion on 29/11/2015
 */
@NotThreadSafe
/*package*/ final class IOSocketChannelRegistration implements Registration<IOSocketChannel> {
    private SelectionRunnable<IOSocketChannel> connectOperation;
    private SelectionRunnable<IOSocketChannel> readOperation;
    private SelectionRunnable<IOSocketChannel> writeOperation;

    @Override
    public void addRegistration(Op op, SelectionRunnable<IOSocketChannel> registration) {
        switch (op) {
            case CONNECT:
                connectOperation = registration;
                break;
            case READ:
                readOperation = registration;
                break;
            case WRITE:
                writeOperation = registration;
                break;
            default:
                throw new IllegalArgumentException("Operation " + op + " not supported");
        }
    }

    @Override
    public void run(IOSocketChannel socket, IOSelectionKey key) {
        if (!key.isValid()) {
            return;
        }

        final Set<Op> interestedOperations = key.interestedOperations();

        if (key.isConnectable() && interestedOperations.contains(CONNECT)) {
            connectOperation.run(socket, new RegistrationHandleImpl(key, CONNECT));
        }
        else if (key.isReadable() && interestedOperations.contains(READ)) {
            readOperation.run(socket, new RegistrationHandleImpl(key, READ));
        }
        else if (key.isWritable() && interestedOperations.contains(WRITE)) {
            writeOperation.run(socket, new RegistrationHandleImpl(key, WRITE));
        }
    }

    public final class RegistrationHandleImpl implements RegistrationHandle {
        private final IOSelectionKey key;
        private final Op op;

        public RegistrationHandleImpl(IOSelectionKey key, Op op) {
            this.key = key;
            this.op = op;
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
            return key.isConnectable();
        }

        @Override
        public boolean isReadable() {
            return key.isReadable();
        }

        @Override
        public boolean isWritable() {
            return key.isWritable();
        }

        @Override
        public void cancel() {
            key.clearInterestedOperation(op);
            switch (op) {
                case CONNECT:
                    connectOperation = null;
                    break;
                case READ:
                    readOperation = null;
                    break;
                case WRITE:
                    writeOperation = null;
                    break;
            }

            if (connectOperation == null && readOperation == null && writeOperation == null) {
                key.cancel();
            }
        }

        @Override
        public Set<Op> readyOperations() {
            return key.readyOperations();
        }
    }
}
