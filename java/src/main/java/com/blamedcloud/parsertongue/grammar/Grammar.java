package com.blamedcloud.parsertongue.grammar;

import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.ANNOTATION_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.END_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.IDENTIFIER_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.TERMINAL_NAME;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.blamedcloud.parsertongue.grammar.annotations.AnnotationManager;
import com.blamedcloud.parsertongue.grammar.expecterator.ParseResultExpecterator;
import com.blamedcloud.parsertongue.grammar.result.ParseResultFunction;
import com.blamedcloud.parsertongue.grammar.result.ParseResultTransformer;
import com.blamedcloud.parsertongue.smallstrings.SmallestStringIterator;
import com.blamedcloud.parsertongue.tokenizer.Token;
import com.blamedcloud.parsertongue.tokenizer.TokenType;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;
import com.blamedcloud.parsertongue.tokenizer.TokenizerException;
import com.blamedcloud.parsertongue.tokenizer.TokenizerTypeList;
import com.blamedcloud.parsertongue.utility.FixedPair;

public class Grammar {

    private List<Rule> rules;
    private Map<String, Rule> ruleMap;
    private String startRuleName;
    private Rule startRule;
    private Set<String> externalDependencyNames;
    private Map<String, Map<String, Rule>> externalRuleMaps;
    private boolean linkageDone;
    private TokenizerTypeList additionalTokenTypes;
    private AnnotationManager annotationManager;

    public static Tokenizer newTokenizer() {
        return new Tokenizer();
    }

    public static Token getEmptyStringToken() {
        return Grammar.getTerminalToken("");
    }

    public static Token getTerminalToken(String terminal) {
        TokenType terminalType = Grammar.newTokenizer().getTTL().get(TERMINAL_NAME);
        if (terminal.indexOf('"') != -1) {
            return new Token(terminal, terminalType, "'" + terminal + "'");
        } else {
            return new Token(terminal, terminalType, '"' + terminal + '"');
        }
    }

    public static Token getIdentifierToken(String identifier) {
        TokenType identifierType = Grammar.newTokenizer().getTTL().get(IDENTIFIER_NAME);
        return new Token(identifier, identifierType);
    }

    public static Builder newBuilder(File grammarFile) {
        return new Builder(grammarFile);
    }

    public static class Builder {

        private File grammarFile;
        private String startSymbol;
        private boolean lastStart;
        private boolean deferLinkage;
        private AnnotationManager annotationManager;

        public Builder(File grammarFile) {
            this.grammarFile = grammarFile;
            startSymbol = null;
            lastStart = false;
            deferLinkage = false;
            annotationManager = null;
        }

        public Builder setStartSymbol(String startSymbol) {
            this.startSymbol = startSymbol;
            return this;
        }

        public Builder setLastStart(boolean lastStart) {
            this.lastStart = lastStart;
            return this;
        }

        public Builder setDeferLinkage(boolean deferLinkage) {
            this.deferLinkage = deferLinkage;
            return this;
        }

        public Builder setAnnotationManager(AnnotationManager annotations) {
            annotationManager = annotations;
            return this;
        }

        public Grammar build() {
            return new Grammar(this);
        }
    }

    private Grammar(Builder builder) {
        initialSetup();
        annotationManager = builder.annotationManager;
        parseRules(builder);
    }

    public Grammar(List<Rule> rules, String startRuleName, boolean deferLinkage) {
        initialSetup();
        for (Rule rule : rules) {
            addRule(rule);
        }

        if (!deferLinkage) {
            linkRules();
        }

        setStart(startRuleName);
    }

    private void initialSetup() {
        rules = new ArrayList<>();
        ruleMap = new HashMap<>();
        startRuleName = null;
        externalDependencyNames = new HashSet<>();
        externalRuleMaps = new HashMap<>();
        linkageDone = false;
        additionalTokenTypes = new TokenizerTypeList();
        annotationManager = null;
    }

    private void parseRules(Builder builder) {
        String fullText;
        try {
            fullText = Files.readString(builder.grammarFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Unable to read grammar file");
        }

        Tokenizer fullTokenizer = Grammar.newTokenizer();
        try {
            fullTokenizer.tokenize(fullText);
        } catch (TokenizerException e) {
            throw new RuntimeException(e);
        }

        Token splitToken = new Token(";", fullTokenizer.getTTL().get(END_NAME));

        if (!fullTokenizer.getLastToken().isSameAs(splitToken)) {
            throw new RuntimeException("Found text after last rule! Did you forget ';'?");
        }

        List<Tokenizer> ruleTokenizers = fullTokenizer.splitTokensOn(splitToken);

        for (Tokenizer tokens : ruleTokenizers) {
            Rule rule = createRule(tokens);
            addRule(rule);
        }

        if (!builder.deferLinkage) {
            linkRules();
        }

        if (builder.startSymbol == null) {
            int index = 0;
            if (builder.lastStart) {
                index = rules.size() - 1;
            }
            startRule = rules.get(index);
            startRuleName = startRule.lhs().getValue();
        } else {
            setStart(builder.startSymbol);
        }
    }

    private Rule createRule(Tokenizer tokens) {
        Token annotationToken = new Token("@", tokens.getTTL().get(ANNOTATION_NAME));
        List<Tokenizer> splitTokenizers = tokens.splitTokensOn(annotationToken);

        if (splitTokenizers.size() < 1) {
            throw new RuntimeException("No rule tokens");
        }

        Rule rule = new Rule(splitTokenizers.get(0));
        if (splitTokenizers.size() == 2 && annotationManager != null) {
            ParseResultFunction f = annotationManager.parseAnnotation(splitTokenizers.get(1));
            rule.setTransformer(f);
        } else if (splitTokenizers.size() > 2) {
            throw new RuntimeException("More than one annotation token");
        }

        return rule;
    }

    private void addRule(Rule rule) {
        if (rule.hasDependency()) {
            String dependencyName = rule.getDependencyName();
            if (!externalDependencyNames.contains(dependencyName)) {
                externalDependencyNames.add(dependencyName);
            }
        }
        if (rule.isRegexRule()) {
            additionalTokenTypes.add(rule.getRegexTokenType());
        }
        rules.add(rule);
        ruleMap.put(rule.lhs().getValue(), rule);
    }

    public TokenizerTypeList getRegexTokenTypes() {
        return additionalTokenTypes;
    }

    public boolean hasRegexTokenTypes() {
        return additionalTokenTypes.size() > 0;
    }

    public void setExternalRuleMaps(Map<String, Map<String, Rule>> externalRuleMaps) {
        this.externalRuleMaps = externalRuleMaps;
    }

    public Map<String, Map<String, Rule>> getExternalRuleMaps() {
        return externalRuleMaps;
    }

    public Set<String> getDependencyNames() {
        return externalDependencyNames;
    }

    public AnnotationManager getAnnotationManager() {
        return annotationManager;
    }

    public boolean hasDependencies() {
        return externalDependencyNames.size() > 0;
    }

    public boolean hasLinked() {
        return linkageDone;
    }

    public void linkRules() {
        if (!linkageDone) {
            for (Rule rule : rules) {
                rule.createLinkage(ruleMap, externalRuleMaps);
            }
            linkageDone = true;
        }
    }

    public List<Rule> getRules() {
        return rules;
    }

    public Map<String, Rule> getRuleMap() {
        return ruleMap;
    }

    public boolean isRuleName(String name) {
        return ruleMap.containsKey(name);
    }

    public void setRuleTransformer(String ruleName, ParseResultFunction f) {
        if (ruleMap.containsKey(ruleName)) {
            ruleMap.get(ruleName).setTransformer(f);
        } else {
            throw new RuntimeException("No Rule with name '" + ruleName + "' exists!");
        }
    }

    public void composeRuleTransformer(String ruleName, ParseResultFunction f) {
        if (ruleMap.containsKey(ruleName)) {
            ruleMap.get(ruleName).composeTransformer(f);
        } else {
            throw new RuntimeException("No Rule with name '" + ruleName + "' exists!");
        }
    }

    public void setStart(String startSymbol) {
        if (ruleMap.containsKey(startSymbol)) {
            startRuleName = startSymbol;
            startRule = ruleMap.get(startSymbol);
        } else {
            throw new RuntimeException("No Rule with name '" + startSymbol + "' exists!");
        }
    }

    public Rule getStartRule() {
        return startRule;
    }

    public String getStartRuleName() {
        return startRuleName;
    }

    public boolean isInLanguage(Tokenizer tokens) {
        return tryParse(tokens).isValid();
    }

    // the result of walk() is a pair
    // (isInfinite, treeSize) where left
    // is true iff the language is infinite,
    // and right is an upper-bound on the
    // language size if it is not infinite.
    public FixedPair<Boolean, Integer> walk() {
        return startRule.walk();
    }

    public ParseResultTransformer tryParse(Tokenizer tokens) {
        if (!linkageDone) {
            throw new RuntimeException("Cannot try a parse without linking");
        }

        ParseResultExpecterator expecterator = startRule.getExpecterator(tokens);
        ParseResultTransformer result = null;
        while (expecterator.hasNext()) {
            Optional<ParseResultTransformer> optionalResult = expecterator.tryNext();
            if (optionalResult.isPresent()) {
                result = optionalResult.get();
                if (result.isValid()) {
                    if (tokens.size() == 0) {
                        return result;
                    } else if (tokens.isExhausted()) {
                        return result;
                    }
                }
            }
        }

        if ((result != null) && (!result.isValid() || tokens.isExhausted() || tokens.size() == 0)) {
            return result;
        }

        return new ParseResultTransformer(false, null, "Tokens not Exhausted");
    }

    public Set<String> getAlphabet() {
        return getAlphabet(true);
    }

    public Set<String> getAlphabet(boolean includeExternal) {
        if (includeExternal && !linkageDone) {
            throw new RuntimeException("Cannot get external alphabet without linking!");
        }

        Set<String> alphabet = new HashSet<>();
        for (Rule rule : rules) {
            Set<String> terminals = rule.getTerminals();
            alphabet.addAll(terminals);
        }

        if (includeExternal) {
            for (Map.Entry<String, Map<String, Rule>> outerEntry : externalRuleMaps.entrySet()) {
                for (Map.Entry<String, Rule> innerEntry : outerEntry.getValue().entrySet()) {
                    Set<String> terminals = innerEntry.getValue().getTerminals();
                    alphabet.addAll(terminals);
                }
            }
        }

        return alphabet;
    }

    // Note: This only works correctly if there are no regex rules
    public Map<String, Boolean> classifyFirstNStrings(int n) {
        Set<String> alphabet = getAlphabet();
        return classifyFirstNStrings(n, alphabet);
    }

    public Map<String, Boolean> classifyFirstNStrings(int n, Set<String> alphabet) {
        if (!linkageDone) {
            throw new RuntimeException("Cannot classify without linking!");
        }
        Map<String, Boolean> classification = new HashMap<>();
        boolean hadEmptyString = true;
        if (!alphabet.contains("")) {
            alphabet.add("");
            hadEmptyString = false;
        }
        Tokenizer tokenizer = new Tokenizer(TokenizerTypeList.getTTLForTerminals(alphabet), false);
        try {
            tokenizer.tokenize("");
        } catch (TokenizerException e) {
            throw new RuntimeException(e);
        }
        classification.put("", isInLanguage(tokenizer));
        alphabet.remove("");
        n--;
        SmallestStringIterator smallestStringIterator = new SmallestStringIterator(alphabet);
        while (n > 0) {
            String s = smallestStringIterator.next();
            try {
                tokenizer.tokenize(s);
            } catch (TokenizerException e) {
                throw new RuntimeException(e);
            }
            classification.put(s, isInLanguage(tokenizer));
            n--;
        }
        if (hadEmptyString) {
            alphabet.add("");
        }
        return classification;
    }

    public static Token getNextIdentifier(String identifier, Set<String> registeredIdentifiers) {
        Integer suffix = 1;
        String newIdentifier = identifier + "_" + suffix.toString();
        while (registeredIdentifiers.contains(newIdentifier)) {
            suffix += 1;
            newIdentifier = identifier + "_" + suffix.toString();
        }
        return new Token(newIdentifier, TokenizerTypeList.defaultGrammarTTL().get(IDENTIFIER_NAME));
    }

}
