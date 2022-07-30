package com.blamedcloud.parsertongue.grammar.result;

@FunctionalInterface
public interface ParseResultFunction {
    public ParseResult apply(ParseResult parseResult) throws ParseResultException;
}
