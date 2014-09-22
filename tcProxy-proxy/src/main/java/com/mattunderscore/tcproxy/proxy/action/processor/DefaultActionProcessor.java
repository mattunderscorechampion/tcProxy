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

package com.mattunderscore.tcproxy.proxy.action.processor;

import com.mattunderscore.tcproxy.proxy.Connection;
import com.mattunderscore.tcproxy.proxy.Direction;
import com.mattunderscore.tcproxy.proxy.DirectionAndConnection;
import com.mattunderscore.tcproxy.proxy.action.Action;
import com.mattunderscore.tcproxy.proxy.action.queue.ActionQueue;

import java.util.Queue;

/**
 * ActionProcessor that puts the action on the directions action queue and the direction on the new direction queue if
 * the action queue is empty.
 * @author Matt Champion on 22/03/14.
 */
public final class DefaultActionProcessor implements ActionProcessor {
    private final DirectionAndConnection direction;
    private final ActionQueue actionQueue;
    private final Queue<DirectionAndConnection> directions;

    /**
     * Constructor for the default behaviour.
     * @param direction The direction the processor is for.
     * @param directions The queue of new directions.
     */
    public DefaultActionProcessor(final DirectionAndConnection direction, final Queue<DirectionAndConnection> directions) {
        this.direction = direction;
        this.actionQueue = direction.getDirection().getQueue();
        this.directions = directions;
    }

    @Override
    public void process(final Action action) {
        synchronized (actionQueue) {
            final boolean hasData = actionQueue.hasData();
            actionQueue.add(action);
            if (!hasData) {
                directions.add(direction);
            }
        }
    }

    @Override
    public synchronized void flush() {
    }
}
