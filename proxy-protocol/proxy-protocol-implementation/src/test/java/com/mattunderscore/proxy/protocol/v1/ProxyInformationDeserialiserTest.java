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

import com.mattunderscore.proxy.protocol.ProxyInformation;
import com.mattunderscore.proxy.protocol.v1.ProxyInformationDeserialiser;
import com.mattunderscore.tcproxy.io.serialisation.Deserialiser;
import com.mattunderscore.tcproxy.io.serialisation.Deserialiser.Result;
import org.junit.Test;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Matt Champion on 28/05/16
 */
public final class ProxyInformationDeserialiserTest {
    public static final Deserialiser<ProxyInformation, ByteBuffer> INSTANCE = new ProxyInformationDeserialiser();

    @Test
    public void tooShort() throws UnknownHostException {
        final ByteBuffer buffer = ByteBuffer.wrap("P".getBytes());
        final Result<ProxyInformation> address = INSTANCE.read(buffer);
        System.out.println(address);
        assertTrue(address.needsMoreData());
    }

    @Test
    public void badHeader() throws UnknownHostException {
        final ByteBuffer buffer = ByteBuffer.wrap("PXXY".getBytes());
        final Result<ProxyInformation> address = INSTANCE.read(buffer);
        assertTrue(address.notDeserialisable());
        assertFalse(address.needsMoreData());
    }
}
