package com.blamedcloud.parsertongue.grammar;

public class StringParseResult implements ParseResult {

    private final String value;

    public StringParseResult(String s) {
        value = s;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

}
