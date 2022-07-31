package com.blamedcloud.parsertongue.grammar.annotations;

import com.blamedcloud.parsertongue.grammar.result.ParseResultFunction;
import com.blamedcloud.parsertongue.tokenizer.Token;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;

public interface Annotation {

    public Token getPrimaryToken();

    public ParseResultFunction getFunction(Tokenizer tokens);

}
