#!/usr/bin/env python3
import tokenizer
import grammar

def grammarTest():
	ebnf = grammar.Grammar('ebnf.ebnf')
	for i, rule in enumerate(ebnf.getRuleList()):
		print("Rule " + str(i) + ":\n")
		print(str(rule))
		print("\n")

def tokenizerTest():
	with open("ebnf.ebnf", "r") as FILE:
		tokens = tokenizer.Tokenizer(FILE)
	print(tokens)

if __name__ == "__main__":
	grammarTest()
