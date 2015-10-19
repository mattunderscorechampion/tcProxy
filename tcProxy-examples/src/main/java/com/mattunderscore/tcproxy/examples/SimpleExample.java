package com.mattunderscore.tcproxy.examples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.examples.data.RandomDataProducer;
import com.mattunderscore.tcproxy.examples.data.ThrottledDataProducer;
import com.mattunderscore.tcproxy.examples.workers.Acceptor;
import com.mattunderscore.tcproxy.examples.workers.Consumer;
import com.mattunderscore.tcproxy.examples.workers.Producer;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.io.impl.StaticIOFactory;
import com.mattunderscore.tcproxy.proxy.ProxyServer;
import com.mattunderscore.tcproxy.proxy.connection.ConnectionManager;
import com.mattunderscore.tcproxy.proxy.settings.AcceptorSettings;
import com.mattunderscore.tcproxy.proxy.settings.ConnectionSettings;
import com.mattunderscore.tcproxy.proxy.settings.InboundSocketSettings;
import com.mattunderscore.tcproxy.proxy.settings.OutboundSocketSettings;
import com.mattunderscore.tcproxy.proxy.settings.ReadSelectorSettings;

/**
 * Simple example with a single consumer and producer. The producer sends throttled random data to the consumer through
 * a proxy.
 * @author Matt Champion on 09/10/2015
 */
public final class SimpleExample {
    private static final Logger LOG = LoggerFactory.getLogger("example");

    public static void main(String[] args) throws IOException {
        // Start the proxy
        final ProxyServer server = new ProxyServer(
            new AcceptorSettings(8085),
            new ConnectionSettings(1024, 1024),
            new InboundSocketSettings(1024, 1024),
            new OutboundSocketSettings(
                8080,
                "localhost",
                1024,
                1024),
            new ReadSelectorSettings(1024),
            new ConnectionManager());
        server.start();

        final BlockingQueue<IOSocketChannel> channels = new ArrayBlockingQueue<>(2);
        final Acceptor acceptor;
        try {
            // Start an acceptor
            final IOServerSocketChannel acceptorChannel = StaticIOFactory
                .socketFactory(IOServerSocketChannel.class)
                .bind(new InetSocketAddress("localhost", 8080))
                .create();
            acceptor = new Acceptor(acceptorChannel, channels);
            acceptor.start();
        }
        catch (IOException e) {
            LOG.error("Error creating acceptor", e);
            server.stop();
            return;
        }

        final Producer producer;
        try {
            // Start a producer
            final IOSocketChannel producerChannel = StaticIOFactory.socketFactory(IOSocketChannel.class).create();
            producerChannel.connect(new InetSocketAddress("localhost", 8085));
            producer = new Producer(
                producerChannel,
                new ThrottledDataProducer(
                    100L,
                    new RandomDataProducer(32)));
            producer.start();
        }
        catch (IOException e) {
            LOG.error("Error creating producer", e);
            server.stop();
            acceptor.stop();
            return;
        }

        try {
            // Start a consumer
            final IOSocketChannel consumerChannel = channels.take();
            final Consumer consumer = new Consumer(consumerChannel);
            consumer.start();
        }
        catch (InterruptedException e) {
            LOG.error("Error creating consumer", e);
            server.stop();
            server.stop();
            acceptor.stop();
            producer.stop();
            return;
        }

        // Keep going
        System.out.print("Press return to stop: ");
        System.in.read();
        server.stop();
    }
}
