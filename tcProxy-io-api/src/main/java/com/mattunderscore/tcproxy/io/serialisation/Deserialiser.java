package com.mattunderscore.tcproxy.io.serialisation;

/**
 * A deserialiser capable of reading objects from a circular buffer.
 * @param <T> The type of object to deserialise.
 * @param <U> The type of buffer to deserialise from.
 * @author Matt Champion on 11/01/16
 */
public interface Deserialiser<T, U> {
    /**
     * Attempt to read an object from the buffer. Only consumes from the buffer if value is deserialised.
     * @param buffer The buffer to read from
     * @return The result of reading
     */
    Result<T> read(U buffer);

    /**
     * The result of reading an object.
     * @param <T> The type of object that might be read
     */
    interface Result<T> {
        /**
         * @return {@code true} if the buffer does not contain enough data to deserialise an object
         */
        boolean needsMoreData();

        /**
         * @return {@code true} if there is more data in the buffer after reading an object
         */
        boolean hasMoreData();

        /**
         * @return {@code true} if the deserialiser cannot parse the data in the buffer
         */
        boolean notDeserialisable();

        /**
         * @return {@code true} if the deserialiser parsed a value
         */
        boolean hasResult();

        /**
         * @return A deserialised object
         * @throws IllegalStateException If no object has been read
         */
        T result();

        /**
         * @return The number of bytes processed
         */
        int bytesProcessed();
    }
}
