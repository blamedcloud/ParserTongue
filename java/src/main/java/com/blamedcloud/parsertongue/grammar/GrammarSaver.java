package com.blamedcloud.parsertongue.grammar;

public class GrammarSaver {

    public static final String DEFAULT_RULE_SPLIT = "\n";
    public static final String DEFAULT_TOKEN_SPLIT = " ";

    public static String saveGrammar(Grammar grammar) {
        String grammarString = "";

        grammarString += saveRule(grammar.getStartRule());

        for (Rule rule : grammar.getRules()) {
            if (!rule.lhs().getValue().equals(grammar.getStartRuleName())) {
                grammarString += DEFAULT_RULE_SPLIT + saveRule(rule);
            }
        }

        return grammarString;
    }

    public static String saveRule(Rule rule) {
        String ruleString = rule.lhs().getValue() + DEFAULT_TOKEN_SPLIT + '=' + DEFAULT_TOKEN_SPLIT;
        if (rule.hasDependency()) {
            ruleString += rule.getDependencyName() + DEFAULT_TOKEN_SPLIT + ":" + DEFAULT_TOKEN_SPLIT;
        } else if (rule.isRegexRule()) {
            ruleString += "~" + DEFAULT_TOKEN_SPLIT;
        }
        ruleString += saveRHS(rule.rhs());
        ruleString += DEFAULT_TOKEN_SPLIT + ";" + DEFAULT_RULE_SPLIT;

        return ruleString;
    }

    public static String saveRHS(RHSTree tree) {
        String treeString = "";

        RHSType type = tree.getType();
        if (type == RHSType.IDENTIFIER) {
            treeString = tree.getNode().getValue();
        } else if (type == RHSType.TERMINAL) {
            treeString = '"' + tree.getNode().getValue() + '"';
        } else if (type == RHSType.REGEX) {
            treeString = '"' + tree.getRegexNode().getExpression() + '"';
        } else if (type == RHSType.GROUP) {
            treeString = "(" + saveRHS(tree.getChild()) + ")";
        } else if (type == RHSType.OPTIONAL) {
            treeString = "[" + saveRHS(tree.getChild()) + "]";
        } else if (type == RHSType.REPEAT) {
            treeString = "{" + saveRHS(tree.getChild()) + "}";
        } else if (type == RHSType.ALTERNATION || type == RHSType.CONCATENATION) {
            String separator = DEFAULT_TOKEN_SPLIT + (type == RHSType.ALTERNATION ? "|" : ",") + DEFAULT_TOKEN_SPLIT;
            // iterate over all but the last child
            for (int i = 0; i < tree.size() - 1; i++) {
                RHSTree child = tree.getChild(i);
                treeString += saveRHS(child) + separator;
            }
            treeString += saveRHS(tree.getChild(tree.size()-1));
        }

        return treeString;
    }
}
