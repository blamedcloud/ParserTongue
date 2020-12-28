package com.blamedcloud.parsertongue.grammar;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;

import org.junit.Test;

import com.blamedcloud.parsertongue.tokenizer.Tokenizer;

public class GrammarSaverTest {

    @Test
    public void testSimpleGrammar() throws Exception {
        String filePath = "src/test/resources/b_aStar_c.ebnf";
        testGrammar(filePath);
    }

    @Test
    public void testComplexGrammar() throws Exception {
        String filePath = "src/test/resources/testAllInternal.ebnf";
        testGrammar(filePath);
    }

    private void testGrammar(String filePath) throws Exception {
        Grammar grammar = getGrammar(filePath);
        Tokenizer originalTokens = getTokensFromFile(filePath);

        String savedGrammar = GrammarSaver.saveGrammar(grammar);
        Tokenizer newTokens = getTokensFromString(savedGrammar);

        assertTrue("The grammars should have the same tokens", originalTokens.hasSameTokens(newTokens));
    }

    private Grammar getGrammar(String path) throws Exception {
        return Grammar.newBuilder(new File(path)).build();
    }

    private Tokenizer getTokensFromFile(String path) throws Exception {
        File file = new File(path);
        String fullText = Files.readString(file.toPath());
        return getTokensFromString(fullText);
    }

    private Tokenizer getTokensFromString(String grammarString) throws Exception {
        Tokenizer tokenizer = Grammar.newTokenizer();
        tokenizer.tokenize(grammarString);
        return tokenizer;
    }

}
