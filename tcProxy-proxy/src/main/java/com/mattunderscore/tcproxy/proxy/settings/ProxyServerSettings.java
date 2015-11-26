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

package com.mattunderscore.tcproxy.proxy.settings;

import com.mattunderscore.tcproxy.selector.NoBackoff;
import com.mattunderscore.tcproxy.selector.SelectorBackoff;
import com.mattunderscore.tcproxy.selector.server.AcceptSettings;
import com.mattunderscore.tcproxy.selector.server.SocketSettings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * @author Matt Champion on 23/11/2015
 */
@Value
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public final class ProxyServerSettings {
    AcceptSettings acceptSettings;
    ConnectionSettings connectionSettings;
    SocketSettings inboundSocketSettings;
    OutboundSocketSettings outboundSocketSettings;
    ReadSelectorSettings readSelectorSettings;
    SelectorBackoff backoff;
    int selectorThreads;

    public static ProxyServerSettingsBuilder builder() {
        return new ProxyServerSettingsBuilder();
    }

    public static class ProxyServerSettingsBuilder {
        private AcceptSettings acceptSettings;
        private ConnectionSettings connectionSettings;
        private SocketSettings inboundSocketSettings;
        private OutboundSocketSettings outboundSocketSettings;
        private ReadSelectorSettings readSelectorSettings;
        private SelectorBackoff backoff = new NoBackoff();
        int selectorThreads = 1;

        ProxyServerSettingsBuilder() {
        }

        public ProxyServerSettings.ProxyServerSettingsBuilder acceptSettings(AcceptSettings acceptSettings) {
            this.acceptSettings = acceptSettings;
            return this;
        }

        public ProxyServerSettings.ProxyServerSettingsBuilder connectionSettings(ConnectionSettings connectionSettings) {
            this.connectionSettings = connectionSettings;
            return this;
        }

        public ProxyServerSettings.ProxyServerSettingsBuilder inboundSocketSettings(SocketSettings inboundSocketSettings) {
            this.inboundSocketSettings = inboundSocketSettings;
            return this;
        }

        public ProxyServerSettings.ProxyServerSettingsBuilder outboundSocketSettings(OutboundSocketSettings outboundSocketSettings) {
            this.outboundSocketSettings = outboundSocketSettings;
            return this;
        }

        public ProxyServerSettings.ProxyServerSettingsBuilder readSelectorSettings(ReadSelectorSettings readSelectorSettings) {
            this.readSelectorSettings = readSelectorSettings;
            return this;
        }

        public ProxyServerSettings.ProxyServerSettingsBuilder backoff(SelectorBackoff backoff) {
            this.backoff = backoff;
            return this;
        }

        public ProxyServerSettings.ProxyServerSettingsBuilder selectorThreads(int selectorThreads) {
            this.selectorThreads = selectorThreads;
            return this;
        }

        public ProxyServerSettings build() {
            return new ProxyServerSettings(this.acceptSettings, this.connectionSettings, this.inboundSocketSettings, this.outboundSocketSettings, this.readSelectorSettings, this.backoff, this.selectorThreads);
        }

        public String toString() {
            return "ProxyServerSettings.ProxyServerSettingsBuilder(acceptSettings=" + this.acceptSettings + ", connectionSettings=" + this.connectionSettings + ", inboundSocketSettings=" + this.inboundSocketSettings + ", outboundSocketSettings=" + this.outboundSocketSettings + ", readSelectorSettings=" + this.readSelectorSettings + ", backoff=" + this.backoff + ", selectorThreads=" + selectorThreads + ")";
        }
    }
}
