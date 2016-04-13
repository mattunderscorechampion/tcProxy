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

package com.mattunderscore.tcproxy.proxy.order;

import com.mattunderscore.tcproxy.proxy.ProxyServerFactory;
import com.mattunderscore.tcproxy.proxy.settings.ConnectionSettings;
import com.mattunderscore.tcproxy.proxy.settings.OutboundSocketSettings;
import com.mattunderscore.tcproxy.proxy.settings.ProxyServerSettings;
import com.mattunderscore.tcproxy.proxy.settings.ReadSelectorSettings;
import com.mattunderscore.tcproxy.selector.server.AcceptSettings;
import com.mattunderscore.tcproxy.selector.server.Server;
import com.mattunderscore.tcproxy.selector.server.SocketSettings;

/**
 * Test that data is passed in the correct order.
 * @author Matt Champion on 02/04/2016
 */
public final class OrderTest {

    public static void main(String[] args) throws InterruptedException {
        final OrderServer serverTask = new OrderServer();
        final Thread server = new Thread(serverTask);
        final OrderClient clientTask0 = new OrderClient();
        final OrderClient clientTask1 = new OrderClient();
        final Thread client0 = new Thread(clientTask0);
        final Thread client1 = new Thread(clientTask1);
        final Thread.UncaughtExceptionHandler exceptionHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                serverTask.stop();
                clientTask0.stop();
                clientTask1.stop();
                e.printStackTrace();
            }
        };
        server.setUncaughtExceptionHandler(exceptionHandler);
        client0.setUncaughtExceptionHandler(exceptionHandler);
        server.start();

        serverTask.awaitStart();

        final Server proxy = ProxyServerFactory.factory()
            .create(ProxyServerSettings
                .builder()
                .acceptSettings(AcceptSettings
                    .builder()
                    .listenOn(8085)
                    .build())
                .connectionSettings(ConnectionSettings
                    .builder()
                    .batchSize(1024)
                    .writeQueueSize(1024)
                    .build())
                .inboundSocketSettings(SocketSettings
                    .builder()
                    .receiveBuffer(1024)
                    .sendBuffer(1024)
                    .build())
                .outboundSocketSettings(
                    OutboundSocketSettings
                        .builder()
                        .port(8080)
                        .host("localhost")
                        .receiveBuffer(1024)
                        .sendBuffer(1024)
                        .build())
                .readSelectorSettings(ReadSelectorSettings
                    .builder()
                    .readBufferSize(1024)
                    .build())
                .build());

        proxy.start();
        proxy.waitForRunning();

        client0.start();
        client1.start();

        server.join();
        client0.join();
        client1.join();

        proxy.stop();
    }
}
