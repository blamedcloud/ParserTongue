#!/usr/bin/env python3
import sys
import tokenizer
import grammar

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

if __name__ == "__main__":
	inFile = 'ebnf.ebnf'
	if len(sys.argv) == 2:
		inFile = sys.argv[1]
	grammarTest(inFile)
