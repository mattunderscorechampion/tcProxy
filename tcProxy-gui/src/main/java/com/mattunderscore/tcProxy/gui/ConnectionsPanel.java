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
import com.mattunderscore.tcproxy.proxy.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.awt.VerticalBagLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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
                    revalidate();
                    doLayout();
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
                    revalidate();
                }
            }
        });
    }

    private final class ConnectionPanel extends JPanel {
        private Direction.Listener listener;
        private Connection connection;
        private JLabel clientRead;
        private JLabel clientWritten;
        private JLabel serverRead;
        private JLabel serverWritten;
        private JButton close;
        private JButton remove;
        private AtomicBoolean isClosed = new AtomicBoolean(false);

        private ConnectionPanel(final Connection connection) throws IOException {
            this.connection = connection;
            clientRead = new JLabel("0 read");
            clientWritten = new JLabel("0 written");
            serverRead = new JLabel("0 read");
            serverWritten = new JLabel("0 written");

            listener = new Direction.Listener() {
                @Override
                public void dataRead(Direction direction, int bytesRead) {
                    updateRead();
                }

                @Override
                public void dataWritten(Direction direction, int bytesWritten) {
                    updateWritten();
                }

                @Override
                public void closed(Direction direction) {
                }
            };

            final SocketAddress clientAddress = connection.clientToServer().getFrom().getRemoteAddress();
            final SocketAddress targetAddress = connection.clientToServer().getTo().getRemoteAddress();
            connection.clientToServer().addListener(listener);
            connection.serverToClient().addListener(listener);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    add(new JLabel(clientAddress.toString()));
                    add(clientRead);
                    add(clientWritten);

                    add(new JLabel(targetAddress.toString()));
                    add(serverRead);
                    add(serverWritten);

                    close = new JButton("Close");
                    close.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                connection.close();
                            } catch (IOException e1) {
                                LOG.info("Unable to close connection");
                            }
                        }
                    });
                    add(close);
                    ConnectionsPanel.this.revalidate();
                }
            });
        }

        public void setClosed() {
            if (isClosed.compareAndSet(false, true)) {
                remove = new JButton("Remove");
                remove.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                final Container container = ConnectionsPanel.this;
                                container.remove(ConnectionPanel.this);
                                container.revalidate();
                                container.doLayout();
                                container.repaint();
                            }
                        });
                    }
                });

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        remove(close);
                        add(new JLabel("Closed"));
                        add(remove);
                        ConnectionsPanel.this.revalidate();
                    }
                });
            }
        }

        public void updateWritten() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    clientWritten.setText("" + connection.serverToClient().written() + " written");
                    serverWritten.setText("" + connection.clientToServer().written() + " written");
                    revalidate();
                }
            });
        }

        public void updateRead() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    clientRead.setText("" + connection.clientToServer().read() + " read");
                    serverRead.setText("" + connection.serverToClient().read() + " read");
                    revalidate();
                }
            });
        }
    }
}
