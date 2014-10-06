package com.mattunderscore.tcproxy.io.impl;

import static java.util.EnumSet.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.channels.SelectionKey;
import java.util.Set;

import com.mattunderscore.tcproxy.io.IOSelectionKey;
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
