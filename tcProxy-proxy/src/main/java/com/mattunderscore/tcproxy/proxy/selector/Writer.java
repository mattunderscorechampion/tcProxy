package com.mattunderscore.tcproxy.proxy.selector;

import com.mattunderscore.tcproxy.proxy.direction.DirectionAndConnection;

/**
 * Handler for new writing work.
 * @author Matt Champion on 22/11/2015
 */
public interface Writer {
    void registerNewWork(DirectionAndConnection dc);
}
