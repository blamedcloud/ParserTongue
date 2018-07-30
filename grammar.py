#!/usr/bin/env python3
import tokenizer

class Grammar(object):
	
	def __init__(self, grammarFile):
		full_text = ""
		with open(grammarFile) as FILE:
			for line in FILE:
				full_text += line
		full_text = full_text.replace("\n"," ")
		rawRules = full_text.split(';')
		if rawRules[-1] != '':
			#Last entry should be empty
			#because last rule should end with ';'
			raise GrammarParsingError("Found Text after last rule:\n\t" + rawRules[-1])
		rawRules = rawRules[:-1]
		ruleObjs = [Rule(r) for r in rawRules]
		
		self.ruleDict = {r.lhs() : r for r in ruleObjs}
		
		



class Rule(object):
	
	def __init__(self, rawRule):
		self.terminals = set()
		self.nonTerminals = {}

	def _parseOutComments(self):
		pass

	def lhs(self):
		pass

	def _parseRaw(self, raw):
		tokensObj = tokenizer.Tokenizer(raw)
		tokens = tokensObj.getTokenList()
		tokenStrs = [str(x) for x in tokens]
		if '=' not in tokenStrs:
			raise RuleParsingError("Rule has no '=':\n\t" + raw)
		defIndex = tokenStrs.index('=')
		









class GrammarParsingError(Exception):

	def __init__(self, message):
		self.message = message


class RuleParsingError(Exception):

	def __init__(self, message):
		self.message = message

 
