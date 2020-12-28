package com.blamedcloud.parsertongue.grammar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.blamedcloud.parsertongue.grammar.transformer.SugarTransformer;

public class SugarTest {

    @Test
    public void testGrammarNoSugar() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/aToN.ebnf");
        SugarTransformer sugar = new SugarTransformer(grammar);
        assertFalse(sugar.containsAffectedRules());
    }

    @Test
    public void testGrammarSugar() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/b_aStar_c.ebnf");
        SugarTransformer sugar = new SugarTransformer(grammar);
        assertTrue(sugar.containsAffectedRules());
    }

    @Test
    public void testComplexGrammarSugar() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/testAllInternal.ebnf");
        SugarTransformer sugar = new SugarTransformer(grammar);
        assertTrue(sugar.containsAffectedRules());
    }

    private Grammar getGrammar(String path) throws Exception {
        return Grammar.newBuilder(new File(path)).build();
    }
}
