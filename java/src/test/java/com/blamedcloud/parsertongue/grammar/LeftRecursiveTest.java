package com.blamedcloud.parsertongue.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.blamedcloud.parsertongue.grammar.transformer.LeftRecursionTransformer;
import com.blamedcloud.parsertongue.tokenizer.Token;
import com.blamedcloud.parsertongue.tokenizer.TokenType;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;
import com.blamedcloud.parsertongue.tokenizer.TokenizerTypeList;

public class LeftRecursiveTest {

    private static final int TEST_ITERATIONS = 50;
    private static final String REGEX_RULE_NAME = "regex";

    @Test
    public void testSimpleDirect() throws Exception {
        Grammar grammar = getGrammar("src/test/resources/leftRecursive/simpleDirect.ebnf");
        LeftRecursionTransformer lrTransformer = new LeftRecursionTransformer(grammar);
        assertTrue(lrTransformer.isGrammarAffected());

        Rule startRule = grammar.getStartRule();
        assertTrue(lrTransformer.isRuleAffected(startRule));
        assertTrue(lrTransformer.hasDirectLR(startRule));
        assertFalse(lrTransformer.hasIndirectLR(startRule));

        Grammar newGrammar = lrTransformer.getTransformedGrammar();
        LeftRecursionTransformer newLrTransformer = new LeftRecursionTransformer(newGrammar);
        assertFalse(newLrTransformer.isGrammarAffected());

        TokenizerTypeList ttl = TokenizerTypeList.getTTLForAlphabet("a");

        assertFalse(isInLanguage(newGrammar, "", ttl));
        assertTrue( isInLanguage(newGrammar, "a", ttl));
        assertTrue( isInLanguage(newGrammar, "aa", ttl));
        assertTrue( isInLanguage(newGrammar, "aaa", ttl));

        compareGrammars(newGrammar, "a+", newGrammar.getAlphabet());
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

        Grammar newGrammar = lrTransformer.getTransformedGrammar();
        LeftRecursionTransformer newLrTransformer = new LeftRecursionTransformer(newGrammar);
        assertFalse(newLrTransformer.isGrammarAffected());

        TokenizerTypeList ttl = TokenizerTypeList.getTTLForAlphabet("123abc");

        assertFalse(isInLanguage(newGrammar, "", ttl));
        assertFalse(isInLanguage(newGrammar, "cc", ttl));
        assertTrue( isInLanguage(newGrammar, "ac", ttl));
        assertTrue( isInLanguage(newGrammar, "bc", ttl));
        assertTrue( isInLanguage(newGrammar, "3c", ttl));
        assertTrue( isInLanguage(newGrammar, "12c", ttl));

        assertTrue( isInLanguage(newGrammar, "acc", ttl));
        assertTrue( isInLanguage(newGrammar, "bccc", ttl));

        compareGrammars(newGrammar, "((12)|3|a|b)c*", newGrammar.getAlphabet());
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

        Grammar newGrammar = lrTransformer.getTransformedGrammar();
        LeftRecursionTransformer newLrTransformer = new LeftRecursionTransformer(newGrammar);
        assertFalse(newLrTransformer.isGrammarAffected());

        TokenizerTypeList ttl = TokenizerTypeList.getTTLForAlphabet("a");

        assertFalse(isInLanguage(newGrammar, "", ttl));
        assertTrue( isInLanguage(newGrammar, "a", ttl));
        assertFalse(isInLanguage(newGrammar, "aa", ttl));
        assertTrue( isInLanguage(newGrammar, "aaa", ttl));
        assertFalse(isInLanguage(newGrammar, "aaaa", ttl));
        assertTrue( isInLanguage(newGrammar, "aaaaa", ttl));

        compareGrammars(newGrammar, "a(aa)*", newGrammar.getAlphabet());
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

        Grammar newGrammar = lrTransformer.getTransformedGrammar();
        LeftRecursionTransformer newLrTransformer = new LeftRecursionTransformer(newGrammar);
        assertFalse(newLrTransformer.isGrammarAffected());

        TokenizerTypeList ttl = TokenizerTypeList.getTTLForAlphabet("123abc");

        assertFalse(isInLanguage(newGrammar, "", ttl));
        assertFalse(isInLanguage(newGrammar, "cc", ttl));
        assertTrue( isInLanguage(newGrammar, "a", ttl));
        assertTrue( isInLanguage(newGrammar, "b", ttl));
        assertTrue( isInLanguage(newGrammar, "c", ttl));
        assertTrue( isInLanguage(newGrammar, "1c3", ttl));

        assertTrue( isInLanguage(newGrammar, "123", ttl));
        assertTrue( isInLanguage(newGrammar, "223", ttl));
        assertTrue( isInLanguage(newGrammar, "1123", ttl));
    }

    private boolean isInLanguage(Grammar grammar, String input, TokenizerTypeList ttl) {
        Tokenizer tokens = new Tokenizer(ttl, true);
        tokens.tokenize(input);
        return grammar.isInLanguage(tokens);
    }

    private Grammar getGrammar(String path) throws Exception {
        return Grammar.newBuilder(new File(path)).build();
    }

    private void compareGrammars(Grammar grammar, String regex, Set<String> regexAlphabet) throws Exception {
        Map<String, Boolean> oldClassification = grammar.classifyFirstNStrings(TEST_ITERATIONS);

        TokenType regexTokenType = new TokenType(REGEX_RULE_NAME, regex);
        Token lhs = Grammar.getIdentifierToken(REGEX_RULE_NAME);
        Rule rule = Rule.newBuilder().setLHSToken(lhs).setRegexTokenType(regexTokenType).build();

        List<Rule> rules = new ArrayList<>();
        rules.add(rule);

        Grammar regexGrammar = new Grammar(rules, REGEX_RULE_NAME, false);

        Map<String, Boolean> regexClassification = regexGrammar.classifyFirstNStrings(TEST_ITERATIONS, regexAlphabet);
        assertEquals("The classifications should be the same", regexClassification, oldClassification);
    }

}
