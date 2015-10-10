package com.mattunderscore.tcproxy.examples.workers;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.mattunderscore.tcproxy.io.IOSocketChannel;

/**
 * Consumer worker.
 * @author Matt Champion on 09/10/2015
 */
public final class Consumer extends AbstractWorker {
    private final IOSocketChannel channel;

    /**
     * Constructor.
     * @param channel The channel to consume from
     */
    public Consumer(IOSocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public String getName() {
        return "example-consumer";
    }

    @Override
    public WorkerRunnable getTask() {
        return new ConsumerRunnable();
    }

    private final class ConsumerRunnable implements WorkerRunnable {
        private final ByteBuffer buffer = ByteBuffer.allocate(1024);

        @Override
        public void run() throws IOException {
            channel.read(buffer);
            buffer.clear();
        }
    }
}
