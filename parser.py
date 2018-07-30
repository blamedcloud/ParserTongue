#!/usr/bin/env python3


class Parser(object):

	def __init__(self, grammar, rawFile):
		self.grammar = grammar
		with open(rawFile, 'r') as FILE:
			self.tokens = Tokenizer(FILE)	
		


