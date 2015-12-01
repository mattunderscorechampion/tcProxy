package com.mattunderscore.tcproxy.io;

import java.nio.channels.ByteChannel;

/**
 * A channel that can have bytes read from it and written to it.
 * @author Matt Champion on 01/12/2015
 */
public interface IOByteChannel extends IOReadableByteChannel, IOWritableByteChannel, ByteChannel {
}
