package com.mattunderscore.tcproxy.io;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * A circular buffer.
 * @author Matt Champion on 31/10/2015
 */
public interface CircularBuffer {

    /**
     * Put a single byte into the buffer.
     * @param b The byte
     * @throws BufferOverflowException if there is not enough room in the buffer
     */
    void put(byte b) throws BufferOverflowException;

    /**
     * Put an entire array of bytes into the buffer.
     * @param bytes The byte array
     * @throws BufferOverflowException if there is not enough room in the buffer
     */
    void put(byte[] bytes) throws BufferOverflowException;

    /**
     * Put some data from a {@link ByteBuffer} into the buffer. If the source contains more data then can fit into the
     * buffer, some data will be written.
     * @param src The source buffer
     * @return The number of bytes copied into the buffer
     */
    int put(ByteBuffer src);

    /**
     * @return A single byte from the buffer
     * @throws BufferUnderflowException if there is no data to read from the buffer
     */
    byte get() throws BufferUnderflowException;

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
