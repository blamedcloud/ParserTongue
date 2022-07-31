package com.blamedcloud.parsertongue.grammar.annotations;

import com.blamedcloud.parsertongue.grammar.result.ParseResultFunction;
import com.blamedcloud.parsertongue.tokenizer.Token;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;

public class NoArgAnnotation implements Annotation {

    private Token primaryToken;
    private ParseResultFunction function;

    public NoArgAnnotation(Token token, ParseResultFunction f) {
        primaryToken = token;
        function = f;
    }

    @Override
    public Token getPrimaryToken() {
        return primaryToken;
    }

    // Trusts that the tokens passed in are correct
    @Override
    public ParseResultFunction getFunction(Tokenizer tokens) {
        return function;
    }

}
