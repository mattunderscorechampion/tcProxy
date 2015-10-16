package com.mattunderscore.tcproxy.examples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.mattunderscore.tcproxy.examples.data.RandomDataProducer;
import com.mattunderscore.tcproxy.examples.data.ThrottledDataProducer;
import com.mattunderscore.tcproxy.examples.workers.Acceptor;
import com.mattunderscore.tcproxy.examples.workers.Consumer;
import com.mattunderscore.tcproxy.examples.workers.Producer;
import com.mattunderscore.tcproxy.io.IOServerSocketChannel;
import com.mattunderscore.tcproxy.io.IOSocketChannel;
import com.mattunderscore.tcproxy.io.impl.StaticIOFactory;
import com.mattunderscore.tcproxy.proxy.connection.ConnectionManager;
import com.mattunderscore.tcproxy.proxy.ProxyServer;
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
    public static void main(String[] args) throws IOException, InterruptedException {
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

        // Start an acceptor
        final IOServerSocketChannel acceptorChannel = StaticIOFactory.openServerSocket();
        acceptorChannel.bind(new InetSocketAddress("localhost", 8080));
        final BlockingQueue<IOSocketChannel> channels = new ArrayBlockingQueue<>(2);
        final Acceptor acceptor = new Acceptor(acceptorChannel, channels);
        acceptor.start();

        // Start a producer
        final IOSocketChannel producerChannel = StaticIOFactory.openSocket();
        producerChannel.connect(new InetSocketAddress("localhost", 8085));
        final Producer producer = new Producer(
            producerChannel,
            new ThrottledDataProducer(
                100L,
                new RandomDataProducer(32)));
        producer.start();

        // Start a consumer
        final IOSocketChannel consumerChannel = channels.take();
        final Consumer consumer = new Consumer(consumerChannel);
        consumer.start();

        // Keep going
        System.out.print("Press return to stop: ");
        System.in.read();
        server.stop();
    }
}
