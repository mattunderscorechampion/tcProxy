package com.mattunderscore.proxy.protocol.v1;

import com.mattunderscore.tcproxy.io.serialisation.Deserialiser.Result;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link PlainTextIPV4Deserialiser}.
 * @author Matt Champion on 21/05/16
 */
public final class PlainTextIPV4DeserialiserTest {

    @Test
    public void local() throws UnknownHostException {
        final ByteBuffer buffer = ByteBuffer.wrap("127.0.0.1".getBytes());
        final Result<InetAddress> address = PlainTextIPV4Deserialiser.INSTANCE.read(buffer);
        assertEquals(InetAddress.getByAddress(new byte[] {127, 0, 0, 1}), address.result());
        assertFalse(buffer.hasRemaining());
    }

    @Test
    public void tooShort() throws UnknownHostException {
        final ByteBuffer buffer = ByteBuffer.wrap("127.0.0".getBytes());
        final Result<InetAddress> address = PlainTextIPV4Deserialiser.INSTANCE.read(buffer);
        assertFalse(address.hasResult());
        assertTrue(address.hasMoreData());
        assertThat(address.bytesProcessed(), greaterThan(1));
        assertTrue(buffer.hasRemaining());
    }
}
