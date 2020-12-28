package com.blamedcloud.parsertongue.grammar.expecterator;

import java.util.Optional;

import com.blamedcloud.parsertongue.grammar.RHSTree;
import com.blamedcloud.parsertongue.grammar.result.ParseResultTransformer;
import com.blamedcloud.parsertongue.grammar.result.StringParseResult;
import com.blamedcloud.parsertongue.tokenizer.TokenType;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;

public class RegexExpecterator extends ParseResultExpecterator {

    private TokenType regexNode;

    private boolean haveNext;

    public RegexExpecterator(RHSTree tree, Tokenizer tokenizer) {
        super(tokenizer);
        regexNode = tree.getRegexNode();
        haveNext = true;
    }

    @Override
    public boolean hasNext() {
        return haveNext;
    }

    @Override
    public Optional<ParseResultTransformer> tryNext() {
        haveNext = false;

        if (!tokens.isExhausted() && regexNode.isTypeOf(tokens.currentToken().getValue())) {
            String tokenValue = tokens.currentToken().getValue();
            tokens.nextToken();
            return Optional.of(new ParseResultTransformer(true, new StringParseResult(tokenValue), null));
        } else {
            String error = "ERROR: Expected token of type: '" + regexNode.getName() + "', got: '" + tokens.currentToken().getValue() + "'";
            return Optional.of(new ParseResultTransformer(false, null, error));
        }
    }

}
