package com.blamedcloud.parsertongue.grammar;

import java.util.Optional;

import com.blamedcloud.parsertongue.tokenizer.Token;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;

public class TerminalExpecterator extends ParseResultExpecterator {

    private Token node;
    private String nodeValue;

    private boolean haveNext;

    public TerminalExpecterator(RHSTree tree, Tokenizer tokenizer) {
        super(tokenizer);
        node = tree.getNode();
        nodeValue = node.getValue();
        haveNext = true;
    }

    @Override
    public boolean hasNext() {
        return haveNext;
    }

    @Override
    public Optional<ParseResultTransformer> tryNext() {
        haveNext = false;

        if (nodeValue.length() == 0) {
            return Optional.of(new ParseResultTransformer(true, new StringParseResult(""), null));
        } else if (!tokens.isExhausted() && nodeValue.equals(tokens.currentToken().getValue())) {
            tokens.nextToken();
            return Optional.of(new ParseResultTransformer(true, new StringParseResult(nodeValue), null));
        } else {
            String error = "ERROR: Expected '" + nodeValue + "', got: '" + tokens.currentToken().getValue() + "'";
            return Optional.of(new ParseResultTransformer(false, null, error));
        }
    }

}
