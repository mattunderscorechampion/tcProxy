package com.mattunderscore.tcproxy.examples.data;

import java.nio.ByteBuffer;

/**
 * Data producer.
 * @author Matt Champion on 10/10/2015
 */
public interface DataProducer {
    /**
     * @return A byte array containing data
     */
    ByteBuffer getData();
}
