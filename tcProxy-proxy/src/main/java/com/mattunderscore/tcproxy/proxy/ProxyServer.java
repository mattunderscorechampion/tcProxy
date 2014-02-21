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

import com.mattunderscore.tcproxy.proxy.com.mattunderscore.tcproxy.settings.*;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author matt on 18/02/14.
 */
public class ProxyServer {
    private final Acceptor acceptor;
    private final WriteSelector writer;
    private final ReadSelector proxy;
    private Thread t0;
    private Thread t1;
    private Thread t2;

    public ProxyServer(final AcceptorSettings acceptorSettings,
                       final ConnectionSettings connectionSettings,
                       final InboundSocketSettings inboundSocketSettings,
                       final OutboundSocketSettings outboundSocketSettings,
                       final ReadSelectorSettings readSelectorSettings) throws IOException {
        final BlockingQueue<Connection> newConnections = new ArrayBlockingQueue<>(5000);
        final BlockingQueue<WriteQueue> newWrites = new ArrayBlockingQueue<>(5000);
        final OutboundSocketFactory socketFactory = new OutboundSocketFactory(outboundSocketSettings);
        final ConnectionFactory connectionFactory = new ConnectionFactory(connectionSettings);
        final Selector readSelector = Selector.open();
        final Selector writeSelector = Selector.open();

        acceptor = new Acceptor(acceptorSettings, inboundSocketSettings, connectionFactory, socketFactory, newConnections);
        proxy = new ReadSelector(readSelector,readSelectorSettings, newConnections, newWrites);
        writer = new WriteSelector(writeSelector, newWrites);
    }

    public void start() {
        t0 = new Thread(acceptor);
        t1 = new Thread(proxy);
        t2 = new Thread(writer);
        t0.setDaemon(false);
        t1.setDaemon(false);
        t2.setDaemon(false);
        t0.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });
        t1.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });
        t2.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });
        t0.start();
        t1.start();
        t2.start();
    }
}
