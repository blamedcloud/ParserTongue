package com.blamedcloud.parsertongue.grammar.result;

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

    @Override
    public String toString() {
        return "{valid: '" + valid + "'; result: '" + result + "'; error: '" + errorMessage + "'}";
    }

    public ParseResultTransformer transform(ParseResultFunction f) {
        if (valid) {
            try {
                return new ParseResultTransformer(true, f.apply(result), null);
            } catch (ParseResultException e) {
                return new ParseResultTransformer(false, null, e.getMessage());
            }
        } else {
            return this;
        }
    }

    public static ParseResultFunction identity = (x -> x);
}
