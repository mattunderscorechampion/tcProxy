package com.mattunderscore.tcproxy.examples.data;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Producer of random data. Produces random length, random values.
 * @author Matt Champion on 10/10/2015
 */
public final class RandomDataProducer implements DataProducer {
    private final Random random = new Random();
    private final int maxLength;

    /**
     * Constructor.
     * @param maxLength The maximum length of data
     */
    public RandomDataProducer(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public ByteBuffer getData() {
        final int length = random.nextInt(maxLength);
        final byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return ByteBuffer.wrap(bytes);
    }
}
