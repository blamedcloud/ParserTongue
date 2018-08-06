#!/usr/bin/env python3
import sys
import tokenizer
import grammar
import parser

def grammarTest(inFile = 'ebnf.ebnf'):
	ebnf = grammar.Grammar(inFile)
	for i, rule in enumerate(ebnf.getRuleList()):
		print("Rule " + str(i) + ":\n")
		print(str(rule))
		print()

def tokenizerTest():
	with open("ebnf.ebnf", "r") as FILE:
		tokens = tokenizer.Tokenizer(FILE)
	print(tokens)

def grammarEnumeration(grammarFile, number):
	g = grammar.Grammar(grammarFile)
	classification = g.classifyFirstNStrings(number)
	notInLang = sorted([k for k, v in classification.items() if not v], key=lambda x: (len(x),x))
	isInLang = sorted([k for k, v in classification.items() if v], key=lambda x: (len(x), x))
	print("In Language:")
	print('\t' + str(isInLang))
	print("Not In Language:")
	print('\t' + str(notInLang))

def parserTest(grammarFile, testString, _debug = False):
	p = parser.Parser(grammarFile)
	p.getGrammarAlphabet()
	if p.parse(testString, debug = _debug):
		print("Test String in Language!")
	else:
		print("Test String NOT in Language!")

if __name__ == "__main__":
	if len(sys.argv) == 2:
		inFile = sys.argv[1]
		grammarTest(inFile)
	elif len(sys.argv) == 3:
		grammarFile = sys.argv[1]
		testStr = sys.argv[2]
		try:
			testNum = int(testStr)
			grammarEnumeration(grammarFile, testNum)
		except ValueError:
			parserTest(grammarFile, testStr)
	elif len(sys.argv) == 4:
		print('Test')
		grammarFile = sys.argv[1]
		testStr = sys.argv[2]
		parserTest(grammarFile, testStr, True)
	else:
		tokenizerTest()

