package com.mattunderscore.tcproxy.io.impl;

import static java.util.EnumSet.of;
import static org.junit.Assert.assertTrue;

import java.nio.channels.SelectionKey;

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
}
