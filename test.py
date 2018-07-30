#!/usr/bin/env python3
import tokenizer

if __name__ == "__main__":
	
	with open("ebnf.ebnf", "r") as FILE:
		tokens = tokenizer.Tokenizer(FILE)
	print(tokens)

