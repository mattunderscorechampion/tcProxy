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

package com.mattunderscore.tcproxy.selector.server;

import static java.util.Objects.requireNonNull;

import com.mattunderscore.tcproxy.io.configuration.IOSocketConfiguration;
import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;

/**
 * An abstract builder for servers.
 * @author Matt Champion on 13/04/2016
 */
public abstract class AbstractServerBuilder<B extends ServerBuilder<B>> implements ServerBuilder<B> {
    protected final AcceptSettings acceptSettings;
    protected final IOSocketConfiguration<IOSocketChannel> socketSettings;

    protected AbstractServerBuilder(AcceptSettings acceptSettings, IOSocketConfiguration<IOSocketChannel> socketSettings) {
        this.acceptSettings = acceptSettings;
        this.socketSettings = socketSettings;
    }

    @Override
    public final B acceptSettings(AcceptSettings acceptSettings) {
        requireNonNull(acceptSettings, "Accept settings cannot be null");

        return newServerBuilder(acceptSettings, socketSettings);
    }

    @Override
    public final B socketSettings(IOSocketConfiguration<IOSocketChannel> socketSettings) {
        requireNonNull(socketSettings, "Socket settings cannot be null");

        return newServerBuilder(acceptSettings, socketSettings);
    }

    @Override
    public abstract Server build();

    protected abstract B newServerBuilder(AcceptSettings acceptSettings, IOSocketConfiguration<IOSocketChannel> socketSettings);
}
