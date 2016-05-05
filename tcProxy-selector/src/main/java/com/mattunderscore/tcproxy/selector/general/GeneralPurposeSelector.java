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

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.selection.IOSelectionKey;
import com.mattunderscore.tcproxy.io.selection.IOSelector;
import com.mattunderscore.tcproxy.io.socket.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;
import com.mattunderscore.tcproxy.selector.SelectionRunnable;
import com.mattunderscore.tcproxy.selector.SelectorBackoff;
import com.mattunderscore.tcproxy.selector.ServerSocketChannelSelector;
import com.mattunderscore.tcproxy.selector.SocketChannelSelector;

/**
 * A general purpose selector. {@link SelectionRunnable}s can be registered against it for both
 * {@link IOServerSocketChannel} and {@link IOSocketChannel}. These tasks can be registered from any thread. Selected
 * keys will be removed from the selected set when they are processed but will not be cancelled.
 * @author Matt Champion on 24/10/2015
 */
public final class GeneralPurposeSelector implements SocketChannelSelector, ServerSocketChannelSelector {
    private static final Logger LOG = LoggerFactory.getLogger("selector");
    private final BlockingQueue<RegistrationRequest> registrations = new ArrayBlockingQueue<>(64);
    private final IOSelector selector;
    private final SelectorBackoff backoff;

    public GeneralPurposeSelector(IOSelector selector, SelectorBackoff backoff) {
        this.selector = selector;
        this.backoff = backoff;
    }

    @Override
    public void onStart() {
    }

    @Override
    public void run() {
        // Populate the selected set
        try {
            selector.selectNow();
        }
        catch (final IOException e) {
            LOG.debug("{} : Error selecting keys", this, e);
        }

        // Process any new registrations that have been requested
        final Collection<RegistrationRequest> newRegistrations = new HashSet<>();
        registrations.drainTo(newRegistrations);
        for (final RegistrationRequest registrationRequest : newRegistrations) {
            try {
                registrationRequest.register(selector);
            }
            catch (ClosedChannelException e) {
                LOG.debug("{} : Problem registering key", this, e);
            }
        }

        // Process the selector set
        final Set<IOSelectionKey> selectedKeySet = selector.selectedKeys();
        final int selectedSize = selectedKeySet.size();
        final Iterator<IOSelectionKey> selectedKeys = selectedKeySet.iterator();
        while (selectedKeys.hasNext()) {
            final IOSelectionKey key = selectedKeys.next();
            selectedKeys.remove();
            final Registration registration = (Registration) key.attachment();
            registration.run(key);
        }

        backoff.backoff(selectedSize);
    }

    @Override
    public void onStop() {
    }

    @Override
    public void register(IOSocketChannel channel, IOSelectionKey.Op op, SelectionRunnable<IOSocketChannel> runnable) {
        registrations.add(new IOSocketChannelRegistrationRequest(channel, Collections.singleton(op), runnable));
    }

    @Override
    public void register(IOSocketChannel channel, Set<IOSelectionKey.Op> ops, SelectionRunnable<IOSocketChannel> runnable) {
        registrations.add(new IOSocketChannelRegistrationRequest(channel, ops, runnable));
    }

    @Override
    public void register(IOServerSocketChannel channel, SelectionRunnable<IOServerSocketChannel> runnable) {
        registrations.add(new IOServerSocketChannelRegistrationRequest(channel, runnable));
    }

    @Override
    public String toString() {
        return "General Purpose selector";
    }
}
