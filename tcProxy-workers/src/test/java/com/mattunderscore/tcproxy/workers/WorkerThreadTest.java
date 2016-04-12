package com.mattunderscore.tcproxy.workers;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

/**
 * Unit tests for {@link WorkerThread}.
 *
 * @author Matt Champion on 12/04/2016
 */
public final class WorkerThreadTest {

    @Test
    public void start() {
        final StoppingTestRunnable runnable = new StoppingTestRunnable();
        final WorkerThread thread = new WorkerThread(
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r);
                }
            },
            runnable);

        runnable.workerRef.set(thread);

        thread.start();

        thread.waitForRunning();
        thread.waitForStopped();

        assertEquals(1, runnable.started.get());
        assertEquals(1, runnable.run.get());
        assertEquals(1, runnable.stopped.get());
    }

    private static final class StoppingTestRunnable implements WorkerRunnable {
        private final AtomicInteger started = new AtomicInteger(0);
        private final AtomicInteger stopped = new AtomicInteger(0);
        private final AtomicInteger run = new AtomicInteger(0);
        private final AtomicReference<Worker> workerRef = new AtomicReference<>(null);

        @Override
        public void onStart() {
            started.incrementAndGet();
        }

        @Override
        public void run() {
            run.incrementAndGet();
            final Worker worker = workerRef.get();
            worker.stop();
        }

        @Override
        public void onStop() {
            stopped.incrementAndGet();
        }
    }
}
