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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.mattunderscore.tcproxy.proxy.action.Action;
import com.mattunderscore.tcproxy.proxy.action.BatchedWrite;
import com.mattunderscore.tcproxy.proxy.action.WriteAction;

/**
 * Implementation of {@link ActionQueue}.
 * @author Matt Champion on 19/02/14.
 */
public final class ActionQueueImpl implements ActionQueue {
    private final int batchSize;
    private final BlockingQueue<Action> actions;
    private volatile Action current = null;

    public ActionQueueImpl(final int queueSize, final int batchSize) {
        this.batchSize = batchSize;
        this.actions = new ArrayBlockingQueue<>(queueSize);
    }

    @Override
    public boolean queueFull() {
        return actions.remainingCapacity() == 0;
    }

    @Override
    public int actionsPending() {
        final Action currentAction = current;
        return actions.size() + (currentAction != null && !currentAction.writeComplete() ? 1 : 0);
    }

    @Override
    public void add(final Action action) {
        actions.add(action);
    }

    @Override
    public Action head() {
        final Action currentAction = current;
        if (currentAction != null && !currentAction.writeComplete()) {
            // There is a head action that has not been completed
            return currentAction;
        }
        else {
            final Action batchedAction = pollActions();
            current = batchedAction;
            return batchedAction;
        }
    }

    /**
     * Polls the next action from the queue, batching multiple write actions.
     */
    private Action pollActions() {
        final BatchedWrite batchedWrite = new BatchedWrite(batchSize);
        boolean batchedData = false;
        while (true) {
            final Action nextAction = actions.peek();
            if (nextAction != null && nextAction.isBatchable()) {
                if (batchedWrite.batch((WriteAction)nextAction)) {
                    batchedData = true;
                    actions.poll();
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
        return head() != null;
    }

}
