#!/usr/bin/env python3
#parser.py
import os
from errors import *
from grammar import Grammar
from tokenizer import Tokenizer, getTTLForAlphabet
from PythonLibraries.tree import Tree

def populateDependencies(depTree, dependentGrammarDict):
	name = str(depTree.getName())
	dependencies = depTree.getData().getDependencies()
	for dep in dependencies:
		if dep in dependentGrammarDict:
			if depTree.hasAncestorWithName(dep) or depTree.getName() == dep:
				raise GrammarDependencyError("Found Recursive Dependency! Dep = " + str(dep) + "; Node = " + name)
			else:
				depTree.addChild(Tree(dep, dependentGrammarDict[dep]))
		else:
			raise GrammarDependencyError("Missing Dependency! Dep = " + str(dep) + "; Node = " + name)
	for i in range(len(depTree)):
		child = depTree.getChild(i)
		populateDependencies(child, dependentGrammarDict)

def grammarFileToName(grammarFile):
	return os.path.basename(os.path.splitext(grammarFile)[0])

def resolveDependencies(depTree):
	if not depTree.getData().hasLinked():
		neededRuleDicts = {}
		if len(depTree) > 0:
			for i in range(len(depTree)):
				child = depTree.getChild(i)
				childRuleDicts = resolveDependencies(child)
				neededRuleDicts.update(childRuleDicts)
				neededRuleDicts[child.getName()] = child.getData().getRuleDict()
		depTree.getData().setExternalRuleDicts(neededRuleDicts)
		depTree.getData().linkRules()
	return depTree.getData().getExternalRuleDicts()


class Parser(object):

	def __init__(self, grammarFile, startIdentifier = None, dependentGrammarFiles = None):
		self.alphabet = None
		self.ttl = None
		self.grammar = Grammar(grammarFile, startSym = startIdentifier, deferLinkage = True)
		neededDependencies = self.grammar.getDependencies()
		if len(neededDependencies) > 0:
			depTree = Tree(grammarFileToName(grammarFile), self.grammar)
			dependentGrammarDict = {grammarFileToName(gF) : Grammar(gF, deferLinkage = True) for gF in dependentGrammarFiles}
			populateDependencies(depTree, dependentGrammarDict)
			resolveDependencies(depTree)
		else:
			self.grammar.setExternalRuleDicts({})
			self.grammar.linkRules()
		if not self.grammar.hasLinked():
			raise GrammarLinkError("Grammar was never linked. Aborting.")
		self.setGrammarAlphabet()

	def setTTL(self, ttl):
		self.ttl = ttl

	def setAlphabet(self, alphabet):
		self.alphabet = alphabet
		self.ttl = getTTLForAlphabet(self.alphabet)

	def setGrammarAlphabet(self):
		self.alphabet = self.grammar.getAlphabet()
		alphabetTTL = getTTLForAlphabet(self.alphabet)
		if self.grammar.hasRegExTTs():
			self.ttl = self.grammar.getRegExTTs()
			self.ttl.extendWith(alphabetTTL)
		else:
			self.ttl = alphabetTTL

	def setRuleTransform(self, ruleName, f):
		self.grammar.setRuleTransformer(ruleName, f)

	def getGrammar(self):
		return self.grammar

	def parseFile(self, objFile, ignoreWS = False, debug = False):
		raw = ''
		with open(objFile) as FILE:
			for line in FILE:
				raw += line
		return self.parseRaw(raw, ignoreWS, debug)

	def parseRaw(self, raw, ignoreWS = False, debug = False):
		tokens = Tokenizer(self.ttl, ignoreWS)
		tokens.tokenize(raw)
		obj = self.grammar.tryMatch(tokens, debug)
		if obj:
			return obj.getArgs()
		else:
			return obj.getError()


