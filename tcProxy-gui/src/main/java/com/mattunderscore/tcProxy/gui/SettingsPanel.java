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

import static java.lang.Integer.parseInt;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mattunderscore.tcproxy.proxy.settings.AcceptorSettings;
import com.mattunderscore.tcproxy.proxy.settings.ConnectionSettings;
import com.mattunderscore.tcproxy.proxy.settings.OutboundSocketSettings;
import com.mattunderscore.tcproxy.proxy.settings.ReadSelectorSettings;
import com.mattunderscore.tcproxy.selector.server.SocketSettings;

/**
 * @author matt on 15/03/14.
 */
public final class SettingsPanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger("gui");
    private final JTextField listeningPort;
    private final JTextField inboundReceiveBufferSize;
    private final JTextField inboundSendBufferSize;
    private final JTextField outboundReceiveBufferSize;
    private final JTextField outboundSendBufferSize;
    private final JTextField targetHost;
    private final JTextField targetPort;
    private final JTextField readBufferSize;
    private final JTextField writeQueueBound;
    private final JTextField batchSize;

    public SettingsPanel(final Runnable callback) {
        listeningPort = new JTextField("8085", 8);
        inboundReceiveBufferSize = new JTextField("1024", 8);
        inboundSendBufferSize = new JTextField("1024", 8);
        outboundReceiveBufferSize = new JTextField("1024", 8);
        outboundSendBufferSize = new JTextField("1024", 8);
        targetHost = new JTextField("localhost", 8);
        targetPort = new JTextField("8080", 8);
        readBufferSize = new JTextField("1024", 8);
        writeQueueBound = new JTextField("5000", 8);
        batchSize = new JTextField("2048", 8);

        setLayout(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.gridx = 0;
        c.gridy = 0;
        add(new JLabel("Listening port"), c);
        c.gridx = 0;
        c.gridy = 1;
        add(new JLabel("Inbound receive buffer size"), c);
        c.gridx = 0;
        c.gridy = 2;
        add(new JLabel("Inbound send buffer size"), c);
        c.gridx = 0;
        c.gridy = 3;
        add(new JLabel("Outbound receive buffer size"), c);
        c.gridx = 0;
        c.gridy = 4;
        add(new JLabel("Outbound send buffer size"), c);
        c.gridx = 0;
        c.gridy = 5;
        add(new JLabel("Target host"), c);
        c.gridx = 0;
        c.gridy = 6;
        add(new JLabel("Target port"), c);
        c.gridx = 0;
        c.gridy = 7;
        add(new JLabel("Read buffer size"), c);
        c.gridx = 0;
        c.gridy = 8;
        add(new JLabel("Per connection action queue max size"), c);

        c.gridx = 1;
        c.gridy = 0;
        add(listeningPort, c);
        c.gridx = 1;
        c.gridy = 1;
        add(inboundReceiveBufferSize, c);
        c.gridx = 1;
        c.gridy = 2;
        add(inboundSendBufferSize, c);
        c.gridx = 1;
        c.gridy = 3;
        add(outboundReceiveBufferSize, c);
        c.gridx = 1;
        c.gridy = 4;
        add(outboundSendBufferSize, c);
        c.gridx = 1;
        c.gridy = 5;
        add(targetHost, c);
        c.gridx = 1;
        c.gridy = 6;
        add(targetPort, c);
        c.gridx = 1;
        c.gridy = 7;
        add(readBufferSize, c);
        c.gridx = 1;
        c.gridy = 8;
        add(writeQueueBound, c);

        c.gridx = 1;
        c.gridy = 9;
        final JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LOG.info("Creating settings");
                callback.run();
            }
        });
        add(ok, c);
    }

    public AcceptorSettings getAcceptorSettings() {
        return new AcceptorSettings(parseInt(listeningPort.getText()));
    }

    public ConnectionSettings getConnectionSettings() {
        return new ConnectionSettings(parseInt(writeQueueBound.getText()), parseInt(batchSize.getText()));
    }

    public SocketSettings getInboundSocketSettings() {
        return SocketSettings
            .builder()
            .receiveBuffer(parseInt(inboundReceiveBufferSize.getText()))
            .sendBuffer(parseInt(inboundSendBufferSize.getText()))
            .build();
    }

    public OutboundSocketSettings getOutboundSocketSettings() {
        return OutboundSocketSettings
            .builder()
            .port(parseInt(targetPort.getText()))
            .host(targetHost.getText()).receiveBuffer(parseInt(outboundReceiveBufferSize.getText()))
            .sendBuffer(parseInt(outboundSendBufferSize.getText()))
            .build();
    }

    public ReadSelectorSettings getReadSelectorSettings() {
        return new ReadSelectorSettings(parseInt(readBufferSize.getText()));
    }
}
