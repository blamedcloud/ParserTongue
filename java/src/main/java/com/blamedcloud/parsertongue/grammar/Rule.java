package com.blamedcloud.parsertongue.grammar;

import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.ALTERNATION_SEP;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.CONCATENATION_SEP;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.CONTROL_END_TOKENS;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.CONTROL_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.CONTROL_SEPARATOR_TOKENS;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.CONTROL_START_TOKENS;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.DEFINE_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.EXTERNAL_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.GROUP_END;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.GROUP_START;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.IDENTIFIER_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.OPTIONAL_END;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.OPTIONAL_START;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.REGEX_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.REPEAT_END;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.REPEAT_START;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.TERMINAL_NAME;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.blamedcloud.parsertongue.grammar.expecterator.ParseResultExpecterator;
import com.blamedcloud.parsertongue.grammar.expecterator.RuleExpecterator;
import com.blamedcloud.parsertongue.grammar.result.ParseResultFunction;
import com.blamedcloud.parsertongue.grammar.result.ParseResultTransformer;
import com.blamedcloud.parsertongue.tokenizer.RegexToken;
import com.blamedcloud.parsertongue.tokenizer.Token;
import com.blamedcloud.parsertongue.tokenizer.TokenType;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;
import com.blamedcloud.parsertongue.tokenizer.TokenizerTypeList;
import com.blamedcloud.parsertongue.utility.FixedPair;

public class Rule {

    private Token lhsToken;
    private RHSTree rhsTree;
    private Tokenizer ruleTokens;
    private ParseResultFunction transformer;
    private boolean external;
    private String externalName;
    private boolean regex;
    private TokenType regexTokenType;

    private static final int MIN_TOKEN_COUNT = 3;
    private static final int EXTERNAL_RULE_SIZE = 5;
    private static final int REGEX_RULE_SIZE = 4;

    private static final ParseResultFunction DEFAULT_TRANSFORMER = ParseResultTransformer.identity;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private Token lhsToken;
        private RHSTree rhsTree;
        private ParseResultFunction transformer;
        private boolean external;
        private String externalName;
        private boolean regex;
        private TokenType regexTokenType;

        public Builder() {
            lhsToken = null;
            rhsTree = null;
            transformer = DEFAULT_TRANSFORMER;
            external = false;
            externalName = null;
            regex = false;
            regexTokenType = null;
        }

        public Builder setLHSToken(Token lhs) {
            this.lhsToken = lhs;
            return this;
        }

        public Builder setRHSTree(RHSTree tree) {
            this.rhsTree = tree;
            return this;
        }

        public Builder setTransformer(ParseResultFunction transformer) {
            this.transformer = transformer;
            return this;
        }

        public Builder setExternalName(String externalName) {
            this.externalName = externalName;
            this.external = true;
            return this;
        }

        public Builder setRegexTokenType(TokenType regexTT) {
            this.regexTokenType = regexTT;
            this.regex = true;
            this.rhsTree = new RHSTree(RHSType.REGEX);
            this.rhsTree.createRegexNode(this.regexTokenType);
            return this;
        }

        public Rule build() {
            if (this.lhsToken == null || this.rhsTree == null) {
                throw new RuntimeException("Can't create rule without lhs or rhs!");
            }
            return new Rule(this);
        }

    }

    private Rule(Builder builder) {
        lhsToken = builder.lhsToken;
        rhsTree = builder.rhsTree;
        ruleTokens = Grammar.newTokenizer();
        transformer = builder.transformer;
        external = builder.external;
        externalName = builder.externalName;
        regex = builder.regex;
        regexTokenType = builder.regexTokenType;
    }

    public Rule(Tokenizer tokens) {
        lhsToken = null;
        rhsTree = null;
        ruleTokens = tokens;
        transformer = DEFAULT_TRANSFORMER;
        external = false;
        externalName = null;
        regex = false;
        regexTokenType = null;
        parseRule();
    }

    public Rule copy() {
        Rule.Builder builder = Rule.newBuilder()
                                   .setLHSToken(lhsToken.copy())
                                   .setRHSTree(rhsTree.copy())
                                   .setTransformer(transformer);
        if (external) {
            builder.setExternalName(externalName);
        }
        if (regex) {
            builder.setRegexTokenType(regexTokenType);
        }
        return builder.build();
    }

    public static ParseResultFunction compose(ParseResultFunction outer, ParseResultFunction inner) {
        return (pr -> outer.apply(inner.apply(pr)));
    }

    public void composeTransformer(ParseResultFunction f) {
        transformer = Rule.compose(f, transformer);
    }

    public void setTransformer(ParseResultFunction f) {
        transformer = f;
    }

    public ParseResultFunction getTransformer() {
        return transformer;
    }

    public boolean hasDependency() {
        return external;
    }

    public String getDependencyName() {
        return externalName;
    }

    public boolean isRegexRule() {
        return regex;
    }

    public TokenType getRegexTokenType() {
        return regexTokenType;
    }

    public ParseResultExpecterator getExpecterator(Tokenizer tokens) {
        return new RuleExpecterator(this, tokens);
    }

    public FixedPair<Boolean, Integer> walk() {
        return walk(new HashSet<>());
    }

    public FixedPair<Boolean, Integer> walk(Set<String> previousIdentifiers) {
        if ((previousIdentifiers.size() > 0) && (previousIdentifiers.contains(lhsToken.getValue()))) {
            // if there is a recursive loop of identifiers,
            // we assume the language generated is infinite
            // this might not be the case, but it *should*
            // be fine for non-pathalogical examples.
            // For example, it probably runs forever on a
            // left-recursive rule (which we can't parse anyway)
            return new FixedPair<>(true, -1);
        } else {
            previousIdentifiers.add(lhsToken.getValue());
            return rhsTree.walkTree(previousIdentifiers);
        }
    }

    public Set<String> getTerminals() {
        return rhsTree.nonLinkedTerminals();
    }

    public Token lhs() {
        return lhsToken;
    }

    public RHSTree rhs() {
        return rhsTree;
    }

    public TokenType getTTByName(String name) {
        return ruleTokens.getTTL().get(name);
    }

    @Override
    public String toString() {
        return lhsToken.getValue() + ":\n" + rhsTree.toString();
    }

    public void createLinkage(Map<String, Rule> ruleMap, Map<String, Map<String, Rule>> externalRuleMaps) {
        if (!external) {
            rhsTree.addLinkage(ruleMap);
        } else {
            if (externalRuleMaps.containsKey(externalName)) {
                rhsTree.addLinkage(externalRuleMaps.get(externalName));
            } else {
                throw new RuntimeException("No external rule dict by the name of: " + externalName);
            }
        }
    }

    private TokenType currentTokenType() {
        return ruleTokens.currentToken().getType();
    }

    private boolean currentTokenTypeIs(String name) {
        return currentTokenType().isSameAs(getTTByName(name));
    }

    private void parseRule() {
        if (ruleTokens.size() < MIN_TOKEN_COUNT) {
            throw new RuntimeException("Rule has too few tokens");
        }

        if (currentTokenTypeIs(IDENTIFIER_NAME)) {
            lhsToken = ruleTokens.currentToken();
            ruleTokens.nextToken();
        } else {
            throw new RuntimeException("LHS is not an identifier");
        }

        if (currentTokenTypeIs(DEFINE_NAME)) {
            ruleTokens.nextToken();
        } else {
            throw new RuntimeException("Rule has no '='");
        }

        String tmpExternalName = null;
        int index = ruleTokens.getIndex();
        boolean exhausted = ruleTokens.isExhausted();

        // check if this is an external rule
        if (ruleTokens.size() == EXTERNAL_RULE_SIZE) {
            if (currentTokenTypeIs(IDENTIFIER_NAME)) {
                tmpExternalName = ruleTokens.currentToken().getValue();
                ruleTokens.nextToken();
                if (currentTokenTypeIs(EXTERNAL_NAME)) {
                    ruleTokens.nextToken();
                    if (currentTokenTypeIs(IDENTIFIER_NAME)) {
                        external = true;
                        externalName = tmpExternalName;
                        rhsTree = new RHSTree(RHSType.IDENTIFIER);
                        rhsTree.createNode(ruleTokens.currentToken());
                        ruleTokens.nextToken();
                    }
                }
            }
        }

        // since EXTERNAL_RULE_SIZE != REGEX_RULE_SIZE, we don't
        // need to worry about resetting ruleTokens right here.

        // check if this is a regex rule
        if (ruleTokens.size() == REGEX_RULE_SIZE) {
            if (currentTokenTypeIs(REGEX_NAME)) {
                ruleTokens.nextToken();
                if (currentTokenTypeIs(TERMINAL_NAME)) {
                    regex = true;
                    regexTokenType = new RegexToken(lhsToken.getValue(), ruleTokens.currentToken().getValue());
                    rhsTree = new RHSTree(RHSType.REGEX);
                    rhsTree.createRegexNode(regexTokenType);
                    ruleTokens.nextToken();
                }
            }
        }

        // if neither external nor regex, reset ruleTokens and parse normally
        if (!external && !regex) {
            ruleTokens.setIndex(index, exhausted);
            rhsTree = parseRHS();
        }

        if (!ruleTokens.isExhausted()) {
            throw new RuntimeException("Didn't exhaust all tokens parsing rule for: " + lhsToken.getValue());
        }
    }

    private RHSTree parseRHS() {
        RHSTree tree = null;
        TokenType currentType = currentTokenType();
        String currentTokenValue = ruleTokens.currentToken().getValue();

        if (currentType.isSameAs(getTTByName(IDENTIFIER_NAME)) || currentType.isSameAs(getTTByName(TERMINAL_NAME))) {
            tree = new RHSTree(getRHSLeafTypeFromTokenType(currentType, ruleTokens.getTTL()));
            tree.createNode(ruleTokens.currentToken());
            if (ruleTokens.nextToken()) {
                return parseRHS(tree);
            } else {
                return tree;
            }
        } else if (currentType.isSameAs(getTTByName(CONTROL_NAME))) {
            if (CONTROL_START_TOKENS.contains(currentTokenValue)) {
                if (ruleTokens.nextToken()) {
                    tree = new RHSTree(getRHSSingleTypeFromStartSymbol(currentTokenValue));
                    tree.addChild(parseRHS());

                    String newTokenValue = ruleTokens.currentToken().getValue();
                    TokenType newTokenType = currentTokenType();
                    String expectedTokenValue = getMatchingControlBlock(currentTokenValue);
                    if (newTokenType.isSameAs(getTTByName(CONTROL_NAME)) && newTokenValue.equals(expectedTokenValue)) {
                        if (ruleTokens.nextToken()) {
                            return parseRHS(tree);
                        } else {
                            return tree;
                        }
                    } else {
                        throw new RuntimeException("ERROR: found: '" + newTokenValue + "', expecting: '" + expectedTokenValue + "'");
                    }
                } else {
                    throw new RuntimeException("ERROR: No matching end-symbol matching: '" + currentTokenValue + "'");
                }
            } else {
                throw new RuntimeException("ERROR: got token: '" + currentTokenValue + "' without working tree");
            }
        } else {
            throw new RuntimeException("ERROR: got token: '" + currentTokenValue + "' of type: " + currentType.toString());
        }
    }

    private RHSTree parseRHS(RHSTree workingTree) {
        if (workingTree == null) {
            throw new RuntimeException("ERROR: workingTree was null");
        }

        RHSTree tree = null;
        TokenType currentType = currentTokenType();
        String currentTokenValue = ruleTokens.currentToken().getValue();

        RHSKind workingKind = workingTree.getKind();

        if (workingKind == RHSKind.LEAF || workingKind == RHSKind.SINGLE) {
            if (currentType.isSameAs(getTTByName(CONTROL_NAME))) {
                if (CONTROL_SEPARATOR_TOKENS.contains(currentTokenValue)) {
                    tree = new RHSTree(getRHSListTypeFromSepToken(currentTokenValue));
                    tree.addChild(workingTree);
                    if (ruleTokens.nextToken()) {
                        tree.addChild(parseRHSNonList());
                        if (ruleTokens.isExhausted()) {
                            return tree;
                        } else {
                            return parseRHS(tree);
                        }
                    } else {
                        throw new RuntimeException("ERROR expected rhs subtree after control token: '" + currentTokenValue + "'");
                    }
                } else if (CONTROL_END_TOKENS.contains(currentTokenValue)) {
                    return workingTree;
                } else {
                    throw new RuntimeException("ERROR got bad control token: '" + currentTokenValue + "' after working tree of kind: " + workingKind);
                }
            } else {
                throw new RuntimeException("ERROR got non-control token: '" + currentTokenValue + "' after working tree of kind: " + workingKind);
            }
        } else if (workingKind == RHSKind.LIST) {
            if (currentType.isSameAs(getTTByName(CONTROL_NAME))) {
                if (CONTROL_SEPARATOR_TOKENS.contains(currentTokenValue)) {
                    if (getRHSListTypeFromSepToken(currentTokenValue) == workingTree.getType()) {
                        if (ruleTokens.nextToken()) {
                            workingTree.addChild(parseRHSNonList());
                            if (ruleTokens.isExhausted()) {
                                return workingTree;
                            } else {
                                return parseRHS(workingTree);
                            }
                        } else {
                            throw new RuntimeException("ERROR expected rhs subtree after control token: '" + currentTokenValue + "'");
                        }
                    } else {
                        tree = new RHSTree(getRHSListTypeFromSepToken(currentTokenValue));
                        if (currentTokenValue.equals(ALTERNATION_SEP)) {
                            tree.addChild(workingTree);
                            if (ruleTokens.nextToken()) {
                                tree.addChild(parseRHSNonList());
                                if (ruleTokens.isExhausted()) {
                                    return tree;
                                } else {
                                    return parseRHS(tree);
                                }
                            } else {
                                throw new RuntimeException("ERROR expected rhs subtree after control token: '" + currentTokenValue + "'");
                            }
                        } else {
                            RHSTree rightChild = workingTree.popRightChild();
                            tree.addChild(rightChild);
                            while (!ruleTokens.isExhausted() && currentTokenIsConcatenation()) {
                                if (ruleTokens.nextToken()) {
                                    tree.addChild(parseRHSNonList());
                                } else {
                                    throw new RuntimeException("ERROR expected rhs subtree after control token: '" + currentTokenValue + "'");
                                }
                            }
                            workingTree.addChild(tree);
                            if (ruleTokens.isExhausted()) {
                                return workingTree;
                            } else {
                                return parseRHS(workingTree);
                            }
                        }
                    }
                } else if (CONTROL_END_TOKENS.contains(currentTokenValue)) {
                    return workingTree;
                } else {
                    throw new RuntimeException("ERROR got bad control token: '" + currentTokenValue + "' after working tree of kind: " + workingKind);
                }
            } else {
                throw new RuntimeException("ERROR: got token: '" + currentTokenValue + "' of type: " + currentType.toString());
            }
        } else {
            throw new RuntimeException("ERROR: got unknown tree kind: " + workingKind);
        }
    }

    private boolean currentTokenIsConcatenation() {
        Token concatenationSepToken = new Token(CONCATENATION_SEP, getTTByName(CONTROL_NAME));
        return ruleTokens.currentToken().isSameAs(concatenationSepToken);
    }

    private RHSTree parseRHSNonList() {
        RHSTree tree = null;
        TokenType currentType = currentTokenType();
        String currentTokenValue = ruleTokens.currentToken().getValue();

        if (currentType.isSameAs(getTTByName(IDENTIFIER_NAME)) || currentType.isSameAs(getTTByName(TERMINAL_NAME))) {
            tree = new RHSTree(getRHSLeafTypeFromTokenType(currentType, ruleTokens.getTTL()));
            tree.createNode(ruleTokens.currentToken());
            ruleTokens.nextToken();
            return tree;
        } else if (currentType.isSameAs(getTTByName(CONTROL_NAME))) {
            if (CONTROL_START_TOKENS.contains(currentTokenValue)) {
                if (ruleTokens.nextToken()) {
                    tree = new RHSTree(getRHSSingleTypeFromStartSymbol(currentTokenValue));
                    tree.addChild(parseRHS());
                    String newTokenValue = ruleTokens.currentToken().getValue();
                    TokenType newTokenType = currentTokenType();
                    String expectedTokenValue = getMatchingControlBlock(currentTokenValue);
                    if (newTokenType.isSameAs(getTTByName(CONTROL_NAME)) && newTokenValue.equals(expectedTokenValue)) {
                        ruleTokens.nextToken();
                        return tree;
                    } else {
                        throw new RuntimeException("ERROR: found: '" + newTokenValue + "', expecting: '" + expectedTokenValue + "'");
                    }
                } else {
                    throw new RuntimeException("ERROR: No matching end-symbol matching: '" + currentTokenValue + "'");
                }
            } else {
                throw new RuntimeException("ERROR: got token: '" + currentTokenValue + "' without working tree");
            }
        } else {
            throw new RuntimeException("ERROR: got token: '" + currentTokenValue + "' of type: " + currentType.toString());
        }
    }

    private static RHSType getRHSLeafTypeFromTokenType(TokenType tt, TokenizerTypeList ttl) {
        if (tt.isSameAs(ttl.get(IDENTIFIER_NAME))) {
            return RHSType.IDENTIFIER;
        } else if (tt.isSameAs(ttl.get(TERMINAL_NAME))) {
            return RHSType.TERMINAL;
        } else {
            throw new RuntimeException("getRHSLeafTypeFromTokenType: ERROR invalid tokentype");
        }
    }

    private static String getMatchingControlBlock(String startSymbol) {
        if (startSymbol.equals(GROUP_START)) {
            return GROUP_END;
        } else if (startSymbol.equals(REPEAT_START)) {
            return REPEAT_END;
        } else if (startSymbol.equals(OPTIONAL_START)) {
            return OPTIONAL_END;
        } else {
            throw new RuntimeException("Unkown control block start symbol: " + startSymbol);
        }
    }

    private static RHSType getRHSSingleTypeFromStartSymbol(String startSymbol) {
        if (startSymbol.equals(GROUP_START)) {
            return RHSType.GROUP;
        } else if (startSymbol.equals(REPEAT_START)) {
            return RHSType.REPEAT;
        } else if (startSymbol.equals(OPTIONAL_START)) {
            return RHSType.OPTIONAL;
        } else {
            throw new RuntimeException("Unkonwn single type start symbol: " + startSymbol);
        }
    }

    private static RHSType getRHSListTypeFromSepToken(String sepSymbol) {
        if (sepSymbol.equals(CONCATENATION_SEP)) {
            return RHSType.CONCATENATION;
        } else if (sepSymbol.equals(ALTERNATION_SEP)) {
            return RHSType.ALTERNATION;
        } else {
            throw new RuntimeException("Unknown list type separator: " + sepSymbol);
        }
    }

}
