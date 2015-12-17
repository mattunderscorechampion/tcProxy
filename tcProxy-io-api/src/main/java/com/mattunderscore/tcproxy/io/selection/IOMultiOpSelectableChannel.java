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

package com.mattunderscore.tcproxy.io.selection;

import java.nio.channels.ClosedChannelException;
import java.util.Set;

/**
 * A channel that can have {@link IOSelectionKey}s register for it with a selector with an interest in a several types
 * of operation.
 * @author Matt Champion on 01/12/2015
 */
public interface IOMultiOpSelectableChannel extends IOSelectableChannel {
    /**
     * Register the channel with a selector.
     * @param selector The selector
     * @param op The operation
     * @param att A attachment
     * @return The selection key
     * @throws ClosedChannelException
     */
    IOSelectionKey register(IOSelector selector, IOSelectionKey.Op op, Object att) throws ClosedChannelException;

    /**
     * Register the channel with a selector.
     * @param selector The selector
     * @param ops The operations
     * @param att A attachment
     * @return The selection key
     * @throws ClosedChannelException
     */
    IOSelectionKey register(IOSelector selector, Set<IOSelectionKey.Op> ops, Object att) throws ClosedChannelException;
}
