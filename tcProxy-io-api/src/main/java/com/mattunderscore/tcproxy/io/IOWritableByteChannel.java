package com.mattunderscore.tcproxy.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * A channel that can have bytes written to it from a {@link CircularBuffer} or {@link ByteBuffer}.
 * @author Matt Champion on 01/12/2015
 */
public interface IOWritableByteChannel extends WritableByteChannel {
    /**
     * Writes data from the circular buffer to the socket.
     * @param src The buffer
     * @return The number of bytes written
     * @throws IOException
     */
    int write(CircularBuffer src) throws IOException;
}
