package com.blamedcloud.parsertongue.grammar;

import java.util.Optional;

import com.blamedcloud.parsertongue.tokenizer.Tokenizer;

public class AlternationExpecterator extends ParseResultExpecterator {

    private RHSTree tree;
    private ParseResultExpecterator childExpecterator;
    private int childIndex;
    private int numChildren;
    private boolean firstIteration;
    private String lastError;

    protected AlternationExpecterator(RHSTree tree, Tokenizer tokenizer) {
        super(tokenizer);
        this.tree = tree;
        childExpecterator = null;
        childIndex = 0;
        numChildren = tree.size();
        firstIteration = true;
        lastError = null;
    }

    @Override
    public boolean hasNext() {
        if (firstIteration || childIndex < numChildren) {
            return true;
        } else {
            return childExpecterator.hasNext();
        }
    }

    // TODO: test this
    @Override
    public Optional<ParseResultTransformer> tryNext() {
        if (firstIteration) {
            childExpecterator = tree.getChild(childIndex).getExpecterator(tokens);
            childIndex++;
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
            } else {
                return tryNext();
            }
        } else {
            if (childIndex < numChildren) {
                firstIteration = true;
                return tryNext();
            } else if (lastError != null) {
                return Optional.of(new ParseResultTransformer(false, null, lastError));
            }
        }

        return Optional.empty();
    }

}
