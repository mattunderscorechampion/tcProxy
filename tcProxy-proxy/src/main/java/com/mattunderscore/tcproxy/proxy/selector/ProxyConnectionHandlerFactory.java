/* Copyright © 2015 Matthew Champion
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

package com.mattunderscore.tcproxy.proxy.selector;

import com.mattunderscore.tcproxy.io.IOSelectionKey;
import com.mattunderscore.tcproxy.proxy.OutboundConnectionFactory;
import com.mattunderscore.tcproxy.proxy.connection.ConnectionManager;
import com.mattunderscore.tcproxy.proxy.direction.DirectionAndConnection;
import com.mattunderscore.tcproxy.proxy.settings.ConnectionSettings;
import com.mattunderscore.tcproxy.proxy.settings.OutboundSocketSettings;
import com.mattunderscore.tcproxy.selector.SocketChannelSelector;
import com.mattunderscore.tcproxy.selector.connecting.ConnectionHandler;
import com.mattunderscore.tcproxy.selector.connecting.ConnectionHandlerFactory;

/**
 * Implementation of {@link ConnectionHandlerFactory} for the proxy {@link Server}.
 * @author Matt Champion on 18/11/2015
 */
public final class ProxyConnectionHandlerFactory implements ConnectionHandlerFactory {
    private final OutboundSocketSettings outboundSocketSettings;
    private final ConnectionSettings settings;
    private final ConnectionManager manager;

    public ProxyConnectionHandlerFactory(
            OutboundSocketSettings outboundSocketSettings,
            ConnectionSettings settings,
            ConnectionManager manager) {
        this.outboundSocketSettings = outboundSocketSettings;
        this.settings = settings;
        this.manager = manager;
    }

    @Override
    public ConnectionHandler create(final SocketChannelSelector selector) {
        return new ProxyConnectionHandler(new OutboundConnectionFactory(outboundSocketSettings), settings, manager, new Writer() {
            @Override
            public void registerNewWork(DirectionAndConnection dc) {
                selector.register(dc.getDirection().getTo(), IOSelectionKey.Op.WRITE, new WriteSelectionRunnable(dc));
            }
        });
    }
}
