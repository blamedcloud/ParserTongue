#!/usr/bin/env python3
#parser.py
from grammar import Grammar
from tokenizer import Tokenizer

class Parser(object):

	def __init__(self, grammarFile, startIdentifier = None):
		self.grammar = Grammar(grammarFile, startSym = startIdentifier)

	def parse(self, rawFile):
		tokens = None
		with open(rawFile, 'r') as FILE:
			tokens = Tokenizer(FILE)
		return self.grammar.isInLanguage(tokens)



