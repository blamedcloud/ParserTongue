package com.blamedcloud.parsertongue.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.junit.Test;

import com.blamedcloud.parsertongue.tokenizer.Tokenizer;
import com.blamedcloud.parsertongue.tokenizer.TokenizerTypeList;

public class GrammarTest {

    @Test
    public void testGrammar1() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/b_aStar_c.ebnf");
        TokenizerTypeList ttl = TokenizerTypeList.getTTLForAlphabet("abc");

        assertTrue(isInLanguage(grammar, "bc", ttl));
        assertTrue(isInLanguage(grammar, "bac", ttl));
        assertTrue(isInLanguage(grammar, "baac", ttl));
        assertTrue(isInLanguage(grammar, "baaac", ttl));

        assertFalse(isInLanguage(grammar, "ba", ttl));
        assertFalse(isInLanguage(grammar, "ac", ttl));
    }

    @Test
    public void testGrammarAnBn() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/aToNbToN.ebnf");
        TokenizerTypeList ttl = TokenizerTypeList.getTTLForAlphabet("ab");

        assertTrue(isInLanguage(grammar, "", ttl));
        assertTrue(isInLanguage(grammar, "ab", ttl));
        assertTrue(isInLanguage(grammar, "aabb", ttl));
        assertTrue(isInLanguage(grammar, "aaabbb", ttl));
        assertTrue(isInLanguage(grammar, "aaaabbbb", ttl));

        assertFalse(isInLanguage(grammar, "ba", ttl));
        assertFalse(isInLanguage(grammar, "aab", ttl));
        assertFalse(isInLanguage(grammar, "abb", ttl));
        assertFalse(isInLanguage(grammar, "aaabb", ttl));
    }

    @Test
    public void testGrammarAnBn2() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/aToNbToN2.ebnf");
        TokenizerTypeList ttl = TokenizerTypeList.getTTLForAlphabet("ab");

        assertTrue(isInLanguage(grammar, "", ttl));
        assertTrue(isInLanguage(grammar, "ab", ttl));
        assertTrue(isInLanguage(grammar, "aabb", ttl));
        assertTrue(isInLanguage(grammar, "aaabbb", ttl));
        assertTrue(isInLanguage(grammar, "aaaabbbb", ttl));

        assertFalse(isInLanguage(grammar, "ba", ttl));
        assertFalse(isInLanguage(grammar, "aab", ttl));
        assertFalse(isInLanguage(grammar, "abb", ttl));
        assertFalse(isInLanguage(grammar, "aaabb", ttl));
    }

    @Test
    public void testGrammarEqualABs() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/equalABs.ebnf");
        TokenizerTypeList ttl = TokenizerTypeList.getTTLForAlphabet("ab");

        assertTrue(isInLanguage(grammar, "", ttl));
        assertTrue(isInLanguage(grammar, "ab", ttl));
        assertTrue(isInLanguage(grammar, "abab", ttl));
        assertTrue(isInLanguage(grammar, "baaabb", ttl));

        assertFalse(isInLanguage(grammar, "b", ttl));
        assertFalse(isInLanguage(grammar, "aab", ttl));
        assertFalse(isInLanguage(grammar, "bab", ttl));
        assertFalse(isInLanguage(grammar, "aabab", ttl));

        Map<String, Boolean> classification = grammar.classifyFirstNStrings(50);
        for (Map.Entry<String, Boolean> entry : classification.entrySet()) {
            String string = entry.getKey();
            boolean correctClassification = countOccurences(string, "a") == countOccurences(string, "b");
            assertEquals(correctClassification, entry.getValue());
        }
    }

    @Test
    public void testGrammarMoreBs() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/moreBs.ebnf");
        TokenizerTypeList ttl = TokenizerTypeList.getTTLForAlphabet("ab");

        assertTrue(isInLanguage(grammar, "b", ttl));
        assertTrue(isInLanguage(grammar, "abb", ttl));
        assertTrue(isInLanguage(grammar, "bab", ttl));
        assertTrue(isInLanguage(grammar, "baabb", ttl));

        assertFalse(isInLanguage(grammar, "a", ttl));
        assertFalse(isInLanguage(grammar, "aab", ttl));
        assertFalse(isInLanguage(grammar, "ab", ttl));
        assertFalse(isInLanguage(grammar, "aabab", ttl));

        Map<String, Boolean> classification = grammar.classifyFirstNStrings(50);
        for (Map.Entry<String, Boolean> entry : classification.entrySet()) {
            String string = entry.getKey();
            boolean correctClassification = countOccurences(string, "a") < countOccurences(string, "b");
            assertEquals(correctClassification, entry.getValue());
        }
    }

    private int countOccurences(String string, String character) {
        return string.length() - string.replace(character, "").length();
    }

    private boolean isInLanguage(Grammar grammar, String input, TokenizerTypeList ttl) {
        Tokenizer tokens = new Tokenizer(ttl, true);
        tokens.tokenize(input);
        return grammar.isInLanguage(tokens);
    }

    private Grammar getGrammar(String path) throws Exception {
        return Grammar.newBuilder(new File(path)).build();
    }
}
