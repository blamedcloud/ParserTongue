package com.blamedcloud.parsertongue.grammar;

import java.util.Optional;

import com.blamedcloud.parsertongue.tokenizer.Tokenizer;

public class ConcatenationExpecterator extends ParseResultExpecterator {

    private RHSTree tree;
    private int childIndex;
    private ParseResultExpecterator childExpecterator;
    private boolean firstIteration;
    private String lastError;

    private boolean useChild;
    private ParseResultExpecterator concatExpecterator;
    private ListParseResult childResult;
    private boolean firstConcatIteration;

    protected ConcatenationExpecterator(RHSTree tree, int index, Tokenizer tokenizer) {
        super(tokenizer);
        this.tree = tree;
        childIndex = index;
        childExpecterator = null;
        firstIteration = true;
        lastError = null;

        useChild = false;
        concatExpecterator = null;
        childResult = null;
        firstConcatIteration = false;
    }

    @Override
    public boolean hasNext() {
        if (firstIteration || firstConcatIteration || !useChild) {
            return true;
        } else {
            return childExpecterator.hasNext();
        }
    }

    @Override
    public Optional<ParseResultTransformer> tryNext() {
        if (firstIteration) {
            childExpecterator = tree.getChild(childIndex).getExpecterator(tokens);
            useChild = true;
        }

        if (useChild) {
            if (childExpecterator.hasNext()) {
                if (firstIteration) {
                    firstIteration = false;
                } else {
                    childExpecterator.reset();
                }
                Optional<ParseResultTransformer> optionalResult = childExpecterator.tryNext();
                if (optionalResult.isPresent()) {
                    ParseResultTransformer actualResult = optionalResult.get();
                    if (actualResult.isValid()) {
                        ParseResultTransformer newResult = actualResult.transform(ListParseResult::wrapInList);
                        if (childIndex + 1 == tree.size()) {
                            return Optional.of(newResult);
                        } else {
                            concatExpecterator = new ConcatenationExpecterator(tree, childIndex + 1, tokens);
                            useChild = false;
                            firstConcatIteration = true;
                            childResult = (ListParseResult) newResult.getResult();
                            return tryNext();
                        }
                    } else {
                        lastError = actualResult.getError();
                    }
                }
            } else {
                firstIteration = false;
            }
        } else {
            if (concatExpecterator.hasNext()) {
                if (firstConcatIteration) {
                    firstConcatIteration = false;
                } else {
                    concatExpecterator.reset();
                }
                Optional<ParseResultTransformer> optionalResult = concatExpecterator.tryNext();
                if (optionalResult.isPresent()) {
                    ParseResultTransformer actualResult = optionalResult.get();
                    if (actualResult.isValid()) {
                        ParseResultTransformer newResult = actualResult.transform(r -> {
                            ListParseResult listResult = childResult.copy();
                            listResult.extend((ListParseResult)r);
                            return listResult;
                        });
                        return Optional.of(newResult);
                    } else {
                        lastError = actualResult.getError();
                    }
                }
            } else {
                concatExpecterator = null;
                useChild = true;
                childResult = null;
                firstConcatIteration = false;
                return tryNext();
            }
        }

        if (lastError != null) {
            return Optional.of(new ParseResultTransformer(false, null, lastError));
        } else {
            return Optional.empty();
        }
    }

}
