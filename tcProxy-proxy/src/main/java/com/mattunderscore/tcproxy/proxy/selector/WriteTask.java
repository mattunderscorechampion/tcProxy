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
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.io.IOSelector;
import com.mattunderscore.tcproxy.io.impl.StaticIOFactory;
import com.mattunderscore.tcproxy.proxy.direction.DirectionAndConnection;
import com.mattunderscore.tcproxy.selector.SocketChannelSelector;
import com.mattunderscore.tcproxy.selector.general.GeneralPurposeSelector;
import com.mattunderscore.tcproxy.selector.threads.RestartableTask;

/**
 * @author Matt Champion on 18/11/2015
 */
public class WriteTask implements Runnable {
    public static final Logger LOG = LoggerFactory.getLogger("reader");
    private final CountDownLatch readyLatch = new CountDownLatch(1);
    private volatile RestartableTask task;
    private volatile SocketChannelSelector selector;

    /**
     * Constructor.
     */
    public WriteTask() {
    }

    @Override
    public void run() {
        try {
            final IOSelector ioSelector = StaticIOFactory.openSelector();

            selector = new GeneralPurposeSelector(ioSelector);

            task = selector;
            readyLatch.countDown();
            selector.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            readyLatch.await();
            task.stop();
        }
        catch (InterruptedException e) {
            LOG.debug("Interrupted while trying to stop");
        }
    }

    public void waitForReady() throws InterruptedException {
        readyLatch.await();
        task.waitForRunning();
    }

    public void registerNewWork(DirectionAndConnection dc) {
        selector.register(dc.getDirection().getTo(), IOSelectionKey.Op.WRITE, new WriteSelectionRunnable(dc));
    }
}
