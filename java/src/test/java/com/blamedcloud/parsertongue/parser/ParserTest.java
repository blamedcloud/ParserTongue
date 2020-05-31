package com.blamedcloud.parsertongue.parser;

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

}
