#!/usr/bin/env python3
#grammar.py
from errors import *
from token import Token, TokenType
from tokenizer import Tokenizer, defaultGrammarTTL
from enum import Enum

class Grammar(object):

	def __init__(self, grammarFile, startSym = None, lastStart = False, quiet = True):
		self.quiet = quiet
		full_text = ""
		with open(grammarFile) as FILE:
			for line in FILE:
				full_text += line
		full_text = full_text.replace("\n"," ").rstrip(' ')
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
			print("Exception thrown while parsing rule",i)
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

	def setStart(self, startSymbol):
		if startSymbol in self.ruleDict:
			self.start = startSymbol
		else:
			raise GrammarParsingError("Cannot add start symbol: '" + str(startSymbol) + "', because it does not exist!")

	def isInLanguage(self, tokens):
		value = self.ruleDict[self.start].expectMatch(tokens)
		if tokens.isExhausted():
			return value
		else:
			return False

class Rule(object):

	def __init__(self, tokens):
		self.terminals = set()
		self.nonTerminals = {}
		self.lhsToken = None
		self.rhsTree = None
		self.tokens = tokens
		self._parseRule()

	def _parseOutComments(self):
		pass

	def expectMatch(self, tokens, level = 0):
		return self.rhsTree.expect(tokens, level)

	def acceptMatch(self, tokens, level = 0):
		return self.rhsTree.accept(tokens, level)

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

class RHSType(Enum):
	IDENTIFIER = 0
	TERMINAL = 1
	OPTIONAL = 2
	REPEAT = 3
	GROUP = 4
	ALTERNATION = 5
	CONCATENATION = 6


class RHSKind(Enum):
	LIST = -1
	LEAF = 0
	SINGLE = 1

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
		return RHSTYPE.OPTIONAL
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

def getRHSKind(rhsType):
	if rhsType is RHSType.IDENTIFIER or rhsType is RHSType.TERMINAL:
		return RHSKind.LEAF
	if rhsType is RHSType.ALTERNATION or rhsType is RHSType.CONCATENATION:
		return RHSKind.LIST
	return RHSKind.SINGLE

class RHSTree(object):

	def __init__(self, levelType):
		self.levelType = levelType
		self.children = []
		self.node = None
		self.levelKind = getRHSKind(self.levelType)
		self.link = None

	def addChild(self, child):
		if self.levelKind.value == 0:
			raise RuleTreeError("Can't add child:\n" + str(child) + "\nTo LEAF RHSTree kind of Type: " + self.levelType.name)
		elif self.levelKind.value == 1 and len(self) == 1:
			raise RuleTreeError("Can't add child:\n" + str(child) + "\nTo SINGLE RHSKind of Type: " + self.levelType.name + ", because it already has one child!")
		else:
			self.children.append(child)

	def addLinkage(self, ruleDict):
		if self.levelType == RHSType.IDENTIFIER:
			if self.node.getValue() in ruleDict:
				self.link = ruleDict[self.node.getValue()]
			else:
				raise GrammarParsingError("ERROR, identifier: '" + self.node.getValue() + "' does not exist in rule mapping!")
		elif self.levelKind in [RHSKind.LIST, RHSKind.SINGLE]:
			for child in self.children:
				child.addLinkage(ruleDict)

	def expect(self, tokens, level = 0):

		### debug:
		index = tokens.getIndex()
		print('\t'*level + "RHSTree.expect(), Type:",self.levelType.name)
		print('\t'*level + "Before Index:",index)

		result = False
		if self.levelType == RHSType.TERMINAL:
			if self.node.getValue() == '':
				result = True
			elif (not tokens.isExhausted()) and self.node == tokens.currentToken():
				tokens.nextToken()
				result = True
		elif self.levelType == RHSType.IDENTIFIER:
			result = self.link.expectMatch(tokens, level + 1)
		elif self.levelType == RHSType.GROUP:
			result = self.children[0].expect(tokens, level + 1)
		elif self.levelType == RHSType.OPTIONAL:
			result = self.children[0].accept(tokens, level + 1) # ???
		elif self.levelType == RHSType.REPEAT:
			value = True
			while value: # the greedy repeat...
				value = self.children[0].accept(tokens, level + 1)
			result = value
		elif self.levelType == RHSType.CONCATENATION:
			value = True
			for child in self.children:
				if not child.expect(tokens, level + 1):
					value = False
					break
			result = value
		elif self.levelType == RHSType.ALTERNATION:
			for child in self.children[:-1]:
				if child.accept(tokens, level + 1):
					result = True
					break
			if not result:
				result = self.children[-1].expect(tokens, level + 1)

		### debug:
		print('\t'*level + "After  Index:",tokens.getIndex())
		print('\t'*level + "result:",result)
		print('\t'*level + "Is Exhausted:",tokens.isExhausted(),"\n")

		return result

	def accept(self, tokens, level = 0):
		index = tokens.getIndex()

		### debug:
		print('\t'*level + "RHSTree.accept(), Type:",self.levelType.name)
		print('\t'*level + "Before Index:",index)

		result = False
		if self.levelType == RHSType.TERMINAL:
			if self.node.getValue() == '':
				result = True
			elif (not tokens.isExhausted()) and self.node == tokens.currentToken():
				tokens.nextToken()
				result = True
		elif self.levelType == RHSType.IDENTIFIER:
			result = self.link.acceptMatch(tokens, level + 1)
		elif self.levelType == RHSType.GROUP:
			result = self.children[0].accept(tokens, level + 1)
		elif self.levelType == RHSType.OPTIONAL:
			result = self.children[0].accept(tokens, level + 1) # ???
		elif self.levelType == RHSType.REPEAT:
			value = True
			while value: # the greedy repeat...
				value = self.children[0].accept(tokens, level + 1)
			result = value
		elif self.levelType == RHSType.CONCATENATION:
			value = True
			for child in self.children:
				if not child.accept(tokens, level + 1):
					value = False
					break
			result = value
		elif self.levelType == RHSType.ALTERNATION:
			for child in self.children[:-1]:
				if child.accept(tokens, level + 1):
					result = True
					break
			if not result:
				result = self.children[-1].accept(tokens, level + 1)
		if not result:
			tokens.setIndex(index)

		### debug:
		print('\t'*level + "After  Index:",tokens.getIndex())
		print('\t'*level + "result:",result)
		print('\t'*level + "Is Exhausted:",tokens.isExhausted(),"\n")

		return result

	def popRightChild(self):
		if len(self.children) > 0:
			return self.children.pop()
		else:
			raise RuleTreeError("Can't Pop right Child from empty child list!")

	def __len__(self):
		return len(self.children)

	def createNode(self, node):
		if self.node == None and self.levelKind.value == 0:
			self.node = node
		else:
			if self.node != None:
				raise RuleTreeError("Can't create Node: '" + str(node) + "' because a node is already present!")
			if self.levelKind.value != 0:
				raise RuleTreeError("Can't create Node: '" + str(node) + "' on RHSTree of Type: " + self.levelType.name)

	def getType(self):
		return self.levelType

	def getKind(self):
		return self.levelKind

	def getChild(self, index = 0):
		return self.children[index]

	def getNode(self):
		return self.node

	def __str__(self, prevIndent = '', indent = '\t'):
		value = prevIndent + "RHSType: " + self.levelType.name + "\n"
		value += prevIndent + "RHSKind: " + self.levelKind.name + '\n'
		if self.node != None:
			value += prevIndent + "Node: " + str(self.node) + '\n'
		else:
			value += prevIndent + "Children:\n"
			for i, child in enumerate(self.children):
				value += prevIndent + str(i) + ":\n"
				value += child.__str__(prevIndent+indent, indent)
		return value


