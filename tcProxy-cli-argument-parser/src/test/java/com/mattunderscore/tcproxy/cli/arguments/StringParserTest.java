package com.mattunderscore.tcproxy.cli.arguments;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link StringParser}.
 * @author Matt Champion on 04/05/16
 */
public final class StringParserTest {

    @Test
    public void parse() throws NotParsableException {
        final StringParser parser = StringParser.PARSER;
        final String setting = parser.parse("hello");
        assertEquals("hello", setting);
    }
}
