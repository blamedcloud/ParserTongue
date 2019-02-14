#!/usr/bin/env python3
#grammar.py
from errors import *
from token import Token, TokenType
from tokenizer import Tokenizer, getTTLForAlphabet
from smallestStrings import smallestStrGen
from rule import Rule

class Grammar(object):

	def __init__(self, grammarFile, startSym = None, lastStart = False, quiet = True):
		self.quiet = quiet
		full_text = ""
		with open(grammarFile) as FILE:
			for line in FILE:
				full_text += line
		fullTokenizer = Tokenizer()
		fullTokenizer.tokenize(full_text)
		splitToken = Token(';', fullTokenizer.getTTL()['End'])
		if not fullTokenizer.getLastToken()	== splitToken:
			raise GrammarParsingError("Found Text after last rule! Did you forget ';'?")
		ruleTokenizers = fullTokenizer.splitTokensOn(splitToken)

		# debug:
	#	for i, r in enumerate(ruleTokenizers):
	#		print(str(i) + ": \n\t" + str(r))

		try:
			self.rules = []
			for i, tokens in enumerate(ruleTokenizers):
				if not self.quiet:
					print("PARSING RULE:",i)
				self.rules.append(Rule(tokens))
		except GrammarError as err:
			print("Exception thrown while parsing rule",i)
			print(err.message)
			raise err
		except Exception as err:
			print("Unknown Exception thrown while parsing rule",i)
			raise err

		self.ruleDict = {r.lhs().getValue() : r for r in self.rules}
		for rule in self.rules:
			rule.createLinkage(self.ruleDict)

		if startSym == None:
			index = 0
			# set last rule as start instead of first
			if lastStart:
				index = -1
			self.start = self.rules[index].lhs().getValue()
		else:
			self.setStart(startSym)

	def getRuleList(self):
		return self.rules

	def setRuleTransformer(self, ruleName, f):
		if ruleName in self.ruleDict:
			self.ruleDict[ruleName].setTransformer(f)
		else:
			raise KeyError("No Rule with name '" + str(ruleName) + "' exists!")

	def setStart(self, startSymbol):
		if startSymbol in self.ruleDict:
			self.start = startSymbol
		else:
			raise GrammarParsingError("Cannot add start symbol: '" + str(startSymbol) + "', because it does not exist!")

	def isInLanguage(self, tokens, debug = False):
		return bool(self.tryMatch(tokens,debug))

	def tryMatch(self, tokens, debug = False):
		for value in self.ruleDict[self.start].expectMatch(tokens, 0, debug):
			if value:
				if len(tokens) == 0:
					return value
				elif tokens.isExhausted():
					return value
		return value

	# NOTE: this is the naive method that tries each possible string in A* (kleene star)
	# until it finds the next string in the language and yields it
	# as such, it is pretty inefficient.
	def getValidStringGen(self, maxIter = None, ignoreWS = True, debug = False):
		def validStrGen(maxIter, ignoreWS, debug):
			(isInfinite, treeSize) = self.ruleDict[self.start].walk()
			iters = 0
			alphabet = self.getAlphabet()
			if '' not in alphabet:
				alphabet.append('')
			tokenizer = Tokenizer(getTTLForAlphabet(alphabet), ignoreWS)
			tokenizer.tokenize('')
			if self.isInLanguage(tokenizer, debug):
				yield ''
			iters += 1
			alphabet.remove('')
			if maxIter is None and not isInfinite:
				# if this is a finite language, we
				# use an upperbound of |A|^num(terminals)
				# where |A| is the size of the alphabet
				# and num(terminals) is (kind of) the number
				# of terminals in the grammar file.
				# This should be an upper bound, but a seriously
				# bad one in most cases.
				maxIter = (len(alphabet)**treeSize) + 1
			finished = False
			if (maxIter is not None) and iters >= maxIter:
				finished = True
			testStringGen = smallestStrGen(alphabet, True)()
			while not finished:
				testStr = next(testStringGen)
				tokenizer.tokenize(testStr)
				if self.isInLanguage(tokenizer, debug):
					yield testStr
				iters += 1
				if (maxIter is not None) and iters >= maxIter:
					finished = True
		return validStrGen(maxIter, ignoreWS, debug)

	def getAlphabet(self):
		alphabet = []
		for rule in self.rules:
			terminals = rule.getTerminals()
			for t in terminals:
				if t not in alphabet:
					alphabet.append(t)
		return sorted(alphabet, key=lambda x: len(x), reverse=True)

	def classifyFirstNStrings(self, number, ignoreWS = True, debug = False):
		alphabet = self.getAlphabet()
		if '' not in alphabet:
			alphabet.append('')
		tokenizer = Tokenizer(getTTLForAlphabet(alphabet), ignoreWS)
		classification = {}
		tokenizer.tokenize('')
		classification[''] = self.isInLanguage(tokenizer, debug)
		alphabet.remove('')
		number -= 1
		stringGen = smallestStrGen(alphabet, True)()
		while number > 0:
			s = next(stringGen)
			tokenizer.tokenize(s)
			classification[s] = self.isInLanguage(tokenizer, debug)
			number -= 1
		return classification


