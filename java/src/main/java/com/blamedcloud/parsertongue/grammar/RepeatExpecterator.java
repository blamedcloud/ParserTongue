package com.blamedcloud.parsertongue.grammar;

import java.util.Optional;

import com.blamedcloud.parsertongue.tokenizer.Tokenizer;

public class RepeatExpecterator extends ParseResultExpecterator {

    private RHSTree tree;
    private ParseResultExpecterator childExpecterator;
    private boolean firstIteration;
    private boolean secondIteration;
    private String lastError;

    private boolean useChild;
    private ParseResultExpecterator repeatExpecterator;
    private ListParseResult childResult;
    private boolean firstRepeatIteration;

    protected RepeatExpecterator(RHSTree tree, Tokenizer tokenizer) {
        super(tokenizer);
        this.tree = tree;
        childExpecterator = null;
        firstIteration = true;
        secondIteration = false;
        lastError = null;

        useChild = false;
        repeatExpecterator = null;
        childResult = null;
        firstRepeatIteration = false;
    }

    @Override
    public boolean hasNext() {
        if (firstIteration || secondIteration || firstRepeatIteration || !useChild) {
            return true;
        } else {
            return childExpecterator.hasNext();
        }
    }

    @Override
    public Optional<ParseResultTransformer> tryNext() {
        // for the very first iteration, just try not returning from this repeat
        if (firstIteration) {
            firstIteration = false;
            secondIteration = true;
            return Optional.of(new ParseResultTransformer(true, new ListParseResult(), null));
        }

        if (secondIteration) {
            childExpecterator = tree.getChild().getExpecterator(tokens);
            useChild = true;
        }

        if (useChild) {
            if (childExpecterator.hasNext()) {
                if (secondIteration) {
                    secondIteration = false;
                } else {
                    childExpecterator.reset();
                }
                Optional<ParseResultTransformer> optionalResult = childExpecterator.tryNext();
                if (optionalResult.isPresent()) {
                    ParseResultTransformer actualResult = optionalResult.get();
                    if (actualResult.isValid()) {
                        ParseResultTransformer newResult = actualResult.transform(RepeatExpecterator::wrapInList);
                        if (!tokens.isExhausted()) {
                            repeatExpecterator = tree.getExpecterator(tokens);
                            useChild = false;
                            firstRepeatIteration = true;
                            childResult = (ListParseResult)newResult.getResult();
                            return tryNext();
                        } else {
                            return Optional.of(newResult);
                        }
                    } else {
                        lastError = actualResult.getError();
                    }
                }
            } else {
                secondIteration = false;
            }
        } else {
            if (repeatExpecterator.hasNext()) {
                if (firstRepeatIteration) {
                    firstRepeatIteration = false;
                } else {
                    repeatExpecterator.reset();
                }
                Optional<ParseResultTransformer> optionalResult = repeatExpecterator.tryNext();
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
                repeatExpecterator = null;
                useChild = true;
                childResult = null;
                firstRepeatIteration = false;
                return tryNext();
            }
        }

        if (lastError != null) {
            return Optional.of(new ParseResultTransformer(false, null, lastError));
        } else {
            return Optional.empty();
        }
    }

    static ListParseResult wrapInList(ParseResult input) {
        return new ListParseResult(input);
    }

}
