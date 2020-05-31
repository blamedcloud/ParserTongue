package com.blamedcloud.parsertongue.tokenizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AlphabetTokenizerTest {

    @Test
    public void alphabetTTLTest() {
        TokenizerTypeList alphabetTTL = TokenizerTypeList.getTTLForAlphabet("abc");
        assertEquals(3, alphabetTTL.size());
        assertTrue(alphabetTTL.contains("a"));
        assertTrue(alphabetTTL.contains("b"));
        assertTrue(alphabetTTL.contains("c"));
    }

    @Test
    public void alphabetTokenizerTestNoWS() {
        TokenizerTypeList alphabetTTL = TokenizerTypeList.getTTLForAlphabet("abc");
        Tokenizer tokenizer = new Tokenizer(alphabetTTL, false);
        String fullText = "aababcc";
        tokenizer.tokenize(fullText);
        assertEquals(fullText.length(), tokenizer.size());
    }

    @Test
    public void alphabetTokenizerTestWS() {
        TokenizerTypeList alphabetTTL = TokenizerTypeList.getTTLForAlphabet("abcd");
        Tokenizer tokenizer = new Tokenizer(alphabetTTL, true);
        String fullText = "a ab  a\nb\t \nc c";
        tokenizer.tokenize(fullText);
        assertEquals(7, tokenizer.size());
    }

}
