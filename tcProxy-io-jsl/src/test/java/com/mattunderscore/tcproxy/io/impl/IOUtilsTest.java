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

package com.mattunderscore.tcproxy.io.impl;

import static java.util.EnumSet.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.channels.SelectionKey;
import java.util.Set;

import org.junit.Test;

import com.mattunderscore.tcproxy.io.IOSelectionKey.Op;

public final class IOUtilsTest {

    @Test
    public void convertToBitSet0() {
        final int bitSet = IOUtils.convertToBitSet(of(Op.ACCEPT));
        assertTrue((SelectionKey.OP_ACCEPT & bitSet) > 0);
        assertTrue((SelectionKey.OP_CONNECT & bitSet) == 0);
        assertTrue((SelectionKey.OP_READ & bitSet) == 0);
        assertTrue((SelectionKey.OP_WRITE & bitSet) == 0);
    }

    @Test
    public void convertToBitSet1() {
        final int bitSet = IOUtils.convertToBitSet(of(Op.CONNECT));
        assertTrue((SelectionKey.OP_CONNECT & bitSet) > 0);
        assertTrue((SelectionKey.OP_ACCEPT & bitSet) == 0);
        assertTrue((SelectionKey.OP_READ & bitSet) == 0);
        assertTrue((SelectionKey.OP_WRITE & bitSet) == 0);
    }

    @Test
    public void convertToBitSet2() {
        final int bitSet = IOUtils.convertToBitSet(of(Op.READ));
        assertTrue((SelectionKey.OP_READ & bitSet) > 0);
        assertTrue((SelectionKey.OP_ACCEPT & bitSet) == 0);
        assertTrue((SelectionKey.OP_CONNECT & bitSet) == 0);
        assertTrue((SelectionKey.OP_WRITE & bitSet) == 0);
    }

    @Test
    public void convertToBitSet3() {
        final int bitSet = IOUtils.convertToBitSet(of(Op.WRITE));
        assertTrue((SelectionKey.OP_WRITE & bitSet) > 0);
        assertTrue((SelectionKey.OP_ACCEPT & bitSet) == 0);
        assertTrue((SelectionKey.OP_CONNECT & bitSet) == 0);
        assertTrue((SelectionKey.OP_READ & bitSet) == 0);
    }

    @Test
    public void convertToBitSet4() {
        final int bitSet = IOUtils.convertToBitSet(of(Op.CONNECT, Op.ACCEPT));
        assertTrue((SelectionKey.OP_CONNECT & bitSet) > 0);
        assertTrue((SelectionKey.OP_ACCEPT & bitSet) > 0);
        assertTrue((SelectionKey.OP_READ & bitSet) == 0);
        assertTrue((SelectionKey.OP_WRITE & bitSet) == 0);
    }

    @Test
    public void convertToBitSet5() {
        final int bitSet = IOUtils.convertToBitSet(of(Op.CONNECT, Op.ACCEPT, Op.READ));
        assertTrue((SelectionKey.OP_CONNECT & bitSet) > 0);
        assertTrue((SelectionKey.OP_ACCEPT & bitSet) > 0);
        assertTrue((SelectionKey.OP_READ & bitSet) > 0);
        assertTrue((SelectionKey.OP_WRITE & bitSet) == 0);
    }

    @Test
    public void convertToBitSet6() {
        final int bitSet = IOUtils.convertToBitSet(of(Op.CONNECT, Op.ACCEPT, Op.READ, Op.WRITE));
        assertTrue((SelectionKey.OP_CONNECT & bitSet) > 0);
        assertTrue((SelectionKey.OP_ACCEPT & bitSet) > 0);
        assertTrue((SelectionKey.OP_READ & bitSet) > 0);
        assertTrue((SelectionKey.OP_WRITE & bitSet) > 0);
    }

    @Test
    public void convertToOpSet0() {
        final Set<Op> ops = IOUtils.mapToIntFromOps(SelectionKey.OP_ACCEPT);
        assertTrue(ops.contains(Op.ACCEPT));
        assertEquals(1, ops.size());
    }

    @Test
    public void convertToOpSet1() {
        final Set<Op> ops = IOUtils.mapToIntFromOps(SelectionKey.OP_CONNECT);
        assertTrue(ops.contains(Op.CONNECT));
        assertEquals(1, ops.size());
    }

    @Test
    public void convertToOpSet2() {
        final Set<Op> ops = IOUtils.mapToIntFromOps(SelectionKey.OP_READ);
        assertTrue(ops.contains(Op.READ));
        assertEquals(1, ops.size());
    }

    @Test
    public void convertToOpSet3() {
        final Set<Op> ops = IOUtils.mapToIntFromOps(SelectionKey.OP_WRITE);
        assertTrue(ops.contains(Op.WRITE));
        assertEquals(1, ops.size());
    }

    @Test
    public void convertToOpSet4() {
        final Set<Op> ops = IOUtils.mapToIntFromOps(SelectionKey.OP_ACCEPT | SelectionKey.OP_CONNECT);
        assertTrue(ops.contains(Op.ACCEPT));
        assertTrue(ops.contains(Op.CONNECT));
        assertEquals(2, ops.size());
    }

    @Test
    public void convertToOpSet5() {
        final Set<Op> ops = IOUtils.mapToIntFromOps(SelectionKey.OP_CONNECT | SelectionKey.OP_WRITE);
        assertTrue(ops.contains(Op.CONNECT));
        assertTrue(ops.contains(Op.WRITE));
        assertEquals(2, ops.size());
    }

    @Test
    public void convertToOpSet6() {
        final Set<Op> ops = IOUtils.mapToIntFromOps(SelectionKey.OP_READ | SelectionKey.OP_ACCEPT);
        assertTrue(ops.contains(Op.READ));
        assertTrue(ops.contains(Op.ACCEPT));
        assertEquals(2, ops.size());
    }

    @Test
    public void convertToOpSet7() {
        final Set<Op> ops = IOUtils.mapToIntFromOps(SelectionKey.OP_ACCEPT | SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        assertTrue(ops.contains(Op.ACCEPT));
        assertTrue(ops.contains(Op.CONNECT));
        assertTrue(ops.contains(Op.READ));
        assertTrue(ops.contains(Op.WRITE));
        assertEquals(4, ops.size());
    }
}
