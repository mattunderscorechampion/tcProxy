/* Copyright © 2014 Matthew Champion
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

package com.mattunderscore.tcProxy.gui;

import com.mattunderscore.tcproxy.proxy.Connection;
import com.mattunderscore.tcproxy.proxy.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.awt.VerticalBagLayout;

import javax.swing.*;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author matt on 15/03/14.
 */
public final class ConnectionsPanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger("gui");
    final Map<Connection, ConnectionPanel> connections = new HashMap<>();
    public ConnectionsPanel(final ConnectionManager manager) {
        setLayout(new VerticalBagLayout());
        manager.addListener(new ConnectionManager.Listener() {
            @Override
            public void newConnection(final Connection connection) {
                LOG.info("Adding connection {}", connection);
                try {
                    final ConnectionPanel panel = new ConnectionPanel(connection);
                    add(panel);
                    connections.put(connection, panel);
                    validate();
                }
                catch (final IOException e) {
                    LOG.warn("Unable to create panel", e);
                }
            }

            @Override
            public void closedConnection(final Connection connection) {
                LOG.info("Removing connection {}", connection);
                final ConnectionPanel panel = connections.remove(connection);
                if (panel != null) {
                    panel.setClosed();
                    validate();
                }
            }
        });
    }

    private final class ConnectionPanel extends JPanel {
        private Connection connection;
        private JLabel read;
        private JLabel written;

        private ConnectionPanel(Connection connection) throws IOException {
            this.connection = connection;
            read = new JLabel("0 read");
            written = new JLabel("0 written");

            final SocketAddress clientAddress = connection.clientToServer().getFrom().getRemoteAddress();
            final SocketAddress targetAddress = connection.clientToServer().getTo().getRemoteAddress();

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    add(new JLabel(clientAddress.toString()));
                    add(new JLabel(targetAddress.toString()));

                    add(read);
                    add(written);
                }
            });
        }

        public void setClosed() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    add(new JLabel("Closed"));
                }
            });
        }

        public void updateWritten() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    written.setText("" + connection.clientToServer().written() + " written");
                    validate();
                }
            });
        }

        public void updateRead() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    read.setText("" + connection.clientToServer().read() + " read");
                    validate();
                }
            });
        }
    }
}
