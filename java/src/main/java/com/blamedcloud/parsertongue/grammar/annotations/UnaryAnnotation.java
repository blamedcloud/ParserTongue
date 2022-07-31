package com.blamedcloud.parsertongue.grammar.annotations;

import java.util.function.Function;

import com.blamedcloud.parsertongue.grammar.result.ParseResultFunction;
import com.blamedcloud.parsertongue.tokenizer.Token;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;

public class UnaryAnnotation implements Annotation {

    private Token primaryToken;
    private Function<Token, ParseResultFunction> function;

    public UnaryAnnotation(Token token, Function<Token, ParseResultFunction> f) {
        primaryToken = token;
        function = f;
    }

    @Override
    public Token getPrimaryToken() {
        return primaryToken;
    }

    @Override
    public ParseResultFunction getFunction(Tokenizer tokens) {
        if (tokens.size() != 2) {
            throw new RuntimeException("Wrong number of tokens");
        }
        return function.apply(tokens.getLastToken());
    }

}
