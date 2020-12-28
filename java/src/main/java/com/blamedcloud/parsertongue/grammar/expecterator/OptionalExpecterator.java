package com.blamedcloud.parsertongue.grammar.expecterator;

import java.util.Optional;

import com.blamedcloud.parsertongue.grammar.RHSTree;
import com.blamedcloud.parsertongue.grammar.result.ParseResultTransformer;
import com.blamedcloud.parsertongue.grammar.result.StringParseResult;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;

public class OptionalExpecterator extends ParseResultExpecterator {

    private RHSTree tree;
    private ParseResultExpecterator childExpecterator;
    private boolean firstIteration;
    private boolean secondIteration;
    private String lastError;

    public OptionalExpecterator(RHSTree tree, Tokenizer tokenizer) {
        super(tokenizer);
        this.tree = tree;
        childExpecterator = null;
        firstIteration = true;
        secondIteration = false;
        lastError = null;
    }

    @Override
    public boolean hasNext() {
        if (firstIteration || secondIteration) {
            return true;
        } else {
            return childExpecterator.hasNext();
        }
    }

    @Override
    public Optional<ParseResultTransformer> tryNext() {
        // for the very first iteration, just try not returning from this optional
        if (firstIteration) {
            firstIteration = false;
            secondIteration = true;
            return Optional.of(new ParseResultTransformer(true, new StringParseResult(""), null));
        }

        // the second and onward iterations should be the same as a GroupExpecterator
        if (secondIteration) {
            childExpecterator = tree.getChild().getExpecterator(tokens);
        }

        if (childExpecterator.hasNext()) {
            if (secondIteration) {
                secondIteration = false;
            } else {
                reset();
            }
            Optional<ParseResultTransformer> optionalResult = childExpecterator.tryNext();
            if (optionalResult.isPresent()) {
                ParseResultTransformer actualResult = optionalResult.get();
                if (actualResult.isValid()) {
                    return Optional.of(actualResult);
                } else {
                    lastError = actualResult.getError();
                }
            } else if (lastError != null) {
                return Optional.of(new ParseResultTransformer(false, null, lastError));
            }
        } else {
            secondIteration = false;
        }
        return Optional.empty();
    }


}
