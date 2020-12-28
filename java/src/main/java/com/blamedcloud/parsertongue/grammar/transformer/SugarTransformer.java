package com.blamedcloud.parsertongue.grammar.transformer;

import java.util.HashMap;
import java.util.Map;

import com.blamedcloud.parsertongue.grammar.Grammar;
import com.blamedcloud.parsertongue.grammar.RHSKind;
import com.blamedcloud.parsertongue.grammar.RHSTree;
import com.blamedcloud.parsertongue.grammar.RHSType;
import com.blamedcloud.parsertongue.grammar.Rule;

public class SugarTransformer implements GrammarTransformer {

    private Grammar originalGrammar;

    private boolean grammarHasSugar;
    private Map<Rule, Boolean> ruleSugar;
    private Map<RHSTree, Boolean> rhsDirectSugar;
    private Map<RHSTree, Boolean> rhsNestedSugar;

    public SugarTransformer(Grammar grammar) {
        originalGrammar = grammar;
        ruleSugar = new HashMap<>();
        rhsDirectSugar = new HashMap<>();
        rhsNestedSugar = new HashMap<>();
        createSugarMaps();
    }

    private void createSugarMaps() {
        grammarHasSugar = false;
        ruleSugar.clear();
        rhsDirectSugar.clear();
        rhsNestedSugar.clear();

        for (Rule rule : originalGrammar.getRules()) {
            boolean ruleHasSugar = checkRule(rule);
            grammarHasSugar = grammarHasSugar || ruleHasSugar;
        }
    }

    private boolean checkRule(Rule rule) {
        if (rule.isRegexRule() || rule.hasDependency()) {
            ruleSugar.put(rule, false);
            return false;
        }
        boolean hasSugar = checkDirectSugar(rule.rhs());
        hasSugar = checkNestedSugar(rule.rhs()) || hasSugar;
        ruleSugar.put(rule, hasSugar);
        return hasSugar;
    }

    private boolean checkDirectSugar(RHSTree tree) {
        boolean directSugar = tree.getKind() == RHSKind.SINGLE;
        rhsDirectSugar.put(tree, directSugar);
        return directSugar;
    }

    private boolean checkNestedSugar(RHSTree tree) {
        if (rhsNestedSugar.containsKey(tree)) {
            return rhsNestedSugar.get(tree);
        }
        // put this in here to avoid infinite recursion.
        // I think the right value should end up in the map
        // when everything is said and done.
        rhsNestedSugar.put(tree, false);

        boolean nestedSugar = false;
        if (tree.getKind() == RHSKind.SINGLE || tree.getKind() == RHSKind.LIST) {
            for (int i = 0; i < tree.size(); i++) {
                RHSTree child = tree.getChild(i);
                nestedSugar = checkDirectSugar(child);
                nestedSugar = checkNestedSugar(child) || nestedSugar;
            }
        }

        if (tree.getType() == RHSType.IDENTIFIER) {
            nestedSugar = checkRule(tree.getLink());
        }

        rhsNestedSugar.put(tree, nestedSugar);
        return nestedSugar;
    }

    @Override
    public Grammar getOriginalGrammar() {
        return originalGrammar;
    }

    @Override
    public boolean containsAffectedRules() {
        return grammarHasSugar;
    }

    @Override
    public boolean isRuleAffected(Rule rule) {
        return ruleSugar.get(rule);
    }

    @Override
    public boolean isRHSAffected(Rule parent, RHSTree tree) {
        return rhsDirectSugar.get(tree) || rhsNestedSugar.get(tree);
    }

    @Override
    public Grammar getTransformedGrammar() {
        // TODO Auto-generated method stub
        return null;
    }

}
