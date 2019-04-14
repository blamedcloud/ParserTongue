#!/usr/bin/env python3
#rhstree.py
from errors import *
from enum import Enum
from serializer import Serializer

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

	def nonLinkedTerminals(self):
		terminals = []
		if self.levelType == RHSType.TERMINAL:
			terminals.append(self.node.getValue())
		elif self.levelType != RHSType.IDENTIFIER:
			for child in self.children:
				childTerminals = child.nonLinkedTerminals()
				for t in childTerminals:
					if t not in terminals:
						terminals.append(t)
		return terminals

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

	def expect(self, tokens, level = 0, debug = False):
		index = tokens.getIndex()
		exhausted = tokens.isExhausted()
		error = None
		value = Serializer(False, None, "Value never instantiated!")

		if debug:
			print('\t'*level + "RHSTree.expect(), Type:", self.levelType.name, "; Level:",level)
			if self.levelKind == RHSKind.LEAF:
				print('\t'*level + "NODE:", str(self.node))
			print('\t'*level + "Before Index:",index)
			print('\t'*level + "Before Exhaust:",exhausted)
			print('\t'*level + "Current Token:",str(tokens.currentToken()))

		if self.levelType == RHSType.TERMINAL:
			if self.node.getValue() == '':
				yield Serializer(True, '', None)
			elif (not tokens.isExhausted()) and self.node.getValue() == tokens.currentToken().getValue():
				tokens.nextToken()
				yield Serializer(True, self.node.getValue(), None)
			else:
				error = "ERROR: Expected '" + str(self.node.getValue()) + "', got: '" + str(tokens.currentToken().getValue()) + "'"
		elif self.levelType == RHSType.IDENTIFIER:
			for value in self.link.expectMatch(tokens, level + 1, debug):
				if value:
					yield value
				tokens.setIndex(index, exhausted)
			error = value.getError()
		elif self.levelType == RHSType.GROUP:
			for value in self.children[0].expect(tokens, level + 1, debug):
				if value:
					yield value
				tokens.setIndex(index, exhausted)
			error = value.getError()
		elif self.levelType == RHSType.OPTIONAL:
			# First try not parsing this. If the execution gets back to this point,
			# then we need to actually use this option. Thus expect.
			yield Serializer(True, '', None)
			for value in self.children[0].expect(tokens, level + 1, debug):
				if value:
					yield value
				tokens.setIndex(index, exhausted)
			error = value.getError()
		elif self.levelType == RHSType.REPEAT:
			for value in self.expectRepeat(tokens, level, debug):
				if value:
					yield value
			tokens.setIndex(index, exhausted)	# this isn't in the loop because if repeat fails
												# we typically want to try more tokens, not less
			error = value.getError()
		elif self.levelType == RHSType.CONCATENATION:
			for value in self.expectConcat(tokens, 0, level + 1, debug):
				if value:
					yield value
				tokens.setIndex(index, exhausted)
			error = value.getError()
		elif self.levelType == RHSType.ALTERNATION:
			for child in self.children:
				for value in child.expect(tokens, level + 1, debug):
					if value:
						yield value
					tokens.setIndex(index, exhausted)
			error = value.getError()

		if debug:
			print('\t'*level + "After  Index:",tokens.getIndex())
			print('\t'*level + "result:",False)
			print('\t'*level + "Is Exhausted:",tokens.isExhausted())
			print('\t'*level + "value:",str(value), "\n")

		yield Serializer(False, None, error)

	def expectRepeat(self, tokens, level = 0, debug = False):
		yield Serializer(True, [], None)
		# if we get to this point we need at least one instance of this pattern.
		index = tokens.getIndex()
		exhausted = tokens.isExhausted()
		for value in self.children[0].expect(tokens, level + 1, debug):
			if value and (not tokens.isExhausted()):
				newIndex = tokens.getIndex()
				newExhaust = tokens.isExhausted()
				tValue = value.transform(lambda x: [x])
				# create a new instance of the repeat pattern
				# since the first thing this does is yield True,
				# we don't do it here.
				for newValue in self.expectRepeat(tokens, level, debug):
					if newValue:
						tNewValue = newValue.transform(lambda x: tValue.getArgs() + x)
						yield tNewValue
					tokens.setIndex(newIndex, newExhaust)
			elif value and tokens.isExhausted():
				tValue = value.transform(lambda x: [x])
				yield tValue
			tokens.setIndex(index, exhausted)
		err = "Unknown Error in expectRepeat"
		if not value:
			err = value.getError()
		else:
			if not newValue:
				err = newValue.getError()
		yield Serializer(False, None, err)

	def expectConcat(self, tokens, startChild, level = 0, debug = False):
		index = tokens.getIndex()
		exhausted = tokens.isExhausted()
		child = self.children[startChild]
		for value in child.expect(tokens, level, debug):
			if value:
				tValue = value.transform(lambda x: [x])
				if startChild + 1 == len(self):
					yield tValue
				else:
					for childValue in self.expectConcat(tokens, startChild + 1, level, debug):
						if childValue:
							tChildValue = childValue.transform(lambda x: tValue.getArgs() + x)
							yield tChildValue
						tokens.setIndex(index, exhausted)
			tokens.setIndex(index, exhausted)
		err = "Unknown Error in expectConcat"
		if not value:
			err = value.getError()
		else:
			if not childValue:
				err = childValue.getError()
		yield Serializer(False, None, err)

	def walkTree(self, prevIdentifiers):
		isInfinite = False
		treeSize = 0
		if self.levelType == RHSType.TERMINAL:
			treeSize += 1
		elif self.levelType == RHSType.IDENTIFIER:
			(isInfinite, treeSize) = self.link.walk(prevIdentifiers)
		elif self.levelType == RHSType.GROUP:
			(isInfinite, treeSize) = self.children[0].walkTree(prevIdentifiers)
		elif self.levelType == RHSType.OPTIONAL:
			(isInfinite, treeSize) = self.children[0].walkTree(prevIdentifiers)
		elif self.levelType == RHSType.REPEAT:
			(isInf, size) = self.children[0].walkTree(prevIdentifiers)
			if isInf or size >= 1:
				isInfinite = True
				treeSize = -1
		elif self.levelType == RHSType.CONCATENATION:
			for child in self.children:
				(child_isInf, childSize) = child.walkTree(prevIdentifiers)
				if child_isInf:
					isInfinite = True
					treeSize = -1
					break
				treeSize += childSize
		elif self.levelType == RHSType.ALTERNATION:
			for child in self.children:
				(child_isInf, childSize) = child.walkTree(prevIdentifiers)
				if child_isInf:
					isInfinite = True
					treeSize = -1
					break
				if childSize > treeSize:
					treeSize = childSize
		return (isInfinite, treeSize)


