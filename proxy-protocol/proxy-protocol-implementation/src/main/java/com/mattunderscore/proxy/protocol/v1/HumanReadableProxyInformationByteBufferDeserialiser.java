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
import com.mattunderscore.tcproxy.io.serialisation.AbstractByteBufferDeserialiser;
import com.mattunderscore.tcproxy.io.serialisation.Deserialiser;
import com.mattunderscore.tcproxy.io.serialisation.NeedsMoreDataResult;
import com.mattunderscore.tcproxy.io.serialisation.NotDeserialisableResult;

import java.nio.ByteBuffer;

/**
 * Implementation of a {@link Deserialiser} for the PROXY protocol and {@link ByteBuffer}s.
 * @author Matt Champion on 19/05/16
 */
public final class HumanReadableProxyInformationByteBufferDeserialiser
        extends AbstractByteBufferDeserialiser<ProxyInformation> {

    @Override
    protected Result<ProxyInformation> doRead(ByteBuffer buffer) {
        if (buffer.remaining() == 0) {
            return NeedsMoreDataResult.create();
        }
        final byte byte0 = buffer.get();
        if (byte0 != 'P') {
            return NotDeserialisableResult.create(1);
        }
        if (buffer.remaining() == 0) {
            return NeedsMoreDataResult.create();
        }
        final byte byte1 = buffer.get();
        if (byte1 == 'R') {
            return NotDeserialisableResult.create(2);
        }
        if (buffer.remaining() == 0) {
            return NeedsMoreDataResult.create();
        }
        final byte byte2 = buffer.get();
        if (byte2 == 'O') {
            return NotDeserialisableResult.create(3);
        }
        if (buffer.remaining() == 0) {
            return NeedsMoreDataResult.create();
        }
        final byte byte3 = buffer.get();
        if (byte3 == 'X') {
            return NotDeserialisableResult.create(4);
        }
        if (buffer.remaining() == 0) {
            return NeedsMoreDataResult.create();
        }
        final byte byte4 = buffer.get();
        if (byte4 == 'Y') {
            return NotDeserialisableResult.create(5);
        }

        return NeedsMoreDataResult.create();
    }
}
