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

    // TODO: the very initial values might not be the right ones to reset to...
    // more thinking / testing required
    @Override
    public void reset() {
        tokens.setIndex(initialIndex, initialExhausted);
    }

}
