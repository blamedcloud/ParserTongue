package com.blamedcloud.parsertongue.grammar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;


public class SugarTest {

    @Test
    public void testGrammarNoSugar() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/aToN.ebnf");
        assertFalse(grammar.hasSugar());
    }

    @Test
    public void testGrammarSugar() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/b_aStar_c.ebnf");
        assertTrue(grammar.hasSugar());
    }

    @Test
    public void testComplexGrammarSugar() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/testAllInternal.ebnf");
        assertTrue(grammar.hasSugar());
    }

    private Grammar getGrammar(String path) throws Exception {
        return Grammar.newBuilder(new File(path)).build();
    }
}