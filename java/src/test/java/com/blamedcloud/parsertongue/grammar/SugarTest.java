package com.blamedcloud.parsertongue.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.junit.Test;

import com.blamedcloud.parsertongue.grammar.transformer.SugarTransformer;

public class SugarTest {

    private static final int TEST_ITERATIONS = 200;

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

        Grammar newGrammar = sugar.getTransformedGrammar();
        SugarTransformer newSugar = new SugarTransformer(newGrammar);
        assertFalse(newSugar.containsAffectedRules());

        compareGrammars(grammar, newGrammar);
    }

    @Test
    public void testComplexGrammarSugar() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/testAllInternal.ebnf");
        SugarTransformer sugar = new SugarTransformer(grammar);
        assertTrue(sugar.containsAffectedRules());

        Grammar newGrammar = sugar.getTransformedGrammar();
        SugarTransformer newSugar = new SugarTransformer(newGrammar);
        assertFalse(newSugar.containsAffectedRules());

        compareGrammars(grammar, newGrammar);
    }

    private Grammar getGrammar(String path) throws Exception {
        return Grammar.newBuilder(new File(path)).build();
    }

    private void compareGrammars(Grammar oldGrammar, Grammar newGrammar) throws Exception {
        Map<String, Boolean> oldClassification = oldGrammar.classifyFirstNStrings(TEST_ITERATIONS);
        Map<String, Boolean> newClassification = newGrammar.classifyFirstNStrings(TEST_ITERATIONS);
        assertEquals("The classifications should be the same", oldClassification, newClassification);
    }
}
