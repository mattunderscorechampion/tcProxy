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
import com.mattunderscore.tcproxy.io.serialisation.AbstractByteBufferSerialiser;
import com.mattunderscore.tcproxy.io.serialisation.Serialiser;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Implementation of a {@link Serialiser} for the PROXY protocol and {@link ByteBuffer}s.
 * @author Matt Champion on 17/05/16
 */
public final class HumanReadableProxyInformationSerialiser extends AbstractByteBufferSerialiser<ProxyInformation> {
    public static final Serialiser<ProxyInformation, ByteBuffer> INSTANCE = new HumanReadableProxyInformationSerialiser();

    private static final byte SPACE = (byte) 0x20;
    private static final byte[] PROXY_HEADER = new byte[] { 0x50, 0x52, 0x4F, 0x58, 0x59, SPACE };
    private static final byte[] IPV4_HEADER = new byte[] { 0x54, 0x43, 0x50, 0x34, SPACE };
    private static final byte[] IPV6_HEADER = new byte[] { 0x54, 0x43, 0x50, 0x36, SPACE };
    private static final byte[] UNKNOWN_HEADER = new byte[] { 0x55, 0x4E, 0x4B, 0x4E, 0x4F, 0x57, 0x4E, 0x0D, 0x0A };
    private static final byte[] CRLF = new byte[] { 0x0D, 0x0A };
    private static final Charset ASCII = Charset.forName("ASCII");

    private HumanReadableProxyInformationSerialiser() {
    }

    @Override
    protected void doWrite(ProxyInformation protocol, ByteBuffer buffer) {
        buffer.put(PROXY_HEADER);
        final InternetAddressFamily addressFamily = protocol.getAddressFamily();
        if (addressFamily == InternetAddressFamily.IPV4) {
            buffer.put(IPV4_HEADER);
        }
        else if (addressFamily == InternetAddressFamily.IPV6) {
            buffer.put(IPV6_HEADER);
        }
        else {
            buffer.put(UNKNOWN_HEADER);
            return;
        }

        buffer.put(protocol.getSourceAddress().getHostAddress().getBytes(ASCII));
        buffer.put(SPACE);
        buffer.put(protocol.getDestinationAddress().getHostAddress().getBytes(Charset.forName("ASCII")));
        buffer.put(SPACE);
        buffer.put(Integer.toString(protocol.getSourcePort(), 10).getBytes(Charset.forName("ASCII")));
        buffer.put(SPACE);
        buffer.put(Integer.toString(protocol.getDestinationPort(), 10).getBytes(Charset.forName("ASCII")));
        buffer.put(CRLF);
    }

    @Override
    protected int calculateMaximumRequiredCapacity(ProxyInformation protocol) {
        final InternetAddressFamily addressFamily = protocol.getAddressFamily();
        if (addressFamily == InternetAddressFamily.IPV4) {
            return 56;
        }
        else if (addressFamily == InternetAddressFamily.IPV6) {
            return 104;
        }
        else {
            return 15;
        }
    }
}
