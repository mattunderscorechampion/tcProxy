package com.mattunderscore.tcproxy.examples.workers;

import java.io.IOException;
import java.util.Queue;

import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketChannel;

/**
 * Acceptor worker.
 * @author Matt Champion on 10/10/2015
 */
public final class Acceptor extends AbstractWorker {
    private final IOServerSocketChannel channel;
    private final Queue<IOSocketChannel> channels;

    /**
     * Constructor.
     * @param channel The channel to accept from
     * @param channels The queue to put accepted channels onto
     */
    public Acceptor(IOServerSocketChannel channel, Queue<IOSocketChannel> channels) {
        this.channel = channel;
        this.channels = channels;
    }

    @Override
    public String getName() {
        return "example-acceptor";
    }

    @Override
    public WorkerRunnable getTask() {
        return new AcceptorRunnable();
    }

    private final class AcceptorRunnable implements WorkerRunnable {
        @Override
        public void run() throws IOException{
            channels.add(channel.accept());
        }
    }
}
