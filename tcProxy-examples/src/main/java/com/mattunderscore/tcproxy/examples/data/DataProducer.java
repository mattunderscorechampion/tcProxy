package com.mattunderscore.tcproxy.examples.data;

import java.nio.ByteBuffer;

/**
 * Data producer. Returns a {@link ByteBuffer} that can written to a channel.
 * @author Matt Champion on 10/10/2015
 */
public interface DataProducer {
    /**
     * @return A {@link ByteBuffer} containing data
     */
    ByteBuffer getData();
}
