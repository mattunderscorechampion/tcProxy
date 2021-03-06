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

package com.mattunderscore.tcproxy.selector.general;

import static com.mattunderscore.tcproxy.io.selection.IOSelectionKey.Op.READ;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.nio.channels.ClosedChannelException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.mattunderscore.tcproxy.io.selection.IOSelectionKey;
import com.mattunderscore.tcproxy.io.selection.IOSelector;
import com.mattunderscore.tcproxy.io.socket.IOSocketChannel;
import com.mattunderscore.tcproxy.selector.NoBackoff;
import com.mattunderscore.tcproxy.selector.SelectionRunnable;

/**
 * Unit tests for {@link GeneralPurposeSelector}.
 * @author Matt Champion on 24/10/2015
 */
public final class GeneralPurposeSelectorTest {
    @Mock
    private IOSelector ioSelector;
    @Mock
    private IOSocketChannel channel;
    @Mock
    private IOSelectionKey key;
    @Mock
    private SelectionRunnable<IOSocketChannel> runnable;

    @Before
    public void setUp() {
        initMocks(this);
        when(key.isValid()).thenReturn(true);
        when(key.isReadable()).thenReturn(true);
        when(key.interestedOperations()).thenReturn(EnumSet.allOf(IOSelectionKey.Op.class));
        when(key.socket()).thenReturn(channel);
    }

    @Test
    public void startAndStop() throws ClosedChannelException {
        when(channel.register(eq(ioSelector), eq(Collections.singleton(READ)), any())).thenAnswer(new Answer<IOSelectionKey>() {
            @Override
            public IOSelectionKey answer(InvocationOnMock invocationOnMock) throws Throwable {
                // Return the attachment registered
                when(key.attachment()).thenReturn(invocationOnMock.getArguments()[2]);
                return key;
            }
        });
        final Set<IOSelectionKey> selectionKeySet = new HashSet<>();
        selectionKeySet.add(key);
        when(ioSelector.selectedKeys()).thenReturn(selectionKeySet);

        final GeneralPurposeSelector selector = new GeneralPurposeSelector(ioSelector, NoBackoff.get());
        selector.register(channel, READ, runnable);

        selector.run();

        verify(runnable).run(eq(channel), isA(RegistrationHandle.class));
    }
}
