package com.mattunderscore.tcproxy.io;

/**
 * A circular buffer.
 * @author Matt Champion on 31/10/2015
 */
public interface CircularBuffer {

    boolean put(byte b);

    boolean put(byte[] bytes);

    byte get();

    int freeCapacity();

    int occupied();
}
