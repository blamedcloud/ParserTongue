package com.blamedcloud.parsertongue.grammar;

import java.util.function.Function;

public class ParseResultTransformer {

    private final boolean valid;
    private final ParseResult result;
    private final String errorMessage;

    public ParseResultTransformer(boolean valid, ParseResult result, String error) {
        this.valid = valid;
        this.result = result;
        this.errorMessage = error;
    }

    public boolean isValid() {
        return valid;
    }

    public ParseResult getResult() {
        return result;
    }

    public String getError() {
        return errorMessage;
    }

    public ParseResultTransformer transform(Function<ParseResult, ParseResult> f) {
        if (valid) {
            return new ParseResultTransformer(valid, f.apply(result), errorMessage);
        } else {
            throw new RuntimeException("Can't transform failed parse!");
        }
    }

    public static ParseResult identity(ParseResult parseResult) {
        return parseResult;
    }
}
