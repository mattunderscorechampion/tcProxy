package com.mattunderscore.tcproxy.examples.workers;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.mattunderscore.tcproxy.io.IOSocketChannel;

/**
 * Consumer worker. Makes blocking reads from a channel and discards the data.
 * @author Matt Champion on 09/10/2015
 */
public final class Consumer extends AbstractWorker {
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);
    private final IOSocketChannel channel;

    /**
     * Constructor.
     * @param channel The channel to consume from
     */
    public Consumer(IOSocketChannel channel) {
        super("example-consumer");
        this.channel = channel;
    }

    @Override
    public void doWork() throws IOException {
        channel.read(buffer);
        buffer.clear();
    }
}
