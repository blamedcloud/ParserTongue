#!/usr/bin/env python3
#json.py
import sys
sys.path.insert(0,'../../ParserTongue')
import parser
from rule import collapseToStr, collapseToList

# this class is here to stop nested arrays in json from
# being mangled by the collapseToList function.
class JsonArray(object):

	def __init__(self, arr):
		self.arr = arr

	def getArr(self):
		return self.arr

	def __repr__(self):
		return str(self)

	def __str__(self):
		return str(self.arr)

def arrayTransform(x):
	x = collapseToList(x)
	value = []
	x = x[1:-1] # remove the brackets
	if len(x) == 1:
		value = x
	elif len(x) % 2 == 1:
		for i in range(0,len(x),2): # count by 2 to skip the ','s
			value.append(x[i])
	else:
		print("Error Parsing array!")
	return value

def objectTransform(x):
	x = collapseToList(x)
	value = {}
	x = x[1:-1] # remove the brackets
	if len(x) == 3:
		value = {x[0] : x[2]}
	elif len(x) % 4 == 3:
		for i in range(0,len(x),4): # count by 4 to skip the ':'s and ','s
			value[x[i]] = x[i+2]
	else:
		print("Error parsing object!")
	return value

class JSONParser(object):

	def __init__(self):
		p = parser.Parser('json.ebnf', dependentGrammarFiles = ['../common.ebnf'])
		p.setRuleTransform('number', lambda x: float(collapseToStr(x)))
		p.setRuleTransform('string', lambda x: collapseToStr(x)[1:-1]) # remove the quotes
		p.setRuleTransform('array', lambda x: JsonArray(arrayTransform(x)))
		p.setRuleTransform('object', objectTransform)
		p.setRuleTransform('true', lambda x: True)
		p.setRuleTransform('false', lambda x: False)
		p.setRuleTransform('null', lambda x: None)
		self.jsonParser = p

	def getJsonObj(self, jsonFile):
		return self.jsonParser.parseFile(jsonFile, True)


