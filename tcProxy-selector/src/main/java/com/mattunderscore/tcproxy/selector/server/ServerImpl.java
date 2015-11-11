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

package com.mattunderscore.tcproxy.selector.server;

import java.util.Collection;
import java.util.Collections;

import com.mattunderscore.tcproxy.selector.threads.RestartableTask;
import com.mattunderscore.tcproxy.selector.threads.RestartableThread;

/**
 * A basic server implementation.
 * @author Matt Champion on 09/11/2015
 */
public final class ServerImpl implements Server {
    private final Collection<RestartableThread> selectorThreads;

    public ServerImpl(RestartableThread selectorThread) {
        this(Collections.singleton(selectorThread));
    }

    public ServerImpl(Collection<RestartableThread> selectorThreads) {
        this.selectorThreads = selectorThreads;
    }

    @Override
    public void start() {
        for (RestartableTask selector : selectorThreads) {
            selector.start();
        }
    }

    @Override
    public void stop() {
        for (RestartableTask selector : selectorThreads) {
            selector.stop();
        }
    }

    @Override
    public void restart() {
        stop();
        waitForStopped();
        start();
    }

    @Override
    public void waitForRunning() {
        for (RestartableTask selector : selectorThreads) {
            selector.waitForRunning();
        }
    }

    @Override
    public void waitForStopped() {
        for (RestartableTask selector : selectorThreads) {
            selector.waitForStopped();
        }
    }
}
