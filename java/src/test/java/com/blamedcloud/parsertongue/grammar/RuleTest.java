package com.blamedcloud.parsertongue.grammar;

import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.IDENTIFIER_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.TERMINAL_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.blamedcloud.parsertongue.tokenizer.Token;
import com.blamedcloud.parsertongue.tokenizer.TokenType;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;

public class RuleTest {

    // NOTE: by the time rules get to the rule class, they are stripped of the ';'

    @Test
    public void externalRuleTest() {
        Tokenizer tokenizer = new Tokenizer();
        TokenType identifierType = tokenizer.getTTL().get(IDENTIFIER_NAME);

        String ruleInput = "this = otherGrammar : that";
        tokenizer.tokenize(ruleInput);

        Rule rule = new Rule(tokenizer);

        Token lhs = rule.lhs();
        assertTrue(identifierType.isSameAs(lhs.getType()));
        assertEquals("this", lhs.getValue());

        assertTrue(rule.hasDependency());
        assertEquals("otherGrammar", rule.getDependencyName());

        RHSTree rhs = rule.rhs();
        assertEquals(RHSType.IDENTIFIER, rhs.getType());
        assertEquals(0, rhs.size());

        Token node = rhs.getNode();
        assertTrue(identifierType.isSameAs(node.getType()));
        assertEquals("that", node.getValue());
    }

    @Test
    public void regexRuleTest() {
        Tokenizer tokenizer = new Tokenizer();
        TokenType identifierType = tokenizer.getTTL().get(IDENTIFIER_NAME);

        TokenType regexType = new TokenType("aManyB", "ab+");

        String ruleInput = "aManyB=~'ab+'";
        tokenizer.tokenize(ruleInput);

        Rule rule = new Rule(tokenizer);

        Token lhs = rule.lhs();
        assertTrue(identifierType.isSameAs(lhs.getType()));
        assertEquals("aManyB", lhs.getValue());

        assertTrue(rule.isRegexRule());
        assertTrue(regexType.isSameAs(rule.getRegexTokenType()));

        RHSTree rhs = rule.rhs();
        assertEquals(RHSType.REGEX, rhs.getType());
        assertEquals(0, rhs.size());

        assertTrue(regexType.isSameAs(rhs.getRegexNode()));
    }

    @Test
    public void optionalRuleTest() {
        Tokenizer tokenizer = new Tokenizer();
        TokenType identifierType = tokenizer.getTTL().get(IDENTIFIER_NAME);
        TokenType terminalType = tokenizer.getTTL().get(TERMINAL_NAME);

        String ruleInput = "start = [ 'text' ]";
        tokenizer.tokenize(ruleInput);

        Rule rule = new Rule(tokenizer);

        Token lhs = rule.lhs();
        assertTrue(identifierType.isSameAs(lhs.getType()));
        assertEquals("start", lhs.getValue());

        assertFalse(rule.hasDependency());
        assertFalse(rule.isRegexRule());

        RHSTree rhs = rule.rhs();
        assertEquals(RHSType.OPTIONAL, rhs.getType());
        assertEquals(1, rhs.size());

        RHSTree child = rhs.getChild();
        assertEquals(RHSType.TERMINAL, child.getType());
        assertEquals(0, child.size());

        Token node = child.getNode();
        assertTrue(terminalType.isSameAs(node.getType()));
        assertEquals("text", node.getValue());
    }

    @Test
    public void repeatRuleTest() {
        Tokenizer tokenizer = new Tokenizer();
        TokenType identifierType = tokenizer.getTTL().get(IDENTIFIER_NAME);
        TokenType terminalType = tokenizer.getTTL().get(TERMINAL_NAME);

        String ruleInput = "start = { 'text' }";
        tokenizer.tokenize(ruleInput);

        Rule rule = new Rule(tokenizer);

        Token lhs = rule.lhs();
        assertTrue(identifierType.isSameAs(lhs.getType()));
        assertEquals("start", lhs.getValue());

        assertFalse(rule.hasDependency());
        assertFalse(rule.isRegexRule());

        RHSTree rhs = rule.rhs();
        assertEquals(RHSType.REPEAT, rhs.getType());
        assertEquals(1, rhs.size());

        RHSTree child = rhs.getChild();
        assertEquals(RHSType.TERMINAL, child.getType());
        assertEquals(0, child.size());

        Token node = child.getNode();
        assertTrue(terminalType.isSameAs(node.getType()));
        assertEquals("text", node.getValue());
    }

    @Test
    public void groupRuleTest() {
        Tokenizer tokenizer = new Tokenizer();
        TokenType identifierType = tokenizer.getTTL().get(IDENTIFIER_NAME);
        TokenType terminalType = tokenizer.getTTL().get(TERMINAL_NAME);

        String ruleInput = "start = ( 'text' )";
        tokenizer.tokenize(ruleInput);

        Rule rule = new Rule(tokenizer);

        Token lhs = rule.lhs();
        assertTrue(identifierType.isSameAs(lhs.getType()));
        assertEquals("start", lhs.getValue());

        assertFalse(rule.hasDependency());
        assertFalse(rule.isRegexRule());

        RHSTree rhs = rule.rhs();
        assertEquals(RHSType.GROUP, rhs.getType());
        assertEquals(1, rhs.size());

        RHSTree child = rhs.getChild();
        assertEquals(RHSType.TERMINAL, child.getType());
        assertEquals(0, child.size());

        Token node = child.getNode();
        assertTrue(terminalType.isSameAs(node.getType()));
        assertEquals("text", node.getValue());
    }

    @Test
    public void alternationRuleTest() {
        Tokenizer tokenizer = new Tokenizer();
        TokenType identifierType = tokenizer.getTTL().get(IDENTIFIER_NAME);
        TokenType terminalType = tokenizer.getTTL().get(TERMINAL_NAME);

        String ruleInput = "start = 'a' | b | 'c'";
        tokenizer.tokenize(ruleInput);

        Rule rule = new Rule(tokenizer);

        Token lhs = rule.lhs();
        assertTrue(identifierType.isSameAs(lhs.getType()));
        assertEquals("start", lhs.getValue());

        assertFalse(rule.hasDependency());
        assertFalse(rule.isRegexRule());

        RHSTree rhs = rule.rhs();
        assertEquals(RHSType.ALTERNATION, rhs.getType());
        assertEquals(3, rhs.size());

        RHSTree child;
        Token node;

        child = rhs.getChild(0);
        assertEquals(RHSType.TERMINAL, child.getType());
        assertEquals(0, child.size());
        node = child.getNode();
        assertTrue(terminalType.isSameAs(node.getType()));
        assertEquals("a", node.getValue());

        child = rhs.getChild(1);
        assertEquals(RHSType.IDENTIFIER, child.getType());
        assertEquals(0, child.size());
        node = child.getNode();
        assertTrue(identifierType.isSameAs(node.getType()));
        assertEquals("b", node.getValue());

        child = rhs.getChild(2);
        assertEquals(RHSType.TERMINAL, child.getType());
        assertEquals(0, child.size());
        node = child.getNode();
        assertTrue(terminalType.isSameAs(node.getType()));
        assertEquals("c", node.getValue());
    }

    @Test
    public void concatenationRuleTest() {
        Tokenizer tokenizer = new Tokenizer();
        TokenType identifierType = tokenizer.getTTL().get(IDENTIFIER_NAME);
        TokenType terminalType = tokenizer.getTTL().get(TERMINAL_NAME);

        String ruleInput = "start = 'a' , b,c , 'd'";
        tokenizer.tokenize(ruleInput);

        Rule rule = new Rule(tokenizer);

        Token lhs = rule.lhs();
        assertTrue(identifierType.isSameAs(lhs.getType()));
        assertEquals("start", lhs.getValue());

        assertFalse(rule.hasDependency());
        assertFalse(rule.isRegexRule());

        RHSTree rhs = rule.rhs();
        assertEquals(RHSType.CONCATENATION, rhs.getType());
        assertEquals(4, rhs.size());

        RHSTree child;
        Token node;

        child = rhs.getChild(0);
        assertEquals(RHSType.TERMINAL, child.getType());
        assertEquals(0, child.size());
        node = child.getNode();
        assertTrue(terminalType.isSameAs(node.getType()));
        assertEquals("a", node.getValue());

        child = rhs.getChild(1);
        assertEquals(RHSType.IDENTIFIER, child.getType());
        assertEquals(0, child.size());
        node = child.getNode();
        assertTrue(identifierType.isSameAs(node.getType()));
        assertEquals("b", node.getValue());

        child = rhs.getChild(2);
        assertEquals(RHSType.IDENTIFIER, child.getType());
        assertEquals(0, child.size());
        node = child.getNode();
        assertTrue(identifierType.isSameAs(node.getType()));
        assertEquals("c", node.getValue());

        child = rhs.getChild(3);
        assertEquals(RHSType.TERMINAL, child.getType());
        assertEquals(0, child.size());
        node = child.getNode();
        assertTrue(terminalType.isSameAs(node.getType()));
        assertEquals("d", node.getValue());
    }

    @Test
    public void complexRuleTest() {
        Tokenizer tokenizer = new Tokenizer();
        TokenType identifierType = tokenizer.getTTL().get(IDENTIFIER_NAME);
        TokenType terminalType = tokenizer.getTTL().get(TERMINAL_NAME);

        String ruleInput = "start = { 'a' | b } | ( c , [ 'd' ] )";
        tokenizer.tokenize(ruleInput);

        Rule rule = new Rule(tokenizer);

        Token lhs = rule.lhs();
        assertTrue(identifierType.isSameAs(lhs.getType()));
        assertEquals("start", lhs.getValue());

        assertFalse(rule.hasDependency());
        assertFalse(rule.isRegexRule());

        RHSTree rhs = rule.rhs();
        assertEquals(RHSType.ALTERNATION, rhs.getType());
        assertEquals(2, rhs.size());

        // left overall alternate: { 'a' | b }
        {
            RHSTree repeat = rhs.getChild(0);
            assertEquals(RHSType.REPEAT, repeat.getType());
            assertEquals(1, repeat.size());

            // inner alternation: 'a' | b
            {
                RHSTree alternation = repeat.getChild();
                assertEquals(RHSType.ALTERNATION, alternation.getType());
                assertEquals(2, alternation.size());

                RHSTree child;
                Token node;

                child = alternation.getChild(0);
                assertEquals(RHSType.TERMINAL, child.getType());
                assertEquals(0, child.size());
                node = child.getNode();
                assertTrue(terminalType.isSameAs(node.getType()));
                assertEquals("a", node.getValue());

                child = alternation.getChild(1);
                assertEquals(RHSType.IDENTIFIER, child.getType());
                assertEquals(0, child.size());
                node = child.getNode();
                assertTrue(identifierType.isSameAs(node.getType()));
                assertEquals("b", node.getValue());
            }
        }

        // right overall alternate: ( c , [ 'd' ] )
        {
            RHSTree group = rhs.getChild(1);
            assertEquals(RHSType.GROUP, group.getType());
            assertEquals(1, group.size());

            // inner concatenation: c , [ 'd ]
            {
                RHSTree concatenation = group.getChild();
                assertEquals(RHSType.CONCATENATION, concatenation.getType());
                assertEquals(2, concatenation.size());

                RHSTree child = concatenation.getChild(0);
                assertEquals(RHSType.IDENTIFIER, child.getType());
                assertEquals(0, child.size());
                Token node = child.getNode();
                assertTrue(identifierType.isSameAs(node.getType()));
                assertEquals("c", node.getValue());

                // optional: [ 'd' ]
                {
                    RHSTree optional = concatenation.getChild(1);
                    assertEquals(RHSType.OPTIONAL, optional.getType());
                    assertEquals(1, optional.size());

                    child = optional.getChild();
                    assertEquals(RHSType.TERMINAL, child.getType());
                    assertEquals(0, child.size());
                    node = child.getNode();
                    assertTrue(terminalType.isSameAs(node.getType()));
                    assertEquals("d", node.getValue());
                }
            }
        }

        Set<String> terminals = new HashSet<>();
        terminals.add("a");
        terminals.add("d");

        assertEquals(terminals, rule.getTerminals());
    }

}
