package com.blamedcloud.parsertongue.grammar;

import java.util.Iterator;
import java.util.Set;

import com.blamedcloud.parsertongue.smallstrings.SmallestStringIterator;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;
import com.blamedcloud.parsertongue.tokenizer.TokenizerTypeList;

// This is a naive iterator that tries each possible string in
// A* (Kleene star) until it finds the next string in the language
// so it is pretty inefficient.
public class ValidStringIterator implements Iterator<String> {

    private Grammar grammar;
    private Integer maxIterations;
    private boolean ignoreWS;

    private int numIterations;

    private boolean emptyStringValid;

    private SmallestStringIterator stringIterator;
    private Tokenizer tokenizer;

    public ValidStringIterator(Grammar grammar, Integer maxIterations, boolean ignoreWS) {
        this.grammar = grammar;
        this.maxIterations = maxIterations;
        this.ignoreWS = ignoreWS;
        setup();
    }

    public ValidStringIterator(Grammar grammar, Integer maxIterations) {
        this.grammar = grammar;
        this.maxIterations = maxIterations;
        this.ignoreWS = true;
        setup();
    }

    public ValidStringIterator(Grammar grammar, boolean ignoreWS) {
        this.grammar = grammar;
        this.maxIterations = null;
        this.ignoreWS = ignoreWS;
        setup();
    }

    public ValidStringIterator(Grammar grammar) {
        this.grammar = grammar;
        this.maxIterations = null;
        this.ignoreWS = true;
        setup();
    }

    private void setup() {
        if (!grammar.hasLinked()) {
            throw new RuntimeException("Cannot walk without linking");
        }
        WalkResult walkResult = grammar.walk();
        numIterations = 0;
        Set<String> alphabet = grammar.getAlphabet();
        if (!alphabet.contains("")) {
            alphabet.add("");
        }
        TokenizerTypeList ttlWithEmpty = TokenizerTypeList.getTTLForTerminals(alphabet);
        Tokenizer emptyTokenizer = new Tokenizer(ttlWithEmpty, ignoreWS);
        emptyTokenizer.tokenize("");
        emptyStringValid = grammar.isInLanguage(emptyTokenizer);
        alphabet.remove("");
        if (maxIterations == null && !walkResult.isInfinite) {
            // in the case of a finite language, we use an upperbound
            // of |A|^num(terminals) where |A| is the size of the alphabet
            // and num(terminals) is (kind of) the number of terminals
            // in the grammar file. This should be an upper bound, but
            // a very bad one in most cases
            maxIterations = ((int) Math.pow(alphabet.size(), walkResult.treeSize)) + 1;
        }
        stringIterator = new SmallestStringIterator(alphabet);
        TokenizerTypeList ttl = TokenizerTypeList.getTTLForTerminals(alphabet);
        tokenizer = new Tokenizer(ttl, ignoreWS);
    }

    @Override
    public boolean hasNext() {
        if (maxIterations == null) {
            return true;
        } else if (numIterations < maxIterations) {
            return true;
        }
        return false;
    }

    @Override
    public String next() {
        if (emptyStringValid) {
            emptyStringValid = false;
            numIterations++;
            return "";
        }

        boolean inLanguage = false;
        String nextString = "";
        while (!inLanguage) {
            nextString = stringIterator.next();
            tokenizer.tokenize(nextString);
            inLanguage = grammar.isInLanguage(tokenizer);
        }
        numIterations++;
        return nextString;
    }

}
