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

package com.mattunderscore.tcproxy.proxy;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author matt on 19/02/14.
 */
public class ActionQueueImpl implements ActionQueue {
    private final Direction direction;
    private final Connection connection;
    private final BlockingQueue<Action> actions;

    public ActionQueueImpl(final Direction direction, final Connection connection, final int queueSize) {
        this.direction = direction;
        this.connection = connection;
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
        final Action action = actions.peek();
        if (action == null) {
            return null;
        }
        else if (!action.writeComplete()) {
            return action;
        }
        else {
            actions.remove(action);
            return current();
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

    @Override
    public Direction getDirection() {
        return direction;
    }
}
