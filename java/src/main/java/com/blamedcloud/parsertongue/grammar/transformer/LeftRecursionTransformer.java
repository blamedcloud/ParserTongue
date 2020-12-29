package com.blamedcloud.parsertongue.grammar.transformer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.blamedcloud.parsertongue.grammar.Grammar;
import com.blamedcloud.parsertongue.grammar.RHSKind;
import com.blamedcloud.parsertongue.grammar.RHSTree;
import com.blamedcloud.parsertongue.grammar.RHSType;
import com.blamedcloud.parsertongue.grammar.Rule;

public class LeftRecursionTransformer implements GrammarTransformer {

    private Grammar originalGrammar;

    private Set<String> directLRRules;
    private Set<String> indirectLRRules;

    public LeftRecursionTransformer(Grammar grammar) {
        originalGrammar = grammar;
        directLRRules = new HashSet<>();
        indirectLRRules = new HashSet<>();
        checkLeftRecursion();
    }

    private void checkLeftRecursion() {
        Map<String, Set<String>> directLeftCornerIdentifiers = new HashMap<>();
        for (Map.Entry<String, Rule> entry : originalGrammar.getRuleMap().entrySet()) {
            String ruleName = entry.getKey();
            Rule rule = entry.getValue();
            Set<String> dlcIdentifiersOfRule = getRuleDirectLeftCornerIdentifiers(rule);
            directLeftCornerIdentifiers.put(ruleName, dlcIdentifiersOfRule);

            if (dlcIdentifiersOfRule.contains(ruleName)) {
                directLRRules.add(ruleName);
            }
        }

        Map<String, Set<String>> properLeftCornerIdentifiers = new HashMap<>();
        for (Map.Entry<String, Rule> entry : originalGrammar.getRuleMap().entrySet()) {
            String ruleName = entry.getKey();
            Set<String> properLCIsOfRule = getTransitiveClosure(ruleName, directLeftCornerIdentifiers, new HashSet<>());
            properLeftCornerIdentifiers.put(ruleName, properLCIsOfRule);
            if (!directLRRules.contains(ruleName) && properLCIsOfRule.contains(ruleName)) {
                indirectLRRules.add(ruleName);
            }
        }
    }

    private Set<String> getRuleDirectLeftCornerIdentifiers(Rule rule) {
        Set<String> dlcIdentifiers = new HashSet<>();
        // external rules could be thought of as direct
        // left corners, but they cannot contribute to
        // left recursion because cyclic grammar
        // dependencies are not allowed, so we don't
        // include them here.
        if (!rule.isRegexRule() && !rule.hasDependency()) {
            dlcIdentifiers = getRHSDirectLeftCornerIdentifiers(rule.rhs());
        }
        return dlcIdentifiers;
    }

    private Set<String> getRHSDirectLeftCornerIdentifiers(RHSTree tree) {
        Set<String> dlcIdentifiers = new HashSet<>();

        RHSType type = tree.getType();
        if (tree.getKind() == RHSKind.SINGLE || type == RHSType.CONCATENATION) {
            dlcIdentifiers = getRHSDirectLeftCornerIdentifiers(tree.getChild());
        } else if (type == RHSType.IDENTIFIER) {
            dlcIdentifiers.add(tree.getNode().getValue());
        } else if (type == RHSType.ALTERNATION) {
            for (RHSTree child : tree.getChildren()) {
                dlcIdentifiers.addAll(getRHSDirectLeftCornerIdentifiers(child));
            }
        }

        return dlcIdentifiers;
    }

    private <T> Set<T> getTransitiveClosure(T lhs, Map<T, Set<T>> relation, Set<T> alreadyRelated) {
        Set<T> directlyRelated = relation.get(lhs);
        for (T t : directlyRelated) {
            if (!alreadyRelated.contains(t)) {
                alreadyRelated.add(t);
                alreadyRelated = getTransitiveClosure(t, relation, alreadyRelated);
            }
        }
        return alreadyRelated;
    }


    @Override
    public Grammar getOriginalGrammar() {
        return originalGrammar;
    }

    @Override
    public boolean isGrammarAffected() {
        return directLRRules.size() > 0 || indirectLRRules.size() > 0;
    }

    @Override
    public boolean isRuleAffected(Rule rule) {
        String ruleName = rule.lhs().getValue();
        return directLRRules.contains(ruleName) || indirectLRRules.contains(ruleName);
    }

    public boolean hasDirectLR(Rule rule) {
        return directLRRules.contains(rule.lhs().getValue());
    }

    public boolean hasIndirectLR(Rule rule) {
        return indirectLRRules.contains(rule.lhs().getValue());
    }

    @Override
    public boolean isRHSAffected(Rule parent, RHSTree tree) {
        return isRuleAffected(parent);
    }

    @Override
    public Grammar getTransformedGrammar() {
        // TODO Auto-generated method stub
        return null;
    }

}