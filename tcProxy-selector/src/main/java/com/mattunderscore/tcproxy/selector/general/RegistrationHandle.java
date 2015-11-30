package com.mattunderscore.tcproxy.selector.general;

import java.util.Set;

import com.mattunderscore.tcproxy.io.IOSelectionKey;

/**
 * Represent the registration of a channel with a selector for a specific operation.
 * @author Matt Champion on 30/11/2015
 */
public interface RegistrationHandle {
    boolean isValid();

    boolean isAcceptable();

    boolean isConnectable();

    boolean isReadable();

    boolean isWritable();

    void cancel();

    Set<IOSelectionKey.Op> readyOperations();
}
