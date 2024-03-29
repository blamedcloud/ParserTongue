package com.blamedcloud.parsertongue.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

import com.blamedcloud.parsertongue.grammar.expecterator.ParseResultExpecterator;
import com.blamedcloud.parsertongue.grammar.result.ParseResultTransformer;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;
import com.blamedcloud.parsertongue.tokenizer.TokenizerException;
import com.blamedcloud.parsertongue.tokenizer.TokenizerTypeList;

public class RuleExpecteratorTest {

    @Test
    public void simpleTerminalTest() throws Exception {
        Rule rule = createRule("start = 'a'");

        ParseResultTransformer result = parseString(rule, "a");
        assertTrue(result.isValid());
        assertEquals("a", result.getResult().toString());

        TokenizerTypeList ttl = TokenizerTypeList.getTTLForAlphabet("ab");
        ParseResultTransformer badResult = parseString(rule, "b", ttl);
        assertFalse(badResult.isValid());
    }

    @Test
    public void simpleRegexTest() throws Exception {
        Rule rule = createRule("start = ~ 'abc*'");
        Set<String> terminals = new HashSet<>();
        terminals.add("abcc");
        terminals.add("abcd");
        TokenizerTypeList ttl = TokenizerTypeList.getTTLForTerminals(terminals);

        ParseResultTransformer result = parseString(rule, "abcc", ttl);
        assertTrue(result.isValid());
        assertEquals("abcc", result.getResult().toString());

        ParseResultTransformer badResult = parseString(rule, "abcd", ttl);
        assertFalse(badResult.isValid());
    }

    @Test
    public void simpleIdentifierTest() throws Exception {
        Rule first = createRule("first = second");
        Rule second = createRule("second = 'third'");
        Map<String, Rule> ruleMap = new HashMap<>();
        ruleMap.put("second", second);
        first.createLinkage(ruleMap, null);

        Set<String> terminals = new HashSet<>();
        terminals.add("third");
        terminals.add("fourth");
        TokenizerTypeList ttl = TokenizerTypeList.getTTLForTerminals(terminals);

        ParseResultTransformer result = parseString(first, "third", ttl);
        assertTrue(result.isValid());
        assertEquals("third", result.getResult().toString());

        ParseResultTransformer badResult = parseString(first, "fourth", ttl);
        assertFalse(badResult.isValid());
    }

    @Test
    public void simpleGroupTest() throws Exception {
        Rule rule = createRule("start = ( 'a' )");
        TokenizerTypeList ttl = TokenizerTypeList.getTTLForAlphabet("ab");

        ParseResultTransformer result = parseString(rule, "a", ttl);
        assertTrue(result.isValid());
        assertEquals("a", result.getResult().toString());

        ParseResultTransformer badResult = parseString(rule, "b", ttl);
        assertFalse(badResult.isValid());
    }

    @Test
    public void simpleOptionalTest() throws Exception {
        Rule rule = createRule("start = [ 'a' ]");
        TokenizerTypeList ttl = TokenizerTypeList.getTTLForAlphabet("ab");

        ParseResultTransformer result = parseString(rule, "a", ttl);
        assertTrue(result.isValid());
        assertEquals("a", result.getResult().toString());

        ParseResultTransformer emptyResult = parseString(rule, "", ttl);
        assertTrue(emptyResult.isValid());
        assertEquals("", emptyResult.getResult().toString());

        ParseResultTransformer badResult = parseString(rule, "b", ttl);
        assertFalse(badResult.isValid());
    }

    @Test
    public void simpleAlternationTest() throws Exception {
        Rule rule = createRule("start = 'a' | 'b'");
        TokenizerTypeList ttl = TokenizerTypeList.getTTLForAlphabet("abc");

        ParseResultTransformer firstResult = parseString(rule, "a", ttl);
        assertTrue(firstResult.isValid());
        assertEquals("a", firstResult.getResult().toString());

        ParseResultTransformer secondResult = parseString(rule, "b", ttl);
        assertTrue(secondResult.isValid());
        assertEquals("b", secondResult.getResult().toString());

        ParseResultTransformer badResult = parseString(rule, "c", ttl);
        assertFalse(badResult.isValid());
    }

    @Test
    public void simpleRepeatTest() throws Exception {
        Rule rule = createRule("start = { 'a' }");
        TokenizerTypeList ttl = TokenizerTypeList.getTTLForAlphabet("ab");

        ParseResultTransformer result;

        result = parseString(rule, "", ttl);
        assertTrue(result.isValid());
        assertEquals("[]", result.getResult().toString());

        result = parseString(rule, "a", ttl);
        assertTrue(result.isValid());
        assertEquals("[a]", result.getResult().toString());

        result = parseString(rule, "aa", ttl);
        assertTrue(result.isValid());
        assertEquals("[a, a]", result.getResult().toString());

        result = parseString(rule, "aaa", ttl);
        assertTrue(result.isValid());
        assertEquals("[a, a, a]", result.getResult().toString());

        result = parseString(rule, "aaaa", ttl);
        assertTrue(result.isValid());
        assertEquals("[a, a, a, a]", result.getResult().toString());

        result = parseString(rule, "aaba", ttl);
        assertFalse(result.isValid());

        result = parseString(rule, "ab", ttl);
        assertFalse(result.isValid());

        result = parseString(rule, "aab", ttl);
        assertFalse(result.isValid());

        result = parseString(rule, "b", ttl);
        assertFalse(result.isValid());
    }

    @Test
    public void simpleConcatTest() throws Exception {
        Rule rule = createRule("start = 'a' , 'b' , 'a'");
        TokenizerTypeList ttl = TokenizerTypeList.getTTLForAlphabet("abc");

        ParseResultTransformer result;

        result = parseString(rule, "aba", ttl);
        assertTrue(result.isValid());
        assertEquals("[a, b, a]", result.getResult().toString());

        result = parseString(rule, "aab", ttl);
        assertFalse(result.isValid());

        result = parseString(rule, "ab", ttl);
        assertFalse(result.isValid());

        result = parseString(rule, "ba", ttl);
        assertFalse(result.isValid());

        result = parseString(rule, "abac", ttl);
        assertFalse(result.isValid());
    }

    @Test
    public void complexRuleTest() throws Exception {
        Rule rule = createRule("start = 'a' | ('b' | [ 'c' | 'd' ] ) | 'e'");
        TokenizerTypeList ttl = TokenizerTypeList.getTTLForAlphabet("abcdef");

        ParseResultTransformer result;

        result = parseString(rule, "", ttl);
        assertTrue(result.isValid());
        assertEquals("", result.getResult().toString());

        result = parseString(rule, "a", ttl);
        assertTrue(result.isValid());
        assertEquals("a", result.getResult().toString());

        result = parseString(rule, "b", ttl);
        assertTrue(result.isValid());
        assertEquals("b", result.getResult().toString());

        result = parseString(rule, "c", ttl);
        assertTrue(result.isValid());
        assertEquals("c", result.getResult().toString());

        result = parseString(rule, "d", ttl);
        assertTrue(result.isValid());
        assertEquals("d", result.getResult().toString());

        result = parseString(rule, "e", ttl);
        assertTrue(result.isValid());
        assertEquals("e", result.getResult().toString());

        result = parseString(rule, "f", ttl);
        assertFalse(result.isValid());
    }

    private Rule createRule(String ruleInput) throws Exception {
        Tokenizer tokenizer = Grammar.newTokenizer();
        tokenizer.tokenize(ruleInput);
        Rule rule = new Rule(tokenizer);
        return rule;
    }

    private ParseResultTransformer parseString(Rule rule, String input) {
        TokenizerTypeList ttl = TokenizerTypeList.getTTLForTerminals(rule.getTerminals());
        return parseString(rule, input, ttl);
    }

    private ParseResultTransformer parseString(Rule rule, String input, TokenizerTypeList ttl) {
        Tokenizer tokens = new Tokenizer(ttl, true);
        try {
            tokens.tokenize(input);
        } catch (TokenizerException e) {
            return new ParseResultTransformer(false, null, e.getMessage());
        }

        ParseResultExpecterator expecterator = rule.getExpecterator(tokens);
        ParseResultTransformer result = null;
        while (expecterator.hasNext()) {
            Optional<ParseResultTransformer> optionalResult = expecterator.tryNext();
            if (optionalResult.isPresent()) {
                result = optionalResult.get();
                if (result.isValid()) {
                    if (tokens.size() == 0) {
                        return result;
                    } else if (tokens.isExhausted()) {
                        return result;
                    }
                }
            }
        }
        if ((result != null) && (tokens.isExhausted() || tokens.size() == 0)) {
            return result;
        }
        return new ParseResultTransformer(false, null, "Tokens not Exhausted");
    }

}
