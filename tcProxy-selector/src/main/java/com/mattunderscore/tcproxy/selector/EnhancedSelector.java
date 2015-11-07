package com.mattunderscore.tcproxy.selector;

/**
 * Selector that allows {@link SelectorRunnable}s to be registered to run when a channel is ready for some operation.
 * @author Matt Champion on 31/10/2015
 */
public interface EnhancedSelector extends SocketChannelSelector, ServerSocketChannelSelector, StartStopLifecycle {
}
