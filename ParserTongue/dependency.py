#!/usr/bin/env python3
#dependency.py
import os
from PythonLibraries.tree import Tree
from grammar import Grammar
from errors import GrammarDependencyError

### 'main' function ###
def manageDependencies(mainGrammar, mainGrammarName, extraGrammarFiles):
		depTree = Tree(mainGrammarName, mainGrammar)
		dependentGrammarDict = DependencyDict(extraGrammarFiles)
		populateDependencies(depTree, dependentGrammarDict)
		resolveDependencies(depTree)

### helpers ###

def grammarFileToName(grammarFile):
	return os.path.basename(os.path.splitext(grammarFile)[0])

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

class DependencyDict(object):

	def __init__(self, dependentGrammarFiles):
		self.grammarDict = {grammarFileToName(gf) : gf for gf in dependentGrammarFiles}

	def __contains__(self, gf):
		return gf in self.grammarDict

	def __len__(self):
		return len(self.grammarDict)

	def __getitem__(self, key):
		if key in self.grammarDict:
			if not isinstance(self.grammarDict[key], Grammar):
				gf = self.grammarDict[key]
				self.grammarDict[key] = Grammar(gf, deferLinkage = True)
			return self.grammarDict[key]
		else:
			raise KeyError

	def __iter__(self):
		return iter(self.grammarDict.keys())


