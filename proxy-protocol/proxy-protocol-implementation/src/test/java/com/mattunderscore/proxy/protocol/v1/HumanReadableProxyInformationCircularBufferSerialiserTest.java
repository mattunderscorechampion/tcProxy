package com.mattunderscore.proxy.protocol.v1;

import com.mattunderscore.proxy.protocol.InternetAddressFamily;
import com.mattunderscore.proxy.protocol.ProxyInformation;
import com.mattunderscore.tcproxy.io.data.CircularBuffer;
import com.mattunderscore.tcproxy.io.impl.CircularBufferImpl;
import com.mattunderscore.tcproxy.io.serialisation.Serialiser;
import com.mattunderscore.tcproxy.io.serialisation.Serialiser.HasCapacity;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link HumanReadableProxyInformationCircularBufferSerialiser}.
 * @author Matt Champion on 18/05/16
 */
public final class HumanReadableProxyInformationCircularBufferSerialiserTest {

    @Test
    public void testIPV4() throws UnknownHostException {
        final Serialiser<ProxyInformation, CircularBuffer> serialiser = HumanReadableProxyInformationCircularBufferSerialiser.INSTANCE;
        final ProxyInformation proxyInformation = ProxyInformation
                .builder()
                .addressFamily(InternetAddressFamily.IPV4)
                .sourceAddress(InetAddress.getByAddress(new byte[]{ -1, -1, -1, -1 }))
                .destinationAddress(InetAddress.getByAddress(new byte[]{ -1, -1, -1, -1 }))
                .sourcePort(65535)
                .destinationPort(65535)
                .build();

        final byte[] bytes = new byte[56];
        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        final CircularBuffer buffer = CircularBufferImpl.allocate(56);
        assertEquals(HasCapacity.HAS_CAPACITY, serialiser.hasCapacity(proxyInformation, buffer));

        serialiser.write(proxyInformation, buffer);
        buffer.get(byteBuffer);

        assertEquals(
                "PROXY TCP4 255.255.255.255 255.255.255.255 65535 65535\r\n",
                new String(bytes, Charset.forName("ASCII")));
    }

    @Test
    public void testIPV6() throws UnknownHostException {
        final Serialiser<ProxyInformation, CircularBuffer> serialiser = HumanReadableProxyInformationCircularBufferSerialiser.INSTANCE;
        final ProxyInformation proxyInformation = ProxyInformation
                .builder()
                .addressFamily(InternetAddressFamily.IPV6)
                .sourceAddress(InetAddress.getByAddress(new byte[]{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }))
                .destinationAddress(InetAddress.getByAddress(new byte[]{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }))
                .sourcePort(65535)
                .destinationPort(65535)
                .build();

        final byte[] bytes = new byte[104];
        final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        final CircularBuffer buffer = CircularBufferImpl.allocate(104);
        assertEquals(HasCapacity.HAS_CAPACITY, serialiser.hasCapacity(proxyInformation, buffer));

        serialiser.write(proxyInformation, buffer);
        buffer.get(byteBuffer);

        assertEquals(
                "PROXY TCP6 ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff 65535 65535\r\n",
                new String(bytes, Charset.forName("ASCII")));
    }

    @Test
    public void testLacksTotalCapacity() throws UnknownHostException {
        final Serialiser<ProxyInformation, CircularBuffer> serialiser = HumanReadableProxyInformationCircularBufferSerialiser.INSTANCE;
        final ProxyInformation proxyInformation = ProxyInformation
                .builder()
                .addressFamily(InternetAddressFamily.IPV4)
                .sourceAddress(InetAddress.getByAddress(new byte[]{ -1, -1, -1, -1 }))
                .destinationAddress(InetAddress.getByAddress(new byte[]{ -1, -1, -1, -1 }))
                .sourcePort(65535)
                .destinationPort(65535)
                .build();

        final CircularBuffer buffer = CircularBufferImpl.allocate(30);
        assertEquals(HasCapacity.LACKS_TOTAL_CAPACITY, serialiser.hasCapacity(proxyInformation, buffer));
    }

    @Test
    public void testLacksFreeCapacity() throws UnknownHostException {
        final Serialiser<ProxyInformation, CircularBuffer> serialiser = HumanReadableProxyInformationCircularBufferSerialiser.INSTANCE;
        final ProxyInformation proxyInformation = ProxyInformation
                .builder()
                .addressFamily(InternetAddressFamily.IPV4)
                .sourceAddress(InetAddress.getByAddress(new byte[]{ -1, -1, -1, -1 }))
                .destinationAddress(InetAddress.getByAddress(new byte[]{ -1, -1, -1, -1 }))
                .sourcePort(65535)
                .destinationPort(65535)
                .build();

        final CircularBuffer buffer = CircularBufferImpl.allocate(56);
        buffer.put((byte) 10);
        assertEquals(HasCapacity.LACKS_FREE_CAPACITY, serialiser.hasCapacity(proxyInformation, buffer));
    }
}
