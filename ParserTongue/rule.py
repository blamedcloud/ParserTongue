#!/usr/bin/env python3
#rule.py
from errors import *
from rhstree import RHSTree, RHSType, RHSKind, getRHSKind
from tokenizer import defaultGrammarTTL

def identity(x):
	return x

class Rule(object):

	def __init__(self, tokens):
		self.terminals = set()
		self.nonTerminals = {}
		self.lhsToken = None
		self.rhsTree = None
		self.tokens = tokens
		self._parseRule()
		self.transformer = identity

	def setTransformer(self, f):
		self.transformer = f

	def expectMatch(self, tokens, level = 0, debug = False):
		for value in self.rhsTree.expect(tokens, level, debug):
			if value:
				tValue = value.transform(self.transformer)
				yield tValue

	def walk(self, prevIdentifiers = None):
		if prevIdentifiers is not None:
			if self.lhsToken in prevIdentifiers:
				# if there is a recursive loop of identifiers,
				# we assume the language generated is infinite
				# this might not be the case, but it *should*
				# be fine for non-pathalogical examples.
				# For example, it probably runs forever on a
				# left-recursive rule (which we can't parse anyway)
				return (True, -1)
			else:
				identifierList = prevIdentifiers + [self.lhsToken]
				return self.rhsTree.walkTree(identifierList)
		else:
			return self.rhsTree.walkTree([self.lhsToken])

	def getTerminals(self):
		return self.rhsTree.nonLinkedTerminals()

	def lhs(self):
		return self.lhsToken

	def getTTByName(self, name):
		return self.tokens.getTTL()[name]

	def _parseRule(self):
		if len(self.tokens) < 3:
			raise RuleParsingError("Rule has too few tokens: " + raw)
		if self._currentTokenType() == self.getTTByName('Identifier'):
			self.lhsToken = self.tokens.currentToken()
			self.tokens.nextToken()
		else:
			raise RuleParsingError("LHS is not an identifier: " + raw)
		if self._currentTokenType() == self.getTTByName('Define'):
			self.tokens.nextToken()
		else:
			raise RuleParsingError("Rule has no '=': " + raw)
		self.rhsTree = self._parseRHS()
		if not self.tokens.isExhausted():
			raise RuleParsingError("Didn't Exhaust all tokens!")

	def createLinkage(self, ruleDict):
		self.rhsTree.addLinkage(ruleDict)

	def _currentTokenType(self):
		return self.tokens.currentToken().getType()

	def __str__(self):
		return str(self.rhsTree)

	def _parseRHS(self, workingTree = None):
		tree = None
		currentType = self._currentTokenType()
		currentTokenStr = self.tokens.currentToken().getValue()
		if workingTree == None:
			if currentType in [self.getTTByName('Identifier'), self.getTTByName('Terminal')]:
				tree = RHSTree(getRHSLeafTypeFromTokenType(currentType))
				tree.createNode(self.tokens.currentToken())
				if self.tokens.nextToken():
					return self._parseRHS(tree)
				else:
					return tree
			elif currentType == self.getTTByName('Control'):
				if currentTokenStr in ['[', '{', '(']:
					if self.tokens.nextToken():
						tree = RHSTree(getRHSSingleTypeFromTokenValue(currentTokenStr))
						tree.addChild(self._parseRHS())
						newTokenStr = self.tokens.currentToken().getValue()
						newTokenType = self._currentTokenType()
						expectedTokenStr = getMatchingControlBlock(currentTokenStr)
						if newTokenType == self.getTTByName('Control') and expectedTokenStr == newTokenStr:
							if self.tokens.nextToken():
								return self._parseRHS(tree)
							else:
								return tree
						else:
							raise RuleParsingError("ERROR found: '" + newTokenStr + "', excpecting: '" + expectedTokenStr + "'")
					else:
						raise RuleParsingError("ERROR No matching end-symbol matching: '" + currentTokenStr + "'")
				else:
					raise RuleParsingError("ERROR got token: '" + currentTokenStr + "' without Working tree!")
			else:
				raise RuleParsingError("ERROR found token '" + currentTokenStr + "' of type: " + str(currentType))
		else:
			workingKind = workingTree.getKind()
			if workingKind in [RHSKind.LEAF, RHSKind.SINGLE]:
				if currentType == self.getTTByName('Control'):
					if currentTokenStr in [',', '|']:
						tree = RHSTree(getRHSListTypeFromTokenValue(currentTokenStr))
						tree.addChild(workingTree)
						if self.tokens.nextToken():
							tree.addChild(self._parseRHSNonList())
							if self.tokens.isExhausted():
								return tree
							else:
								return self._parseRHS(tree)
						else:
							raise RuleParsingError("ERROR expected rhs subtree after control token: '" + currentTokenStr + "'")
					elif currentTokenStr in [')', '}', ']']:
						return workingTree
					else:
						raise RuleParsingError("ERROR got bad control token: '"+ currentTokenStr +"' after '"+ workingKind.name +"' Kind.")
				else:
					raise RuleParsingError("ERROR got non-control token: '"+ currentTokenStr +"' after '"+ workingKind.name +"' Kind.")
			else:	# workingTree.getKind() == RHSKind.LIST:
				if currentType == self.getTTByName('Control'):
					if currentTokenStr in [',', '|']:
						if getRHSListTypeFromTokenValue(currentTokenStr) == workingTree.getType():
							if self.tokens.nextToken():
								workingTree.addChild(self._parseRHSNonList())
								if self.tokens.isExhausted():
									return workingTree
								else:
									return self._parseRHS(workingTree)
							else:
								raise RuleParsingError("ERROR expected rhs subtree after control token: '" + currentTokenStr + "'")
						else:
							tree = RHSTree(getRHSListTypeFromTokenValue(currentTokenStr))
							if currentTokenStr == '|':
								tree.addChild(workingTree)
								if self.tokens.nextToken():
									tree.addChild(self._parseRHSNonList())
									if self.tokens.isExhausted():
										return tree
									else:
										return self._parseRHS(tree)
								else:
									raise RuleParsingError("ERROR expected rhs subtree after control token: '" + currentTokenStr + "'")
							else:	# currentTokenStr == ',':
									# workingType = ALTERNATION ('|')
								rightChild = workingTree.popRightChild()
								tree.addChild(rightChild)
								while (not self.tokens.isExhausted()) and self._currentTokenIsConcatenation():
									if self.tokens.nextToken():
										tree.addChild(self._parseRHSNonList())
									else:
										raise RuleParsingError("Error, expected rhs subtree after control token: '" + currentTokenStr + "'")
								workingTree.addChild(tree)
								if self.tokens.isExhausted():
									return workingTree
								else:
									return self._parseRHS(workingTree)
					elif currentTokenStr in [')', '}', ']']:
						return workingTree
					else:
						raise RuleParsingError("ERROR found bad control token: '" + currentTokenStr + "' after LIST Kind.")
				else:
					raise RuleParsingError("ERROR found token '" + currentTokenStr + "' of type: " + str(currentType))
		raise NotImplementedError # I must have missed something

	def _currentTokenIsConcatenation(self):
		thisTokenValue = self.tokens.currentToken().getValue()
		thisTokenType = self.tokens.currentToken().getType()
		if thisTokenType == self.getTTByName('Control') and thisTokenValue == ',':
			return True
		return False

	def _parseRHSNonList(self):
		tree = None
		currentType = self._currentTokenType()
		currentTokenStr = self.tokens.currentToken().getValue()
		if currentType in [self.getTTByName('Identifier'), self.getTTByName('Terminal')]:
			tree = RHSTree(getRHSLeafTypeFromTokenType(currentType))
			tree.createNode(self.tokens.currentToken())
			self.tokens.nextToken()
			return tree
		elif currentType == self.getTTByName('Control'):
			if currentTokenStr in ['[', '{', '(']:
				if self.tokens.nextToken():
					tree = RHSTree(getRHSSingleTypeFromTokenValue(currentTokenStr))
					tree.addChild(self._parseRHS())
					newTokenStr = self.tokens.currentToken().getValue()
					newTokenType = self._currentTokenType()
					expectedTokenStr = getMatchingControlBlock(currentTokenStr)
					if newTokenType == self.getTTByName('Control') and expectedTokenStr == newTokenStr:
						self.tokens.nextToken()
						return tree
					else:
						raise RuleParsingError("ERROR found: '" + newTokenStr + "', excpecting: '" + expectedTokenStr + "'")
				else:
					raise RuleParsingError("ERROR No matching end-symbol matching: '" + currentTokenStr + "'")
			else:
				raise RuleParsingError("ERROR got token: '" + currentTokenStr + "' without Working tree!")
		else:
			raise RuleParsingError("ERROR found token '" + currentTokenStr + "' of type: " + str(currentType))


def getMatchingControlBlock(startBlock):
	if startBlock == '(':
		return ')'
	elif startBlock == '{':
		return '}'
	elif startBlock == '[':
		return ']'
	raise RuleParsingError("ERROR Token: '" + str(startBlock) + "' is not a control block token!")

def getRHSSingleTypeFromTokenValue(tokenValue):
	if tokenValue == "(":
		return RHSType.GROUP
	elif tokenValue == '{':
		return RHSType.REPEAT
	elif tokenValue == '[':
		return RHSType.OPTIONAL
	raise RuleParsingError("ERROR Cannot determine RHSType of kind SINGLE from token: '" + tokenValue + "'")

def getRHSListTypeFromTokenValue(tokenValue):
	if tokenValue == ',':
		return RHSType.CONCATENATION
	elif tokenValue == '|':
		return RHSType.ALTERNATION
	raise RuleParsingError("ERROR Cannot determine RHSType of kind LIST from token: '" + tokenValue + "'")

def getRHSLeafTypeFromTokenType(tokenType):
	dgttl = defaultGrammarTTL()
	if tokenType == dgttl['Identifier']:
		return RHSType.IDENTIFIER
	elif tokenType == dgttl['Terminal']:
		return RHSType.TERMINAL
	raise RuleParsingError("ERROR Cannot determine RHSType of kind LEAF from token of type: " + str(tokenType))

