package com.blamedcloud.parsertongue.grammar;

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
import java.util.function.Function;

import com.blamedcloud.parsertongue.grammar.expecterator.ParseResultExpecterator;
import com.blamedcloud.parsertongue.grammar.result.ParseResult;
import com.blamedcloud.parsertongue.grammar.result.ParseResultTransformer;
import com.blamedcloud.parsertongue.smallstrings.SmallestStringIterator;
import com.blamedcloud.parsertongue.tokenizer.Token;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;
import com.blamedcloud.parsertongue.tokenizer.TokenizerTypeList;

public class Grammar {

    private List<Rule> rules;
    private Map<String, Rule> ruleMap;
    private String startRuleName;
    private Rule startRule;
    private Set<String> externalDependencyNames;
    private Map<String, Map<String, Rule>> externalRuleMaps;
    private boolean linkageDone;
    private TokenizerTypeList additionalTokenTypes;

    public static Tokenizer newTokenizer() {
        return new Tokenizer();
    }

    public static Token getEmptyStringToken() {
        return new Token("", Grammar.newTokenizer().getTTL().get(TERMINAL_NAME), "''");
    }

    public static Builder newBuilder(File grammarFile) {
        return new Builder(grammarFile);
    }

    public static class Builder {

        private File grammarFile;
        private String startSymbol;
        private boolean lastStart;
        private boolean deferLinkage;

        public Builder(File grammarFile) {
            this.grammarFile = grammarFile;
            startSymbol = null;
            lastStart = false;
            deferLinkage = false;
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

        public Grammar build() {
            return new Grammar(this);
        }
    }

    private Grammar(Builder builder) {
        initialSetup();
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
    }

    private void parseRules(Builder builder) {
        String fullText;
        try {
            fullText = Files.readString(builder.grammarFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Unable to read grammar file");
        }

        Tokenizer fullTokenizer = Grammar.newTokenizer();
        fullTokenizer.tokenize(fullText);

        Token splitToken = new Token(";", fullTokenizer.getTTL().get(END_NAME));

        if (!fullTokenizer.getLastToken().isSameAs(splitToken)) {
            throw new RuntimeException("Found text after last rule! Did you forget ';'?");
        }

        List<Tokenizer> ruleTokenizers = fullTokenizer.splitTokensOn(splitToken);

        for (Tokenizer tokens : ruleTokenizers) {
            Rule rule = new Rule(tokens);
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

    public void setRuleTransformer(String ruleName, Function<ParseResult, ParseResult> f) {
        if (ruleMap.containsKey(ruleName)) {
            ruleMap.get(ruleName).setTransformer(f);
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

    public WalkResult walk() {
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
        if (!linkageDone) {
            throw new RuntimeException("Cannot classify without linking!");
        }
        Map<String, Boolean> classification = new HashMap<>();

        Set<String> alphabet = getAlphabet();
        if (!alphabet.contains("")) {
            alphabet.add("");
        }
        Tokenizer tokenizer = new Tokenizer(TokenizerTypeList.getTTLForTerminals(alphabet), false);
        tokenizer.tokenize("");
        classification.put("", isInLanguage(tokenizer));
        alphabet.remove("");
        n--;
        SmallestStringIterator smallestStringIterator = new SmallestStringIterator(alphabet);
        while (n > 0) {
            String s = smallestStringIterator.next();
            tokenizer.tokenize(s);
            classification.put(s, isInLanguage(tokenizer));
            n--;
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
