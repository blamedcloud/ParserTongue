package com.blamedcloud.parsertongue.grammar.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blamedcloud.parsertongue.grammar.Grammar;
import com.blamedcloud.parsertongue.grammar.RHSKind;
import com.blamedcloud.parsertongue.grammar.RHSTree;
import com.blamedcloud.parsertongue.grammar.RHSType;
import com.blamedcloud.parsertongue.grammar.Rule;
import com.blamedcloud.parsertongue.tokenizer.Token;

public class SugarTransformer implements GrammarTransformer {

    private Grammar originalGrammar;

    private boolean grammarHasSugar;
    private Map<Rule, Boolean> ruleSugar;
    private Map<RHSTree, Boolean> rhsDirectSugar;
    private Map<RHSTree, Boolean> rhsNestedSugar;

    private Grammar newGrammar;
    private Set<String> ruleNames;
    private List<Rule> newRules;

    private static final String GROUP_SUFFIX = "_grp";
    private static final String OPTIONAL_SUFFIX = "_opt";
    private static final String REPEAT_SUFFIX = "_rep";

    public SugarTransformer(Grammar grammar) {
        originalGrammar = grammar;
        ruleSugar = new HashMap<>();
        rhsDirectSugar = new HashMap<>();
        rhsNestedSugar = new HashMap<>();

        newGrammar = null;
        ruleNames = null;
        newRules = null;

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
            for (RHSTree child : tree.getChildren()) {
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

    private void createNewGrammar() {
        ruleNames = new HashSet<>();
        ruleNames.addAll(originalGrammar.getRuleMap().keySet());
        newRules = new ArrayList<>();

        for (Rule oldRule : originalGrammar.getRules()) {
            if (isRuleAffected(oldRule)) {
                newRules.add(createDeSugaredRule(oldRule));
            } else {
                newRules.add(oldRule.copy());
            }
        }

        if (originalGrammar.hasDependencies()) {
            newGrammar = new Grammar(newRules, originalGrammar.getStartRuleName(), true);
            newGrammar.setExternalRuleMaps(originalGrammar.getExternalRuleMaps());
            newGrammar.linkRules();
        } else {
            newGrammar = new Grammar(newRules, originalGrammar.getStartRuleName(), false);
        }
    }

    private Rule createDeSugaredRule(Rule rule) {
        Rule.Builder builder = Rule.newBuilder()
                .setLHSToken(rule.lhs().copy())
                .setRHSTree(createDeSugaredRHS(rule, rule.rhs()))
                .setTransformer(rule.getTransformer());
        if (rule.hasDependency()) {
        builder.setExternalName(rule.getDependencyName());
        }
        if (rule.isRegexRule()) {
        builder.setRegexTokenType(rule.getRegexTokenType());
        }
        return builder.build();
    }

    private RHSTree createDeSugaredRHS(Rule parent, RHSTree tree) {
        RHSTree newTree;
        if (tree.getKind() == RHSKind.LEAF) {
            newTree = tree.copy();
        } else if (tree.getKind() == RHSKind.LIST) {
            newTree = new RHSTree(tree.getType());
            for (RHSTree child : tree.getChildren()) {
                newTree.addChild(createDeSugaredRHS(parent, child));
            }
        } else { // tree.getKind == RHSKind.SINGLE
            newTree = new RHSTree(RHSType.IDENTIFIER);
            Token ruleLHS = addNewRule(parent, tree);
            newTree.createNode(ruleLHS);
        }

        return newTree;
    }

    private Token addNewRule(Rule parent, RHSTree tree) {
        Token ruleLHS;
        Rule newRule;
        if (tree.getType() == RHSType.GROUP) {
            String identifier = parent.lhs().getValue() + GROUP_SUFFIX;
            ruleLHS = Grammar.getNextIdentifier(identifier, ruleNames);
            ruleNames.add(ruleLHS.getValue());

            newRule = Rule.newBuilder()
                               .setLHSToken(ruleLHS)
                               .setRHSTree(createDeSugaredRHS(parent, tree.getChild()))
                               .build();
        } else if (tree.getType() == RHSType.OPTIONAL) {
            String identifier = parent.lhs().getValue() + OPTIONAL_SUFFIX;
            ruleLHS = Grammar.getNextIdentifier(identifier, ruleNames);
            ruleNames.add(ruleLHS.getValue());

            RHSTree newTree = new RHSTree(RHSType.ALTERNATION);

            RHSTree epsilon = getEmptyStringTree();
            RHSTree child = createDeSugaredRHS(parent, tree.getChild());

            newTree.addChild(child);
            newTree.addChild(epsilon);

            newRule = Rule.newBuilder()
                          .setLHSToken(ruleLHS)
                          .setRHSTree(newTree)
                          .build();
        } else { // tree.getType() == RHSType.REPEAT
            String identifier = parent.lhs().getValue() + REPEAT_SUFFIX;
            ruleLHS = Grammar.getNextIdentifier(identifier, ruleNames);
            ruleNames.add(ruleLHS.getValue());

            RHSTree newTree = new RHSTree(RHSType.ALTERNATION);

            RHSTree epsilon = getEmptyStringTree();

            RHSTree leftTree = new RHSTree(RHSType.CONCATENATION);

            RHSTree child = createDeSugaredRHS(parent, tree.getChild());
            RHSTree ruleTree = new RHSTree(RHSType.IDENTIFIER);
            ruleTree.createNode(ruleLHS);

            leftTree.addChild(child);
            leftTree.addChild(ruleTree);

            newTree.addChild(leftTree);
            newTree.addChild(epsilon);

            newRule = Rule.newBuilder()
                          .setLHSToken(ruleLHS)
                          .setRHSTree(newTree)
                          .build();
        }

        newRules.add(newRule);
        return ruleLHS;
    }

    private RHSTree getEmptyStringTree() {
        RHSTree epsilon = new RHSTree(RHSType.TERMINAL);
        Token emptyString = Grammar.getEmptyStringToken();
        epsilon.createNode(emptyString);
        return epsilon;
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
        if (newGrammar == null) {
            createNewGrammar();
        }
        return newGrammar;
    }

}
