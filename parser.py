#!/usr/bin/env python3
#parser.py
from grammar import Grammar
from tokenizer import Tokenizer, getTTLForAlphabet

class Parser(object):

	def __init__(self, grammarFile, startIdentifier = None):
		self.grammar = Grammar(grammarFile, startSym = startIdentifier)
		self.alphabet = None
		self.ttl = None
		self.setGrammarAlphabet()

	def setTTL(self, ttl):
		self.ttl = ttl

	def setAlphabet(self, alphabet):
		self.alphabet = alphabet
		self.ttl = getTTLForAlphabet(self.alphabet)

	def setGrammarAlphabet(self):
		self.alphabet = self.grammar.getAlphabet()
		self.ttl = getTTLForAlphabet(self.alphabet)

	def parse(self, raw, ignoreWS = False, debug = False):
		tokens = Tokenizer(self.ttl, ignoreWS)
		tokens.tokenize(raw)
		return self.grammar.isInLanguage(tokens, debug)


