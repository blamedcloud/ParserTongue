package com.blamedcloud.parsertongue.grammar;

import java.util.Optional;

import com.blamedcloud.parsertongue.tokenizer.Tokenizer;

public class IdentifierExpecterator extends ParseResultExpecterator {

    private RHSTree tree;
    private ParseResultExpecterator linkExpecterator;
    private boolean firstIteration;
    private String lastError;

    protected IdentifierExpecterator(RHSTree tree, Tokenizer tokenizer) {
        super(tokenizer);
        this.tree = tree;
        linkExpecterator = null;
        firstIteration = true;
        lastError = null;
    }

    @Override
    public boolean hasNext() {
        if (firstIteration) {
            return true;
        } else {
            return linkExpecterator.hasNext();
        }
    }

    @Override
    public Optional<ParseResultTransformer> tryNext() {
        if (firstIteration) {
            linkExpecterator = tree.getLink().getExpecterator(tokens);
        }

        if (linkExpecterator.hasNext()) {
            if (firstIteration) {
                firstIteration = false;
            } else {
                reset();
            }
            Optional<ParseResultTransformer> optionalResult = linkExpecterator.tryNext();
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
