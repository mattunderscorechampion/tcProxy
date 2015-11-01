package com.mattunderscore.tcproxy.io;

import java.nio.ByteBuffer;

/**
 * A circular buffer.
 * @author Matt Champion on 31/10/2015
 */
public interface CircularBuffer {

    /**
     * Put a single byte into the buffer.
     * @param b The byte
     * @return false if there was no room in the buffer
     */
    boolean put(byte b);

    /**
     * Put an entire array of bytes into the buffer.
     * @param bytes The byte array
     * @return false if there was not enough room in the buffer
     */
    boolean put(byte[] bytes);

    /**
     * Put some data from a {@link ByteBuffer} into the buffer. If the source contains more data then can fit into the
     * buffer, some data will be written.
     * @param src The source buffer
     * @return The number of bytes copied into the buffer
     */
    int put(ByteBuffer src);

    /**
     * @return A single byte from the buffer
     * @throws IllegalStateException If no data can be read
     */
    byte get();

    /**
     * @param dst A {@link ByteBuffer} to copy data into
     * @return A number of bytes copied to the destination
     */
    int get(ByteBuffer dst);

    /**
     * @return The number of bytes that can be written to the buffer
     */
    int freeCapacity();

    /**
     * @return The number of bytes that can be read from the buffer
     */
    int usedCapacity();
}
