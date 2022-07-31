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
import com.blamedcloud.parsertongue.utility.FixedPair;
import com.blamedcloud.parsertongue.utility.Pair;

// I think this class (or, rather the paper it was based on) doesn't
// perfectly remove all types of left recursion, but it does work
// for some. It can also be used to check for left recursion.
public class LeftRecursionTransformer implements GrammarTransformer {

    private Grammar originalGrammar;

    private Set<String> directLRRules;
    private Set<String> indirectLRRules;
    private Set<String> allLRRules;

    private Map<String, Set<String>> directLeftCorners;
    private Map<String, Set<String>> properLeftCorners;

    private Set<String> dlcOfLRRules;

    private Set<String> retainedLRRules;
    private Set<String> allRuleNames;

    private Grammar newGrammar;
    private List<Rule> newRules;
    private Map<FixedPair<String, String>, Token> newRuleTokens;
    private Map<String, Pair<Token, List<List<RHSTree>>>> newProductions;

    public LeftRecursionTransformer(Grammar grammar) {
        originalGrammar = grammar;
        directLRRules = new HashSet<>();
        indirectLRRules = new HashSet<>();
        allLRRules = new HashSet<>();
        directLeftCorners = new HashMap<>();
        properLeftCorners = new HashMap<>();
        dlcOfLRRules = new HashSet<>();
        retainedLRRules = new HashSet<>();
        allRuleNames = new HashSet<>();

        newGrammar = null;
        newRules = null;
        newRuleTokens = null;
        newProductions = null;

        checkLeftRecursion();
        checkRetainedLRRules();
    }

    private void checkLeftRecursion() {
        for (Map.Entry<String, Rule> entry : originalGrammar.getRuleMap().entrySet()) {
            String ruleName = entry.getKey();
            Rule rule = entry.getValue();
            Set<String> dlcsOfRule = getRuleDirectLeftCorners(rule);
            directLeftCorners.put(ruleName, dlcsOfRule);
            if (dlcsOfRule.contains(ruleName)) {
                directLRRules.add(ruleName);
                allLRRules.add(ruleName);
            }

            allRuleNames.add(ruleName);
        }

        for (Map.Entry<String, Rule> entry : originalGrammar.getRuleMap().entrySet()) {
            String ruleName = entry.getKey();
            Set<String> properLCsOfRule = getTransitiveClosure(ruleName, directLeftCorners, new HashSet<>());
            properLeftCorners.put(ruleName, properLCsOfRule);
            if (!directLRRules.contains(ruleName) && properLCsOfRule.contains(ruleName)) {
                indirectLRRules.add(ruleName);
                allLRRules.add(ruleName);
            }
        }

        for (String ruleName : allLRRules) {
            dlcOfLRRules.addAll(directLeftCorners.get(ruleName));
        }
    }

    private Set<String> getRuleDirectLeftCorners(Rule rule) {
        Set<String> dlcs = new HashSet<>();
        // external rules could be thought of as direct
        // left corners, but they cannot contribute to
        // left recursion because cyclic grammar
        // dependencies are not allowed, so we don't
        // include them here.
        if (!rule.isRegexRule() && !rule.hasDependency()) {
            dlcs = getRHSDirectLeftCorners(rule.rhs());
        }
        return dlcs;
    }

    private Set<String> getRHSDirectLeftCorners(RHSTree tree) {
        Set<String> dlcs = new HashSet<>();

        RHSType type = tree.getType();
        if (tree.getKind() == RHSKind.SINGLE || type == RHSType.CONCATENATION) {
            dlcs = getRHSDirectLeftCorners(tree.getChild());
        } else if (type == RHSType.IDENTIFIER || type == RHSType.TERMINAL) {
            dlcs.add(tree.getNode().getValue());
        } else if (type == RHSType.ALTERNATION) {
            for (RHSTree child : tree.getChildren()) {
                dlcs.addAll(getRHSDirectLeftCorners(child));
            }
        }

        return dlcs;
    }

    private <T> Set<T> getTransitiveClosure(T lhs, Map<T, Set<T>> relation, Set<T> alreadyRelated) {
        if (relation.containsKey(lhs)) {
            Set<T> directlyRelated = relation.get(lhs);
            for (T t : directlyRelated) {
                if (!alreadyRelated.contains(t)) {
                    alreadyRelated.add(t);
                    alreadyRelated = getTransitiveClosure(t, relation, alreadyRelated);
                }
            }
        }
        return alreadyRelated;
    }

    private void checkRetainedLRRules() {
        for (String ruleName : allLRRules) {
            if (ruleName.equals(originalGrammar.getStartRuleName())) {
                retainedLRRules.add(ruleName);
            } else {
                for (Rule otherRule : originalGrammar.getRules()) {
                    if (otherRule.hasDependency() || otherRule.isRegexRule()) {
                        continue;
                    }
                    if (isRetainedBy(ruleName, otherRule.rhs())) {
                        retainedLRRules.add(ruleName);
                        break;
                    }
                }
            }
        }
    }

    private boolean isRetainedBy(String ruleName, RHSTree tree) {
        if (tree.getKind() == RHSKind.SINGLE) {
            throw new RuntimeException("LeftRecursionTransformer doesn't support syntactic sugar");
        }
        RHSType type = tree.getType();

        if (type == RHSType.ALTERNATION) {
            for (RHSTree child : tree.getChildren()) {
                if (isRetainedBy(ruleName, child)) {
                    return true;
                }
            }
        }

        if (type == RHSType.CONCATENATION) {
            // ideally all the children should be leaf's, so this
            // will always be false, but it doesn't hurt to check.
            if (isRetainedBy(ruleName, tree.getChild(0))) {
                return true;
            }

            for (int i = 1; i < tree.size(); i++) {
                RHSTree child = tree.getChild(i);
                // again, all the children should be leaf's,
                // so this first if-block should only fail on
                // terminals but it doesn't hurt to be careful.
                if (child.getType() == RHSType.IDENTIFIER) {
                    if (child.getNode().getValue().equals(ruleName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private List<Pair<String, List<RHSTree>>> getProductions(Rule rule) {
        // this method should only ever be called on LR rules,
        // and these types of rules are excluded.
        if (rule.hasDependency() || rule.isRegexRule()) {
            throw new RuntimeException("getProductions called on external or regex rule!");
        }
        return getProductions(rule.rhs());
    }

    private List<Pair<String, List<RHSTree>>> getProductions(RHSTree tree) {
        List<Pair<String, List<RHSTree>>> productions = new ArrayList<>();
        RHSType type = tree.getType();

        if (tree.getKind() == RHSKind.SINGLE) {
            throw new RuntimeException("LeftRecursionTransformer doesn't support syntactic sugar");
        }

        if (type == RHSType.IDENTIFIER || type == RHSType.TERMINAL) {
            List<RHSTree> following = new ArrayList<>();
            String value = tree.getNode().getValue();
            productions.add(new Pair<>(value, following));
        }

        if (type == RHSType.ALTERNATION) {
            for (RHSTree child : tree.getChildren()) {
                productions.addAll(getProductions(child));
            }
        }

        if (type == RHSType.CONCATENATION) {
            RHSTree firstChild = tree.getChild(0);
            RHSType firstChildType = firstChild.getType();
            String firstChildValue;
            if (firstChildType == RHSType.IDENTIFIER || firstChildType == RHSType.TERMINAL) {
                firstChildValue = firstChild.getNode().getValue();
            } else {
                throw new RuntimeException("LeftRecursionTransformer: Badly formatted RHSTree, expected leaf node");
            }
            List<RHSTree> laterChildren = new ArrayList<>();
            for (int i = 1; i < tree.size(); i++) {
                laterChildren.add(tree.getChild(i));
            }
            productions.add(new Pair<>(firstChildValue, laterChildren));
        }

        return productions;
    }

    private List<List<RHSTree>> getProductionsFollowingLC(Rule rule, String leftCornerName) {
        List<Pair<String, List<RHSTree>>> productions = getProductions(rule);

        List<List<RHSTree>> followingProductions = new ArrayList<>();
        for (Pair<String, List<RHSTree>> production : productions) {
            if (production.left.equals(leftCornerName)) {
                followingProductions.add(production.right);
            }
        }
        return followingProductions;
    }

    private void createNewGrammar() {
        newRules = new ArrayList<>();
        newRuleTokens = new HashMap<>();
        newProductions = new HashMap<>();

        for (Rule oldRule : originalGrammar.getRules()) {
            if (isRuleAffected(oldRule)) {
                // cases 1-3
                processLRRule(oldRule);
            } else {
                // case 4
                newRules.add(oldRule.copy());
            }
        }

        // turn the productions created in cases 1-3 into rules
        processProductions();

        if (originalGrammar.hasDependencies()) {
            newGrammar = new Grammar(newRules, originalGrammar.getStartRuleName(), true);
            newGrammar.setExternalRuleMaps(originalGrammar.getExternalRuleMaps());
            newGrammar.linkRules();
        } else {
            newGrammar = new Grammar(newRules, originalGrammar.getStartRuleName(), false);
        }
    }

    /*
     * for (A : retained LR identifiers):
     *      for (X : plc(A)):
     *          if X is a terminal || X is non-LR identifier:
     *              add A -> X, A-X to TG                     // 1
     *          elif X is LR identifier:
     *              for (Y,Beta : productions(X)):
     *                  add A-Y -> Beta, A-X to TG            // 2
     *          if (X in dlc(A)):
     *              for (Beta : productionsAfterLC(A,X)):
     *                  add A-X -> Beta to TG                 // 3
     * for (A : non-LR rules):
     *      add A to TG                                       // 4
     *
     */
    private void processLRRule(Rule rule) {
        String ruleName = rule.lhs().getValue();
        if (retainedLRRules.contains(ruleName)) {
            for (String plc : properLeftCorners.get(ruleName)) {
                if (allLRRules.contains(plc)) {
                    Rule plcRule = originalGrammar.getRuleMap().get(plc);
                    for (Pair<String, List<RHSTree>> production : getProductions(plcRule)) {
                        // case 2
                        Token lhs = getNewRuleToken(ruleName, production.left);
                        Token rhs = getNewRuleToken(ruleName, plc);

                        RHSTree tree = new RHSTree(RHSType.IDENTIFIER);
                        tree.createNode(rhs);

                        List<RHSTree> newProduction = production.right;
                        newProduction.add(tree);
                        addProduction(lhs, newProduction);
                    }
                } else {
                    // case 1

                    // this differs from what is described in the paper,
                    // but I think case 1 only applies if X is a direct
                    // left corner of a LR rule, not just a proper left
                    // corner of this LR rule.
                    // TODO: more testing for this
                    if (dlcOfLRRules.contains(plc)) {
                        RHSTree firstChild;
                        if (originalGrammar.isRuleName(plc)) {
                            firstChild = new RHSTree(RHSType.IDENTIFIER);
                            Token token = originalGrammar.getRuleMap().get(plc).lhs();
                            firstChild.createNode(token);
                        } else {
                            firstChild = new RHSTree(RHSType.TERMINAL);
                            Token token = Grammar.getTerminalToken(plc);
                            firstChild.createNode(token);
                        }

                        RHSTree secondChild = new RHSTree(RHSType.IDENTIFIER);
                        Token newToken = getNewRuleToken(ruleName, plc);
                        secondChild.createNode(newToken);

                        List<RHSTree> rhs = new ArrayList<>();
                        rhs.add(firstChild);
                        rhs.add(secondChild);

                        addProduction(rule.lhs(), rhs);
                    }
                }

                if (directLeftCorners.get(ruleName).contains(plc)) {
                    for (List<RHSTree> beta : getProductionsFollowingLC(rule, plc)) {
                        // case 3
                        Token lhs = getNewRuleToken(ruleName, plc);
                        if (beta.size() == 0) {
                            beta.add(getEmptyStringTree());
                        }
                        addProduction(lhs, beta);
                    }
                }
            }
        }
    }

    private Token getNewRuleToken(String ruleName, String leftCornerName) {
        FixedPair<String, String> pair = new FixedPair<>(ruleName, leftCornerName);

        if (newRuleTokens.containsKey(pair)) {
            return newRuleTokens.get(pair);
        } else {
            String identifier = ruleName + "_minus_" + leftCornerName;
            Token newToken = Grammar.getNextIdentifier(identifier, allRuleNames);
            allRuleNames.add(newToken.getValue());
            newRuleTokens.put(pair, newToken);
            return newToken;
        }
    }

    private RHSTree getEmptyStringTree() {
        RHSTree epsilon = new RHSTree(RHSType.TERMINAL);
        Token emptyString = Grammar.getEmptyStringToken();
        epsilon.createNode(emptyString);
        return epsilon;
    }

    private void addProduction(Token lhs, List<RHSTree> rhs) {
        String lhsName = lhs.getValue();
        if (newProductions.containsKey(lhsName)) {
            Pair<Token, List<List<RHSTree>>> pair = newProductions.get(lhsName);
            pair.right.add(rhs);
        } else {
            List<List<RHSTree>> alternates = new ArrayList<>();
            alternates.add(rhs);
            Pair<Token, List<List<RHSTree>>> pair = new Pair<>(lhs, alternates);
            newProductions.put(lhsName, pair);
        }
    }

    private void processProductions() {
        for (Pair<Token, List<List<RHSTree>>> pair : newProductions.values()) {
            Token lhs = pair.left;
            RHSTree rhs;
            if (pair.right.size() == 1) { // only one alternative
                List<RHSTree> terms = pair.right.get(0);
                rhs = new RHSTree(RHSType.CONCATENATION);
                for (RHSTree child : terms) {
                    rhs.addChild(child.copy());
                }
            } else {
                rhs = new RHSTree(RHSType.ALTERNATION);
                for (List<RHSTree> alternative : pair.right) {
                    RHSTree child = new RHSTree(RHSType.CONCATENATION);
                    for (RHSTree subChild : alternative) {
                        child.addChild(subChild.copy());
                    }
                    rhs.addChild(child);
                }
            }

            Rule rule = Rule.newBuilder().setLHSToken(lhs).setRHSTree(rhs).build();
            newRules.add(rule);
        }
    }

    @Override
    public Grammar getOriginalGrammar() {
        return originalGrammar;
    }

    @Override
    public boolean isGrammarAffected() {
        return allLRRules.size() > 0;
    }

    @Override
    public boolean isRuleAffected(Rule rule) {
        return allLRRules.contains(rule.lhs().getValue());
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
        if (newGrammar == null) {
            createNewGrammar();
        }
        return newGrammar;
    }

}