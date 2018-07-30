#!/usr/bin/env python3
import tokenizer
from enum import Enum

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
		self.lhsToken = None
		self.rhsTree = None
		self.tokens = tokenizer.Tokenizer(rawRule)
		self._parseRule()

	def _parseOutComments(self):
		pass

	def lhs(self):
		return self.lhsToken

	def _parseRule(self):
		if len(self.tokens) < 3:
			raise RuleParsingError("Rule has too few tokens: " + raw)
		if self._currentTokenType() == tokenizer.Tokenizer.identifierType:
			self.lhsToken = self.tokens.currentToken()
			self.tokens.nextToken()
		else:
			raise RuleParsingError("LHS is not an identifier: " + raw)
		if self._currentTokenType() == tokenizer.Tokenizer.defineType:
			self.tokens.nextToken()
		else:
			raise RuleParsingError("Rule has no '=': " + raw)
		self.rhsTree = self._parseRHS()

	def _currentTokenType(self):
		return self.tokens.currentToken().getType()

	def _parseRHS(self, workingTree = None):
		tree = None
		currentType = self._currentTokenType()
		if workingTree == None:
			if currentType == tokenizer.Tokenizer.identifierType: 
				tree = RHSTree(RHSType.IDENTIFIER)
				tree.createNode(self.tokens.currentToken())
				if self.tokens.nextToken():
					return self._parseRHS(tree)
				else:
					return tree
			elif currentType == tokenizer.Tokenizer.terminalType:
				tree = RHSTree(RHSType.TERMINAL)
				tree.createNode(self.tokens.currentToken())
				if self.tokens.nextToken():
					return self._parseRHS(tree)
				else:
					return tree
			elif currentType == tokenizer.Tokenizer.controlType:
				currentTokenStr = self.tokens.currentToken().getValue()
				if currentTokenStr == '[':
					if self.tokens.nextToken():
						tree = RHSTree(RHSType.OPTIONAL)
						tree.addChild(self._parseRHS())
				elif currentTokenStr == '{':
					pass
				elif currentTokenStr == '(':
					pass
				else:
					raise RuleParsingError("ERROR got token: '" + currentTokenStr + "' without Working tree!")
		else:
			pass
		return tree

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
	
	def addChild(self, child):
		if self.levelKind.value == 0:
			raise RuleTreeError("Can't add child:\n" + str(child) + "\nTo LEAF RHSTree kind of Type: " + self.levelType.name) 
		elif self.levelKind.value == 1 and len(self) == 1:
			raise RuleTreeError("Can't add child:\n" + str(child) + "\nTo SINGLE RHSKind of Type: " + self.levelType.name + ", because it already has one child!")
		else:
			self.children.append(child)

	def __len__(self):
		return len(self.children)

	def createNode(self, node):
		if self.node == None and self.levelKind.value == 0:
			self.node = node
		else:
			if self.none != None:
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
			for child in self.children:
				child.__str__(prevIndent+indent, indent)


class GrammarError(Exception):
	pass


class GrammarParsingError(GrammarError):

	def __init__(self, message):
		self.message = message


class RuleParsingError(GrammarError):

	def __init__(self, message):
		self.message = message

 
class RuleTreeError(GrammarError):

	def __init__(self, message):
		self.message = message


