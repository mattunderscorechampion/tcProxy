package com.mattunderscore.tcproxy.examples.workers;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.mattunderscore.tcproxy.examples.data.DataProducer;
import com.mattunderscore.tcproxy.io.IOSocketChannel;

/**
 * Producer worker. Uses blocking writes to a channel.
 * @author Matt Champion on 09/10/2015
 */
public final class Producer extends AbstractWorker {
    private final IOSocketChannel channel;
    private final DataProducer dataProducer;

    /**
     * Constructor.
     * @param channel The channel to produce onto
     * @param dataProducer The data producer
     */
    public Producer(IOSocketChannel channel, DataProducer dataProducer) {
        super("example-producer");
        this.channel = channel;
        this.dataProducer = dataProducer;
    }

    @Override
    public void doWork() throws IOException {
        final ByteBuffer buffer = dataProducer.getData();
        channel.write(buffer);
    }
}
