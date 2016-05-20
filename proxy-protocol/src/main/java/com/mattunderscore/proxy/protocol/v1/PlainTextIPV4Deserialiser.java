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

import com.mattunderscore.tcproxy.io.serialisation.AbstractByteBufferDeserialiser;
import com.mattunderscore.tcproxy.io.serialisation.AbstractByteBufferSerialiser;
import com.mattunderscore.tcproxy.io.serialisation.DeserialisationResult;
import com.mattunderscore.tcproxy.io.serialisation.Deserialiser;
import com.mattunderscore.tcproxy.io.serialisation.NotDeserialisableResult;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deserialiser for {@link Inet4Address} from {@link ByteBuffer}.
 * @author Matt Champion on 20/05/16
 */
public final class PlainTextIPV4Deserialiser extends AbstractByteBufferDeserialiser<InetAddress> {
    public static final Deserialiser<InetAddress, ByteBuffer> INSTANCE = new PlainTextIPV4Deserialiser();
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "([0-9][0-9]?[0-9]?)\\.([0-9][0-9]?[0-9]?)\\.([0-9][0-9]?[0-9]?)\\.([0-9][0-9]?[0-9]?)");

    private PlainTextIPV4Deserialiser() {
    }

    @Override
    protected Result<InetAddress> doRead(ByteBuffer buffer) {
        final byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        final String address = new String(bytes);

        final Matcher matcher = IPV4_PATTERN.matcher(address);
        if (matcher.matches()) {
            final int firstByte = Integer.parseInt(matcher.group(1), 10);
            final int secondByte = Integer.parseInt(matcher.group(2), 10);
            final int thirdByte = Integer.parseInt(matcher.group(3), 10);
            final int forthByte = Integer.parseInt(matcher.group(4), 10);

            if (firstByte > 255 || secondByte > 255 || thirdByte > 255 || forthByte > 255) {
                return NotDeserialisableResult.create(7);
            }

            try {
                return DeserialisationResult.create(
                        Inet4Address.getByAddress(new byte[] { (byte) firstByte, (byte) secondByte, (byte) thirdByte, (byte) forthByte}),
                        address.length(),
                        buffer.hasRemaining());
            } catch (UnknownHostException e) {
                return NotDeserialisableResult.create(address.length());
            }
        }
        else {
            return NotDeserialisableResult.create(2);
        }
    }
}
