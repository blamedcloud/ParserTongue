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

def parserTest(grammarFile, testFile):
	p = parser.Parser(grammarFile)
	if p.parse(testFile):
		print("Test String in Language!")
	else:
		print("Test String NOT in Language!")

if __name__ == "__main__":
	if len(sys.argv) == 2:
		inFile = sys.argv[1]
		grammarTest(inFile)
	elif len(sys.argv) == 3:
		grammarFile = sys.argv[1]
		testFile = sys.argv[2]
		parserTest(grammarFile, testFile)
	else:
		tokenizerTest()

