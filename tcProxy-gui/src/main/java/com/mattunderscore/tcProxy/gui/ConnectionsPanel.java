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
import com.mattunderscore.tcproxy.proxy.action.ActionProcessorFactory;
import com.mattunderscore.tcproxy.proxy.action.WriteDroppingActionProcessor;
import com.mattunderscore.tcproxy.proxy.action.WriteDroppingActionProcessorFactory;
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
    private static final ActionProcessorFactory dropActionProcessorFactory = new WriteDroppingActionProcessorFactory();
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
                    panel.setComplete();
                    revalidate();
                }
            }
        });
    }

    private final class ConnectionPanel extends JPanel {
        private JButton close;
        private JButton remove;
        private AtomicBoolean isClosed = new AtomicBoolean(false);

        private ConnectionPanel(final Connection connection) throws IOException {
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
            add(new EndpointPanel("Client:", connection.clientToServer(), connection.serverToClient()));
            add(new EndpointPanel("Server:", connection.serverToClient(), connection.clientToServer()));

            close = new JButton("Close");
            close.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        setClosed();
                        connection.close();
                    } catch (IOException e1) {
                        LOG.info("Unable to close connection");
                    }
                }
            });
            add(close);
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

        public void setComplete() {
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
                        add(new JLabel("Complete"));
                        add(remove);
                        ConnectionsPanel.this.revalidate();
                    }
                });
            }
        }
    }

    private final class EndpointPanel extends JPanel {
        private JLabel read;
        private JLabel written;

        private EndpointPanel(final String title, final Direction source, final Direction destination) throws IOException {
            setBorder(BorderFactory.createLineBorder(Color.black));
            add(new JLabel(title));
            read = new JLabel("0 read from");
            written = new JLabel("0 written to");

            final SocketAddress address = source.getFrom().getRemoteAddress();

            add(new JLabel(address.toString()));
            add(read);
            add(written);
            final JButton dropButton = new JButton("Drop writes");
            final JButton restoreButton = new JButton("Restore writes");
            restoreButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    destination.unchainProcessor();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            remove(restoreButton);
                            add(dropButton);
                            revalidate();
                        }
                    });
                }
            });
            dropButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    destination.chainProcessor(dropActionProcessorFactory);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            remove(dropButton);
                            add(restoreButton);
                            revalidate();
                        }
                    });
                }
            });
            add(dropButton);

            final Direction.Listener readListener = new Direction.Listener() {
                @Override
                public void dataRead(Direction direction, int bytesRead) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            read.setText("" + source.read() + " read from");
                            revalidate();
                        }
                    });
                }

                @Override
                public void dataWritten(Direction direction, int bytesWritten) {
                }

                @Override
                public void closed(Direction direction) {
                }
            };
            source.addListener(readListener);
            final Direction.Listener writeListener = new Direction.Listener() {
                @Override
                public void dataRead(Direction direction, int bytesRead) {
                }

                @Override
                public void dataWritten(Direction direction, int bytesWritten) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            written.setText("" + destination.written() + " written to");
                            revalidate();
                        }
                    });
                }

                @Override
                public void closed(Direction direction) {
                }
            };
            destination.addListener(writeListener);
        }
    }
}
