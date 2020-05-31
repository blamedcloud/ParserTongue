package com.blamedcloud.parsertongue.tokenizer;

import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.COMMENT_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.CONTROL_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.DEFINE_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.END_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.EXTERNAL_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.IDENTIFIER_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.REGEX_NAME;
import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.TERMINAL_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GrammarTTLTest {

    @Test
    public void TestControl() {
        Tokenizer tokenizer = new Tokenizer();
        TokenType controlType = tokenizer.getTTL().get(CONTROL_NAME);

        String input = "[[(|,}){]";
        tokenizer.tokenize(input);

        assertEquals(input.length(), tokenizer.size());

        do {
            Token currentToken = tokenizer.currentToken();
            assertTrue(currentToken.getType().isSameAs(controlType));
        } while (tokenizer.nextToken());
    }

    @Test
    public void TestIdentifier() {
        Tokenizer tokenizer = new Tokenizer();
        TokenType identifierType = tokenizer.getTTL().get(IDENTIFIER_NAME);

        String input = "identifier,Id_2_special anotherOne";
        tokenizer.tokenize(input);

        assertEquals(4, tokenizer.size());

        Token currentToken = tokenizer.currentToken();
        assertTrue(currentToken.getType().isSameAs(identifierType));
        assertEquals("identifier", currentToken.getValue());

        assertTrue(tokenizer.nextToken());
        currentToken = tokenizer.currentToken();
        // skip the control token

        assertTrue(tokenizer.nextToken());
        currentToken = tokenizer.currentToken();
        assertTrue(currentToken.getType().isSameAs(identifierType));
        assertEquals("Id_2_special", currentToken.getValue());

        assertTrue(tokenizer.nextToken());
        currentToken = tokenizer.currentToken();
        assertTrue(currentToken.getType().isSameAs(identifierType));
        assertEquals("anotherOne", currentToken.getValue());
    }

    @Test
    public void TestTerminal() {
        Tokenizer tokenizer = new Tokenizer();
        TokenType terminalType = tokenizer.getTTL().get(TERMINAL_NAME);

        String input = "'simple' \n \t 'single \"quoted\" string' \"double quoted string with 'single quotes'\"";
        tokenizer.tokenize(input);

        assertEquals(3, tokenizer.size());

        Token currentToken = tokenizer.currentToken();
        assertTrue(currentToken.getType().isSameAs(terminalType));
        assertEquals("simple", currentToken.getValue());

        assertTrue(tokenizer.nextToken());
        currentToken = tokenizer.currentToken();
        assertTrue(currentToken.getType().isSameAs(terminalType));
        assertEquals("single \"quoted\" string", currentToken.getValue());

        assertTrue(tokenizer.nextToken());
        currentToken = tokenizer.currentToken();
        assertTrue(currentToken.getType().isSameAs(terminalType));
        assertEquals("double quoted string with 'single quotes'", currentToken.getValue());
    }

    @Test
    public void TestComment() {
        Tokenizer tokenizer = new Tokenizer();
        TokenType commentType = tokenizer.getTTL().get(COMMENT_NAME);

        String input = "stuff = 'a' | { 'b' # comment\n } ;#another comment";
        tokenizer.tokenize(input);

        assertEquals(8, tokenizer.size());

        do {
            Token currentToken = tokenizer.currentToken();
            // comments are ignored, and shouldn't be in the tokenizer
            assertFalse(currentToken.getType().isSameAs(commentType));
        } while (tokenizer.nextToken());
    }

    @Test
    public void TestOthers() {
        Tokenizer tokenizer = new Tokenizer();
        TokenType endType = tokenizer.getTTL().get(END_NAME);
        TokenType defineType = tokenizer.getTTL().get(DEFINE_NAME);
        TokenType externalType = tokenizer.getTTL().get(EXTERNAL_NAME);
        TokenType regexType = tokenizer.getTTL().get(REGEX_NAME);

        String input = ";=:~";
        tokenizer.tokenize(input);

        assertEquals(input.length(), tokenizer.size());

        Token currentToken = tokenizer.currentToken();
        assertTrue(currentToken.getType().isSameAs(endType));

        assertTrue(tokenizer.nextToken());
        currentToken = tokenizer.currentToken();
        assertTrue(currentToken.getType().isSameAs(defineType));

        assertTrue(tokenizer.nextToken());
        currentToken = tokenizer.currentToken();
        assertTrue(currentToken.getType().isSameAs(externalType));

        assertTrue(tokenizer.nextToken());
        currentToken = tokenizer.currentToken();
        assertTrue(currentToken.getType().isSameAs(regexType));
    }

}
