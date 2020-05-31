package com.blamedcloud.parsertongue.grammar.expecterator;

import java.util.Optional;

import com.blamedcloud.parsertongue.grammar.ParseResultTransformer;
import com.blamedcloud.parsertongue.grammar.RHSTree;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;

public class GroupExpecterator extends ParseResultExpecterator {

    private RHSTree tree;
    private ParseResultExpecterator childExpecterator;
    private boolean firstIteration;
    private String lastError;

    public GroupExpecterator(RHSTree tree, Tokenizer tokenizer) {
        super(tokenizer);
        this.tree = tree;
        childExpecterator = null;
        firstIteration = true;
        lastError = null;
    }

    @Override
    public boolean hasNext() {
        if (firstIteration) {
            return true;
        } else {
            return childExpecterator.hasNext();
        }
    }

    @Override
    public Optional<ParseResultTransformer> tryNext() {
        if (firstIteration) {
            childExpecterator = tree.getChild().getExpecterator(tokens);
        }

        if (childExpecterator.hasNext()) {
            if (firstIteration) {
                firstIteration = false;
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
            firstIteration = false;
        }
        return Optional.empty();
    }


}
