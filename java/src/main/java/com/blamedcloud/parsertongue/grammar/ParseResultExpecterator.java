package com.blamedcloud.parsertongue.grammar;

import com.blamedcloud.parsertongue.tokenizer.Tokenizer;

public abstract class ParseResultExpecterator implements Expecterator<ParseResultTransformer> {

    protected Tokenizer tokens;
    protected int initialIndex;
    protected boolean initialExhausted;

    protected ParseResultExpecterator(Tokenizer tokenizer) {
        tokens = tokenizer;
        initialIndex = tokenizer.getIndex();
        initialExhausted = tokenizer.isExhausted();
    }

    @Override
    public void reset() {
        tokens.setIndex(initialIndex, initialExhausted);
    }

}
