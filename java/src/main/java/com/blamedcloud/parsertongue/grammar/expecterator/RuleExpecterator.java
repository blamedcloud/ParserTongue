package com.blamedcloud.parsertongue.grammar.expecterator;

import java.util.Optional;

import com.blamedcloud.parsertongue.grammar.Rule;
import com.blamedcloud.parsertongue.grammar.result.ParseResultTransformer;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;

public class RuleExpecterator extends ParseResultExpecterator {

    private Rule rule;
    private ParseResultExpecterator rhsExpecterator;
    private boolean firstIteration;

    public RuleExpecterator(Rule rule, Tokenizer tokenizer) {
        super(tokenizer);
        this.rule = rule;
        rhsExpecterator = null;
        firstIteration = true;
    }

    @Override
    public boolean hasNext() {
        if (firstIteration) {
            return true;
        } else {
            return rhsExpecterator.hasNext();
        }
    }

    @Override
    public Optional<ParseResultTransformer> tryNext() {
        if (firstIteration) {
            rhsExpecterator = rule.rhs().getExpecterator(tokens);
            firstIteration = false;
        }

        if (rhsExpecterator.hasNext()) {
            Optional<ParseResultTransformer> optionalResult = rhsExpecterator.tryNext();
            if (optionalResult.isPresent()) {
                ParseResultTransformer actualResult = optionalResult.get();
                if (actualResult.isValid()) {
                    ParseResultTransformer newResult = actualResult.transform(rule.getTransformer());
                    return Optional.of(newResult);
                }
            }
        }
        return Optional.empty();
    }

}
