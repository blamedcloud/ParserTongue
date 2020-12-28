package com.blamedcloud.parsertongue.grammar.transformer;

import com.blamedcloud.parsertongue.grammar.Grammar;
import com.blamedcloud.parsertongue.grammar.RHSTree;
import com.blamedcloud.parsertongue.grammar.Rule;

public interface GrammarTransformer {

    public Grammar getOriginalGrammar();

    public boolean containsAffectedRules();

    public boolean isRuleAffected(Rule rule);

    public boolean isRHSAffected(Rule parent, RHSTree tree);

    public Grammar getTransformedGrammar();
}
