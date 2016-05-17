/* Copyright © 2016 Matthew Champion
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
 * Neither the name of mattunderscore.com nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL MATTHEW CHAMPION BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.mattunderscore.proxy.protocol.v1;

import com.mattunderscore.proxy.protocol.InternetAddressFamily;
import com.mattunderscore.proxy.protocol.ProxyInformation;
import com.mattunderscore.tcproxy.io.serialisation.Serialiser;
import com.mattunderscore.tcproxy.io.serialisation.Serialiser.HasCapacity;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link HumanReadableProxyInformationSerialiser}.
 * @author Matt Champion on 17/05/16
 */
public final class HumanReadableProxyInformationSerialiserTest {

    @Test
    public void testIPV4() throws UnknownHostException {
        final Serialiser<ProxyInformation, ByteBuffer> serialiser = HumanReadableProxyInformationSerialiser.INSTANCE;
        final ProxyInformation proxyInformation = ProxyInformation
                .builder()
                .addressFamily(InternetAddressFamily.IPV4)
                .sourceAddress(InetAddress.getByAddress(new byte[]{ -1, -1, -1, -1 }))
                .destinationAddress(InetAddress.getByAddress(new byte[]{ -1, -1, -1, -1 }))
                .sourcePort(65535)
                .destinationPort(65535)
                .build();

        final byte[] bytes = new byte[56];
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        assertEquals(HasCapacity.HAS_CAPACITY, serialiser.hasCapacity(proxyInformation, buffer));

        serialiser.write(proxyInformation, buffer);

        assertEquals(
            "PROXY TCP4 255.255.255.255 255.255.255.255 65535 65535\r\n",
            new String(bytes, Charset.forName("ASCII")));
    }

    @Test
    public void testIPV6() throws UnknownHostException {
        final Serialiser<ProxyInformation, ByteBuffer> serialiser = HumanReadableProxyInformationSerialiser.INSTANCE;
        final ProxyInformation proxyInformation = ProxyInformation
                .builder()
                .addressFamily(InternetAddressFamily.IPV6)
                .sourceAddress(InetAddress.getByAddress(new byte[]{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }))
                .destinationAddress(InetAddress.getByAddress(new byte[]{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 }))
                .sourcePort(65535)
                .destinationPort(65535)
                .build();

        final byte[] bytes = new byte[104];
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        assertEquals(HasCapacity.HAS_CAPACITY, serialiser.hasCapacity(proxyInformation, buffer));

        serialiser.write(proxyInformation, buffer);

        assertEquals(
            "PROXY TCP6 ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff 65535 65535\r\n",
            new String(bytes, Charset.forName("ASCII")));
    }

    @Test
    public void testLacksTotalCapacity() throws UnknownHostException {
        final Serialiser<ProxyInformation, ByteBuffer> serialiser = HumanReadableProxyInformationSerialiser.INSTANCE;
        final ProxyInformation proxyInformation = ProxyInformation
                .builder()
                .addressFamily(InternetAddressFamily.IPV4)
                .sourceAddress(InetAddress.getByAddress(new byte[]{ -1, -1, -1, -1 }))
                .destinationAddress(InetAddress.getByAddress(new byte[]{ -1, -1, -1, -1 }))
                .sourcePort(65535)
                .destinationPort(65535)
                .build();

        final byte[] bytes = new byte[30];
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        assertEquals(HasCapacity.LACKS_TOTAL_CAPACITY, serialiser.hasCapacity(proxyInformation, buffer));
    }

    @Test
    public void testLacksFreeCapacity() throws UnknownHostException {
        final Serialiser<ProxyInformation, ByteBuffer> serialiser = HumanReadableProxyInformationSerialiser.INSTANCE;
        final ProxyInformation proxyInformation = ProxyInformation
                .builder()
                .addressFamily(InternetAddressFamily.IPV4)
                .sourceAddress(InetAddress.getByAddress(new byte[]{ -1, -1, -1, -1 }))
                .destinationAddress(InetAddress.getByAddress(new byte[]{ -1, -1, -1, -1 }))
                .sourcePort(65535)
                .destinationPort(65535)
                .build();

        final byte[] bytes = new byte[56];
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.putInt(10);
        assertEquals(HasCapacity.LACKS_FREE_CAPACITY, serialiser.hasCapacity(proxyInformation, buffer));
    }
}
