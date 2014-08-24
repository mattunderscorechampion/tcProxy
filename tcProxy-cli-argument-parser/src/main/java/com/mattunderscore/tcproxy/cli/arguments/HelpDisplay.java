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

package com.mattunderscore.tcproxy.cli.arguments;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author matt on 24/08/14.
 */
public final class HelpDisplay {
    private final List<String> shortFlags;
    private final List<String> longFlags;
    private final List<String> descriptions;
    private final int maxShortFlagLength;
    private final int maxLongFlagLength;

    public HelpDisplay(Option<?>... options) {
        shortFlags = new ArrayList<>();
        longFlags = new ArrayList<>();
        descriptions = new ArrayList<>();
        int maxShortFlagLength = 0;
        int maxLongFlagLength = 0;
        for (Option<?> option : options) {
            final String shortFlag = option.getShortFlag();
            final String longFlag = option.getLongFlag();

            maxShortFlagLength = Math.max(maxShortFlagLength, shortFlag.length());
            maxLongFlagLength = Math.max(maxLongFlagLength, longFlag.length());

            shortFlags.add(shortFlag);
            longFlags.add(longFlag);
            descriptions.add(option.getDescription());
        }
        this.maxShortFlagLength = maxShortFlagLength;
        this.maxLongFlagLength = maxLongFlagLength;
    }

    public void printTo(PrintStream out) throws IOException {
        for (int i = 0; i < shortFlags.size(); i++) {
            final String shortFlag = shortFlags.get(i);
            final String longFlag = longFlags.get(i);
            final String description = descriptions.get(i);

            out.print(shortFlag);
            for (int j = 0; j < (maxShortFlagLength - shortFlag.length()) + 1; j++) {
                out.print(" ");
            }
            out.print(longFlag);
            for (int j = 0; j < (maxLongFlagLength - longFlag.length()) + 1; j++) {
                out.print(" ");
            }
            out.print(description);
            out.println();
        }
    }
}
