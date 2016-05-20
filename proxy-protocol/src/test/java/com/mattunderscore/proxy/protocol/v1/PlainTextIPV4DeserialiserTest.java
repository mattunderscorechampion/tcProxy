package com.mattunderscore.proxy.protocol.v1;

import com.mattunderscore.tcproxy.io.serialisation.Deserialiser.Result;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * @author Matt Champion on 21/05/16
 */
public final class PlainTextIPV4DeserialiserTest {

    @Test
    public void local() throws UnknownHostException {
        final ByteBuffer buffer = ByteBuffer.wrap("127.0.0.1".getBytes());
        final Result<InetAddress> address = PlainTextIPV4Deserialiser.INSTANCE.read(buffer);
        assertEquals(InetAddress.getByAddress(new byte[] {127, 0, 0, 1}), address.result());
    }
}
