package com.blamedcloud.parsertongue.grammar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.blamedcloud.parsertongue.grammar.transformer.LeftRecursionTransformer;

public class LeftRecursiveTest {

    @Test
    public void testSimpleDirect() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/leftRecursive/simpleDirect.ebnf");
        LeftRecursionTransformer lrTransformer = new LeftRecursionTransformer(grammar);
        assertTrue(lrTransformer.isGrammarAffected());

        Rule startRule = grammar.getStartRule();
        assertTrue(lrTransformer.isRuleAffected(startRule));
        assertTrue(lrTransformer.hasDirectLR(startRule));
        assertFalse(lrTransformer.hasIndirectLR(startRule));
    }

    @Test
    public void testComplexDirect() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/leftRecursive/complexDirect.ebnf");
        LeftRecursionTransformer lrTransformer = new LeftRecursionTransformer(grammar);
        assertTrue(lrTransformer.isGrammarAffected());

        Rule startRule = grammar.getStartRule();
        assertTrue(lrTransformer.isRuleAffected(startRule));
        assertTrue(lrTransformer.hasDirectLR(startRule));
        assertFalse(lrTransformer.hasIndirectLR(startRule));
    }

    @Test
    public void testsimpleIndirect() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/leftRecursive/simpleIndirect.ebnf");
        LeftRecursionTransformer lrTransformer = new LeftRecursionTransformer(grammar);
        assertTrue(lrTransformer.isGrammarAffected());

        Rule startRule = grammar.getStartRule();
        assertTrue(lrTransformer.isRuleAffected(startRule));
        assertFalse(lrTransformer.hasDirectLR(startRule));
        assertTrue(lrTransformer.hasIndirectLR(startRule));
    }

    @Test
    public void testComplexIndirect() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/leftRecursive/complexIndirect.ebnf");
        LeftRecursionTransformer lrTransformer = new LeftRecursionTransformer(grammar);
        assertTrue(lrTransformer.isGrammarAffected());

        Rule startRule = grammar.getStartRule();
        assertTrue(lrTransformer.isRuleAffected(startRule));
        assertFalse(lrTransformer.hasDirectLR(startRule));
        assertTrue(lrTransformer.hasIndirectLR(startRule));
    }

    private Grammar getGrammar(String path) throws Exception {
        return Grammar.newBuilder(new File(path)).build();
    }

}