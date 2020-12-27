package com.blamedcloud.parsertongue.grammar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class LeftRecursiveTest {

    @Test
    public void testSimpleDirect() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/leftRecursive/simpleDirect.ebnf");
        assertTrue(grammar.containsLeftRecursion());

        Rule startRule = grammar.getStartRule();
        assertTrue(startRule.hasDirectLeftRecursion());
        assertFalse(startRule.hasIndirectLeftRecursion());
    }

    @Test
    public void testComplexDirect() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/leftRecursive/complexDirect.ebnf");
        assertTrue(grammar.containsLeftRecursion());

        Rule startRule = grammar.getStartRule();
        assertTrue(startRule.hasDirectLeftRecursion());
        assertFalse(startRule.hasIndirectLeftRecursion());
    }

    @Test
    public void testsimpleIndirect() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/leftRecursive/simpleIndirect.ebnf");
        assertTrue(grammar.containsLeftRecursion());

        Rule startRule = grammar.getStartRule();
        assertFalse(startRule.hasDirectLeftRecursion());
        assertTrue(startRule.hasIndirectLeftRecursion());
    }

    @Test
    public void testComplexIndirect() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/leftRecursive/complexIndirect.ebnf");
        assertTrue(grammar.containsLeftRecursion());

        Rule startRule = grammar.getStartRule();
        assertFalse(startRule.hasDirectLeftRecursion());
        assertTrue(startRule.hasIndirectLeftRecursion());
    }

    private Grammar getGrammar(String path) throws Exception {
        return Grammar.newBuilder(new File(path)).build();
    }

}