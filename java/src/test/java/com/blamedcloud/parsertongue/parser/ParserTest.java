package com.blamedcloud.parsertongue.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ParserTest {

    @Test
    public void testAllExternal() {

        File grammarFile = new File("src/test/resources/testAllExternal.ebnf");

        List<File> dependentFiles = new ArrayList<>();
        dependentFiles.add(new File("src/test/resources/aToNbToN.ebnf"));
        dependentFiles.add(new File("src/test/resources/aToN.ebnf"));
        dependentFiles.add(new File("src/test/resources/bMaybe_abStar.ebnf"));
        dependentFiles.add(new File("src/test/resources/moreBs.ebnf"));
        dependentFiles.add(new File("src/test/resources/equalABs.ebnf"));
        dependentFiles.add(new File("src/test/resources/b_aStar_c.ebnf"));

        Parser parser = Parser.newBuilder(grammarFile).setDependentGrammarFiles(dependentFiles).build();

        assertTrue(parser.checkString("1aaabbb"));
        assertTrue(parser.checkString("2aaaa"));
        assertTrue(parser.checkString("3bababab"));
        assertTrue(parser.checkString("4bababb"));
        assertTrue(parser.checkString("5bbabaa"));
        assertTrue(parser.checkString("6baac"));

    }

    @Test
    public void testRegexRules() {

        File grammarFile = new File("src/test/resources/regexRule.ebnf");
        Parser parser = Parser.newBuilder(grammarFile).build();

        // first pattern
        assertTrue(parser.checkString("a"));
        assertTrue(parser.checkString("ab"));
        assertTrue(parser.checkString("ac"));
        assertTrue(parser.checkString("ad"));
        assertTrue(parser.checkString("abccccd"));
        assertTrue(parser.checkString("acddd"));
        assertTrue(parser.checkString("abccccdd"));

        assertFalse(parser.checkString("ba"));
        assertFalse(parser.checkString("abcdddd"));

        // second pattern
        assertTrue(parser.checkString("2"));
        assertTrue(parser.checkString("12"));
        assertTrue(parser.checkString("111223333"));
        assertTrue(parser.checkString("123"));
        assertTrue(parser.checkString("2233"));

        assertFalse(parser.checkString("222"));
        assertFalse(parser.checkString("1221"));
        assertFalse(parser.checkString("13"));
    }

}
