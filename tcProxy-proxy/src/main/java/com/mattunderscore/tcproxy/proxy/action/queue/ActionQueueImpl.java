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

package com.mattunderscore.tcproxy.proxy.action.queue;

import com.mattunderscore.tcproxy.proxy.action.Action;
import com.mattunderscore.tcproxy.proxy.Connection;
import com.mattunderscore.tcproxy.proxy.action.BatchedWrite;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Implementation of {@link ActionQueue}.
 * @author Matt Champion on 19/02/14.
 */
public class ActionQueueImpl implements ActionQueue {
    private final Connection connection;
    private final int batchSize;
    private final BlockingQueue<Action> actions;
    private volatile Action current = null;

    public ActionQueueImpl(final Connection connection, final int queueSize, final int batchSize) {
        this.connection = connection;
        this.batchSize = batchSize;
        this.actions = new ArrayBlockingQueue<>(queueSize);
    }

    public boolean queueFull() {
        return actions.remainingCapacity() == 0;
    }

    public int opsPending() {
        return actions.size();
    }

    @Override
    public void add(final Action action) {
        actions.add(action);
    }

    @Override
    public Action current() {
        final Action currentAction = current;
        if (currentAction != null && !currentAction.writeComplete()) {
            return currentAction;
        }
        else {
            final Action batchedAction = batchActions();
            current = batchedAction;
            return batchedAction;
        }
    }

    private Action batchActions() {
        final BatchedWrite batchedWrite = new BatchedWrite(batchSize);
        boolean batchedData = false;
        while (true) {
            final Action nextAction = actions.peek();
            if (nextAction != null && nextAction.isBatchable()) {
                if (batchedWrite.batch(nextAction)) {
                    batchedData = true;
                    actions.poll();
                }
                else {
                    return batchedWrite;
                }
            }
            else {
                if (batchedData) {
                    return batchedWrite;
                }
                else {
                    return actions.poll();
                }
            }
        }
    }

    @Override
    public boolean hasData() {
        return current() != null;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

}
