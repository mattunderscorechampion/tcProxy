package com.mattunderscore.tcproxy.io.serialisation;

import java.nio.BufferOverflowException;

/**
 * A serialiser capable of writing objects to a circular buffer.
 * @param <T> The type of object to serialise.
 * @param <U> The type of buffer to serialise to.
 * @author Matt Champion on 11/01/16
 */
public interface Serialiser<T, U> {
    /**
     * Check if a buffer has enough capacity to an object write to.
     * @param object The object
     * @param buffer The buffer
     * @return Value indicating the available capacity
     */
    HasCapacity hasCapacity(T object, U buffer);

    /**
     * Write an object to a circular buffer.
     * @param object The object to write
     * @param buffer The buffer to write to
     * @throws BufferOverflowException If there is not enough free capacity in the buffer to write the object
     */
    void write(T object, U buffer) throws BufferOverflowException;

    enum HasCapacity {
        /**
         * Indicates there is enough free capacity to write the object.
         */
        HAS_CAPACITY,
        /**
         * Indicates there is not enough free capacity to write the object. The object may be writable to the buffer
         * after consuming data from it.
         */
        LACKS_FREE_CAPACITY,
        /**
         * Indicates there is not enough total capacity to write the object. The object can never be written to the
         * buffer.
         */
        LACKS_TOTAL_CAPACITY
    }
}
