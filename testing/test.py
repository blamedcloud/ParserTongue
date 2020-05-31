#!/usr/bin/env python3
#test.py
import sys
sys.path.insert(0,'../ParserTongue')
import tokenizer
import grammar
import parser

def grammarTest(inFile = '../ParserTongue/ebnf.ebnf'):
    ebnf = grammar.Grammar(inFile)
    for i, rule in enumerate(ebnf.getRuleList()):
        print("Rule " + str(i) + ":\n")
        print(str(rule))
        print()

def tokenizerTest():
    with open("../ParserTongue/ebnf.ebnf", "r") as FILE:
        tokens = tokenizer.Tokenizer(FILE)
    print(tokens)

def grammarEnumeration(grammarFile, number, debug = False):
    g = grammar.Grammar(grammarFile)
    classification = g.classifyFirstNStrings(number, debug = debug)
    notInLang = sorted([k for k, v in classification.items() if not v], key=lambda x: (len(x),x))
    isInLang = sorted([k for k, v in classification.items() if v], key=lambda x: (len(x), x))
    print("In Language:")
    print('\t' + str(isInLang))
    print("Not In Language:")
    print('\t' + str(notInLang))

def parserTest(grammarFile, testString, _debug = False):
    p = parser.Parser(grammarFile)
    if p.parse(testString, debug = _debug):
        print("Test String in Language!")
    else:
        print("Test String NOT in Language!")

def isInGrammar(grammarFile, testString, debug = False):
    g = grammar.Grammar(grammarFile)
    alphabet = g.getAlphabet()
    tokens = tokenizer.Tokenizer(tokenizer.getTTLForAlphabet(alphabet), True)
    tokens.tokenize(testString)
    if g.isInLanguage(tokens, debug):
        print("Test String in Language!")
    else:
        print("Test String NOT in Language!")

def grammarFileGen(grammarFile, _debug = False):
    g = grammar.Grammar(grammarFile)
    grammarGen(g, _debug)

def grammarGen(g, _debug = False):
    gen = g.getValidStringGen(debug = _debug)
    while input() != 'q':
        print(next(gen))

def grammarFileGenIters(grammarFile, iters, _debug = False):
    g = grammar.Grammar(grammarFile)
    grammarGenIters(g, iters, _debug)

def grammarGenIters(g, iters, _debug = False):
    gen = g.getValidStringGen(debug = _debug)
    for x in range(iters):
        print(next(gen))

def testAllExternal(numIters = -1, _debug = False):
    mainGrammar = "testAllExternal.ebnf"
    otherGrammars = ["aToN.ebnf", "aToNbToN.ebnf", "b_aStar_c.ebnf", "bMaybe_abStar.ebnf", "equalABs.ebnf", "moreBs.ebnf"]
    p = parser.Parser(mainGrammar, dependentGrammarFiles = otherGrammars)
    g = p.getGrammar()
    if numIters > 0:
        grammarGenIters(g, numIters, _debug)
    else:
        grammarGen(g, _debug)

def main2():
    if len(sys.argv) == 2:

        print('Main2, testAllExternal:')

        testNum = int(sys.argv[1])
        testAllExternal(testNum, False)
    elif len(sys.argv) == 3:

        print("Main2, grammarFileGenIters Test:")

        grammarFile = sys.argv[1]
        testStr = sys.argv[2]
        debug = False
        try:
            testNum = int(testStr)
            if testNum < 0:
                debug = True
                testNum = -1*testNum
            if testNum == 0:
                grammarFileGen(grammarFile, debug)
            else:
                grammarFileGenIters(grammarFile, testNum, debug)
        except ValueError:
            parserTest(grammarFile, testStr)
    else:
        testAllExternal()

def main1():
    if len(sys.argv) == 2:
        inFile = sys.argv[1]
        grammarTest(inFile)
    elif len(sys.argv) == 3:
        grammarFile = sys.argv[1]
        testStr = sys.argv[2]
        debug = False
        try:
            testNum = int(testStr)
            if testNum < 0:
                debug = True
                testNum = -1*testNum
            grammarEnumeration(grammarFile, testNum, debug)
        except ValueError:
            parserTest(grammarFile, testStr)
    elif len(sys.argv) == 4:
        print('Test')
        grammarFile = sys.argv[1]
        testStr = sys.argv[2]
        debug = False
        if sys.argv[3] == 'True':
            debug = True
#        parserTest(grammarFile, testStr, True)
        isInGrammar(grammarFile, testStr, debug)
    else:
        tokenizerTest()

if __name__ == "__main__":
    main2()
