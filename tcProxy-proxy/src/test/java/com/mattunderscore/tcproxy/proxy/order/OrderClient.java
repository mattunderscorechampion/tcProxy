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

import static com.mattunderscore.tcproxy.io.impl.StaticIOFactory.socketFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

import com.mattunderscore.tcproxy.io.data.CircularBuffer;
import com.mattunderscore.tcproxy.io.factory.IOOutboundSocketChannelFactory;
import com.mattunderscore.tcproxy.io.impl.CircularBufferImpl;
import com.mattunderscore.tcproxy.io.socket.IOOutboundSocketChannel;
import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;

/**
 * @author Matt Champion on 03/04/2016
 */
public final class OrderClient implements Runnable {
    private volatile boolean running = false;

    private final CountDownLatch runningLatch = new CountDownLatch(1);
    private byte lastValueReceived = 0x0;
    private byte lastValueSent = 0x0;

    public OrderClient() {
    }

    @Override
    public void run() {
        try {
            final CircularBuffer buffer = CircularBufferImpl.allocateDirect(64);

            final IOOutboundSocketChannel socketChannel = socketFactory(IOOutboundSocketChannelFactory.class).create();
            socketChannel.connect(new InetSocketAddress("localhost", 8085));

            System.out.println("Connected " + socketChannel);

            running = true;
            runningLatch.countDown();

            while (running) {
                sendNextValue(buffer, socketChannel);
                readAndCheckValues(buffer, socketChannel);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void awaitStart() throws InterruptedException {
        runningLatch.await();
    }

    public void stop() {
        running = false;
    }

    private void readAndCheckValues(CircularBuffer buffer, IOSocketChannel socketChannel) throws IOException {
        socketChannel.read(buffer);

        while (buffer.usedCapacity() > 0) {
            final byte nextValue = buffer.get();
            if (nextValue != lastValueReceived + 1) {
                throw new AssertionError("Expected " + (lastValueReceived + 1) + " was " + nextValue);
            }
            else {
                System.out.println("Received " + nextValue);
            }
            lastValueReceived = (byte) (nextValue % 127);
        }
    }

    private void sendNextValue(CircularBuffer buffer, IOSocketChannel socketChannel) throws IOException {
        lastValueSent = (byte) ((lastValueSent % 127) + 0x1);
        buffer.put(lastValueSent);
        socketChannel.write(buffer);
        System.out.println("Client sent " + lastValueSent);
    }
}
