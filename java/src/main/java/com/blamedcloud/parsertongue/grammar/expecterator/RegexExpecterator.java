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
    private int consumableTokens;
    private int tokensConsumed;

    public RegexExpecterator(RHSTree tree, Tokenizer tokenizer) {
        super(tokenizer);
        regexNode = tree.getRegexNode();
        haveNext = true;
        tokensConsumed = 0;
        if (tokens.isExhausted()) {
            consumableTokens = 0;
        } else {
            consumableTokens = tokenizer.size() - tokenizer.getIndex();
        }
    }

    @Override
    public boolean hasNext() {
        return haveNext;
    }

    @Override
    public Optional<ParseResultTransformer> tryNext() {
        if (tokens.isExhausted() || tokens.size() == 0) {
            haveNext = false;
            if (regexNode.isTypeOf("")) {
                return Optional.of(new ParseResultTransformer(true, new StringParseResult(""), null));
            } else {
                String error = "ERROR: Expected token of type: '" + regexNode.getName() + "', got: ''";
                return Optional.of(new ParseResultTransformer(false, null, error));
            }
        } else {
            tokensConsumed++;
            if (tokensConsumed == consumableTokens) {
                haveNext = false;
            }
            reset();
            String mergedTokens = getNextNTokens(tokensConsumed);
            if (regexNode.isTypeOf(mergedTokens)) {
                return Optional.of(new ParseResultTransformer(true, new StringParseResult(mergedTokens), null));
            } else {
                reset();
                String error = "ERROR: Expected token of type: '" + regexNode.getName() + "', got: '" + mergedTokens + "'";
                return Optional.of(new ParseResultTransformer(false, null, error));
            }
        }
    }

    private String getNextNTokens(int n) {
        StringBuilder mergedTokens = new StringBuilder();
        while (n > 0) {
            mergedTokens.append(tokens.currentToken().getValue());
            tokens.nextToken();
            n--;
        }
        return mergedTokens.toString();
    }

}
