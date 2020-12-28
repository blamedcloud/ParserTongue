package com.blamedcloud.parsertongue.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.blamedcloud.parsertongue.grammar.Grammar;
import com.blamedcloud.parsertongue.grammar.dependencies.DependencyManager;
import com.blamedcloud.parsertongue.grammar.result.ParseResult;
import com.blamedcloud.parsertongue.grammar.result.ParseResultTransformer;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;
import com.blamedcloud.parsertongue.tokenizer.TokenizerTypeList;

public class Parser {

    private Grammar grammar;
    private Set<String> alphabet;
    private TokenizerTypeList ttl;
    private boolean ignoreWhiteSpaceDefault;

    public static Builder newBuilder(File grammarFile) {
        return new Builder(grammarFile);
    }

    public static class Builder {

        private File grammarFile;
        private String startSymbol;
        private List<File> dependentGrammarFiles;
        private boolean ignoreWhiteSpaceDefault;

        public Builder(File grammarFile) {
            this.grammarFile = grammarFile;
            startSymbol = null;
            dependentGrammarFiles = null;
            ignoreWhiteSpaceDefault = false;
        }

        public Builder setStartSymbol(String startSymbol) {
            this.startSymbol = startSymbol;
            return this;
        }

        public Builder setDependentGrammarFiles(List<File> files) {
            dependentGrammarFiles = files;
            return this;
        }

        public Builder setIgnoreWhiteSpaceDefault(boolean ignoreWS) {
            ignoreWhiteSpaceDefault = ignoreWS;
            return this;
        }

        public Parser build() {
            return new Parser(this);
        }

    }

    private Parser(Builder builder) {
        alphabet = new HashSet<>();
        ttl = null;
        ignoreWhiteSpaceDefault = builder.ignoreWhiteSpaceDefault;
        Grammar.Builder grammarBuilder = Grammar.newBuilder(builder.grammarFile).setDeferLinkage(true);
        if (builder.startSymbol != null) {
            grammarBuilder.setStartSymbol(builder.startSymbol);
        }
        grammar = grammarBuilder.build();
        if (grammar.hasDependencies()) {
            if (builder.dependentGrammarFiles == null || builder.dependentGrammarFiles.size() == 0) {
                throw new RuntimeException("Must supply dependent grammar files for grammar with dependencies");
            }
            String grammarName = DependencyManager.grammarFileToName(builder.grammarFile);
            DependencyManager manager = new DependencyManager(grammarName, grammar, builder.dependentGrammarFiles);
            manager.manage();
        } else {
            grammar.setExternalRuleMaps(new HashMap<>());
            grammar.linkRules();
        }
        if (!grammar.hasLinked()) {
            throw new RuntimeException("Grammar was never linked");
        }
        setGrammarAlphabet();
    }

    public void setGrammarAlphabet() {
        alphabet = grammar.getAlphabet();
        TokenizerTypeList alphabetTTL = TokenizerTypeList.getTTLForTerminals(alphabet);
        if (grammar.hasRegexTokenTypes()) {
            ttl = grammar.getRegexTokenTypes();
            ttl.extend(alphabetTTL);
        } else {
            ttl = alphabetTTL;
        }
    }

    public void setAlphabet(Set<String> alphabet) {
        this.alphabet = alphabet;
        ttl = TokenizerTypeList.getTTLForTerminals(alphabet);
    }

    public void setTTL(TokenizerTypeList ttl) {
        this.ttl = ttl;
    }

    public void setRuleTransform(String ruleName, Function<ParseResult, ParseResult> f) {
        grammar.setRuleTransformer(ruleName, f);
    }

    public Grammar getGrammar() {
        return grammar;
    }

    public void setIgnoreWSDefault(boolean ignoreWS) {
        ignoreWhiteSpaceDefault = ignoreWS;
    }

    public boolean getIgnoreWSDefault() {
        return ignoreWhiteSpaceDefault;
    }

    public ParseResultTransformer parseFile(File parseFile) {
        return parseFile(parseFile, ignoreWhiteSpaceDefault);
    }

    public ParseResultTransformer parseFile(File parseFile, boolean ignoreWhiteSpace) {
        String parseString;
        try {
            parseString = Files.readString(parseFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Unable to read parse file");
        }
        return parseString(parseString, ignoreWhiteSpace);
    }

    public ParseResultTransformer parseString(String parseString) {
        return parseString(parseString, ignoreWhiteSpaceDefault);
    }

    public ParseResultTransformer parseString(String parseString, boolean ingoreWhiteSpace) {
        Tokenizer tokens = new Tokenizer(ttl, ingoreWhiteSpace);
        tokens.tokenize(parseString);
        return grammar.tryParse(tokens);
    }

    public boolean checkFile(File parseFile) {
        return checkFile(parseFile, ignoreWhiteSpaceDefault);
    }

    public boolean checkFile(File parseFile, boolean ignoreWhiteSpace) {
        return parseFile(parseFile, ignoreWhiteSpace).isValid();
    }

    public boolean checkString(String parseString) {
        return checkString(parseString, ignoreWhiteSpaceDefault);
    }

    public boolean checkString(String parseString, boolean ignoreWhiteSpace) {
        return parseString(parseString, ignoreWhiteSpace).isValid();
    }

}
