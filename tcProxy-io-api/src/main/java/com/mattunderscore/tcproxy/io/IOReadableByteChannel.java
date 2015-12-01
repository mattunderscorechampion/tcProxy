package com.mattunderscore.tcproxy.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * A channel that can have bytes read from it in to a {@link CircularBuffer} or {@link ByteBuffer}.
 * @author Matt Champion on 01/12/2015
 */
public interface IOReadableByteChannel extends ReadableByteChannel {
    /**
     * Reads data from the socket into a circular buffer
     * @param dst The buffer
     * @return The number of bytes read
     * @throws IOException
     */
    int read(CircularBuffer dst) throws IOException;
}
