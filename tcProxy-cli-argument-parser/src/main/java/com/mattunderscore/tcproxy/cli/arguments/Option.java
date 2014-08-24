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

/**
 * @author matt on 23/08/14.
 */
public final class Option<T> {
    public static <T> Option<T> create(String shortFlag, String longFlag, String description, T defaultValue, SettingParser<T> parser) {
        return new Option(shortFlag, longFlag, description, defaultValue, parser);
    }

    public static Option<Void> create(String shortFlag, String longFlag, String description) {
        return new Option(shortFlag, longFlag, description, null, null);
    }

    private final String shortFlag;
    private final String longFlag;
    private final String description;
    private final T defaultValue;
    private final SettingParser<T> parser;

    private Option(String shortFlag, String longFlag, String description, T defaultValue, SettingParser<T> parser) {
        this.shortFlag = shortFlag;
        this.longFlag = longFlag;
        this.description = description;
        this.defaultValue = defaultValue;
        this.parser = parser;
    }

    public String getShortFlag() {
        return shortFlag;
    }

    public String getLongFlag() {
        return longFlag;
    }

    public String getDescription() {
        return description;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public SettingParser<T> getParser() {
        return parser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Option option = (Option) o;

        if (defaultValue != null ? !defaultValue.equals(option.defaultValue) : option.defaultValue != null)
            return false;
        if (!description.equals(option.description)) return false;
        if (!longFlag.equals(option.longFlag)) return false;
        if (parser != null ? !parser.equals(option.parser) : option.parser != null) return false;
        if (!shortFlag.equals(option.shortFlag)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = shortFlag.hashCode();
        result = 31 * result + longFlag.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        result = 31 * result + (parser != null ? parser.hashCode() : 0);
        return result;
    }
}
