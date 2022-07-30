package com.blamedcloud.parsertongue.tokenizer;

import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.COMMENT_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.CONTROL_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.DEFINE_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.END_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.EXTERNAL_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.IDENTIFIER_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.REGEX_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.TERMINAL_NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TokenizerTypeList implements Iterable<TokenType> {

    private List<TokenType> typeList;
    private Map<String, Integer> indexLookup;

    public TokenizerTypeList() {
        typeList = new ArrayList<>();
        indexLookup = new HashMap<>();
    }

    public int size() {
        return typeList.size();
    }

    public void add(TokenType tt) {
        if (!indexLookup.containsKey(tt.getName())) {
            indexLookup.put(tt.getName(), typeList.size());
            typeList.add(tt);
        }
    }

    public void extend(TokenizerTypeList ttl) {
        for (TokenType tt : ttl) {
            add(tt);
        }
    }

    public boolean contains(TokenType tt) {
        return indexLookup.containsKey(tt.getName());
    }

    public boolean contains(String name) {
        return indexLookup.containsKey(name);
    }

    // doesn't check if the tt is actually contained first
    // use contains() for that if you need to
    public Integer indexOf(TokenType tt) {
        return indexLookup.get(tt.getName());
    }

    public Integer indexOf(String name) {
        return indexLookup.get(name);
    }

    // doesn't check for inclusion nor index bounds
    public TokenType get(int index) {
        return typeList.get(index);
    }

    public TokenType get(TokenType tt) {
        return typeList.get(indexOf(tt));
    }

    public TokenType get(String name) {
        return typeList.get(indexOf(name));
    }

    @Override
    public Iterator<TokenType> iterator() {
        return typeList.iterator();
    }

    public static TokenizerTypeList defaultGrammarTTL() {
        TokenizerTypeList grammarTTL = new TokenizerTypeList();
        grammarTTL.add(new ExactToken(END_NAME, ";"));
        grammarTTL.add(new ExactToken(DEFINE_NAME, "="));
        grammarTTL.add(new ExactToken(EXTERNAL_NAME, ":"));
        grammarTTL.add(new ExactToken(REGEX_NAME, "~"));
        grammarTTL.add(new RegexToken(CONTROL_NAME, "[()\\[\\]{}|,]"));
        grammarTTL.add(new RegexToken(IDENTIFIER_NAME, "[a-zA-Z][a-zA-Z0-9_]*"));
        grammarTTL.add(new RegexToken(TERMINAL_NAME, "'([^']*)'|\"([^\"]*)\""));
        grammarTTL.add(new RegexToken(COMMENT_NAME, "(#.*\\n)|(#.*\\Z)", true));
        return grammarTTL;
    }

    public static TokenizerTypeList getTTLForAlphabet(String alphabet) {
        TokenizerTypeList alphabetTTL = new TokenizerTypeList();
        for (int i = 0; i < alphabet.length(); i++) {
            char c = alphabet.charAt(i);
            String letter = Character.toString(c);
            alphabetTTL.add(new ExactToken(letter, letter));
        }
        return alphabetTTL;
    }

    public static TokenizerTypeList getTTLForTerminals(Collection<String> terminals) {
        TokenizerTypeList terminalsTTL = new TokenizerTypeList();
        for (String terminal : terminals) {
            terminalsTTL.add(new ExactToken(terminal, terminal));
        }
        return terminalsTTL;
    }

}
