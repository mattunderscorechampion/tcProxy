package com.mattunderscore.tcproxy.examples.data;

import java.nio.ByteBuffer;

/**
 * Producer of null byte vales.
 * @author Matt Champion on 10/10/2015
 */
public final class NullByteDataProducer implements DataProducer {
    private final ByteBuffer buffer;

    /**
     * Constructor.
     */
    public NullByteDataProducer() {
        buffer = ByteBuffer.allocate(128);
        buffer.limit(127);
    }

    @Override
    public ByteBuffer getData() {
        return buffer.asReadOnlyBuffer();
    }
}
