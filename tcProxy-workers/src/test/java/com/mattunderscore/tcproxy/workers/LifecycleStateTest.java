package com.mattunderscore.tcproxy.workers;

import static com.mattunderscore.tcproxy.workers.LifecycleState.State.RUNNING;
import static com.mattunderscore.tcproxy.workers.LifecycleState.State.STOPPED;
import static com.mattunderscore.tcproxy.workers.LifecycleState.State.STOPPING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link LifecycleState}.
 *
 * @author Matt Champion on 27/11/2015
 */
public final class LifecycleStateTest {

    @Test
    public void start() {
        final LifecycleState state = new LifecycleState();
        assertFalse(state.isRunning());
        assertEquals(state.state.get(), STOPPED);
        state.beginStartup();
        assertEquals(state.state.get(), RUNNING);
        assertTrue(state.isRunning());
    }

    @Test(expected = IllegalStateException.class)
    public void stopWhenStopped() {
        final LifecycleState state = new LifecycleState();
        assertEquals(state.state.get(), STOPPED);
        state.beginShutdown();
    }

    @Test(expected = IllegalStateException.class)
    public void startWhenRunning() {
        final LifecycleState state = new LifecycleState();
        state.beginStartup();
        assertEquals(state.state.get(), RUNNING);
        state.beginStartup();
    }

    @Test
    public void stop() {
        final LifecycleState state = new LifecycleState();
        assertFalse(state.isRunning());
        assertEquals(state.state.get(), STOPPED);
        state.beginStartup();
        assertEquals(state.state.get(), RUNNING);
        assertTrue(state.isRunning());
        state.beginShutdown();
        assertEquals(state.state.get(), STOPPING);
        assertFalse(state.isRunning());
        state.endShutdown();
        assertEquals(state.state.get(), STOPPED);
        assertFalse(state.isRunning());
    }
}
