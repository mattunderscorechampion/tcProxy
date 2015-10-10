package com.mattunderscore.tcproxy.examples.data;

import java.nio.ByteBuffer;

/**
 * Throttled data producer.
 * @author Matt Champion on 10/10/2015
 */
public final class ThrottledDataProducer implements DataProducer {
    private final long delay;
    private final DataProducer dataProducer;

    /**
     * Constructor.
     * @param delay The delay in milliseconds before returning data
     * @param dataProducer The throttled producer
     */
    public ThrottledDataProducer(long delay, DataProducer dataProducer) {
        this.delay = delay;
        this.dataProducer = dataProducer;
    }

    @Override
    public ByteBuffer getData() {
        try {
            Thread.sleep(delay);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return dataProducer.getData();
    }
}
