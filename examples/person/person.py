#!/usr/bin/env python3
#person.py
import sys
sys.path.insert(0,'../../ParserTongue')
import parser

# collapses (in-place) a nested structure of lists and strings
# to just a single string
def collapse(args):
	if type(args) == str:
		return args
	elif type(args) == list:
		inPlace = ''
		for item in args:
			inPlace += collapse(item)
		return inPlace
	else:
		raise TypeError("args was not list or str, was: " + str(type(args)))

class Person(object):

	def __init__(self, args):
		# starts from 1 because '{' is at 0
		self.name = args[1]
		self.age = args[2]
		self.dob = args[3]

	def __str__(self):
		output = '{\n'
		output += '\tname: ' + str(self.name) + ';\n'
		output += '\tage:  ' + str(self.age) + ';\n'
		output += '\tdob:  ' + str(self.dob) + ';\n'
		output += '}\n'
		return output


class Name(object):

	def __init__(self, args):
		self.firstName = args[0]
		self.lastName = args[2]

	def __str__(self):
		return self.firstName + ', ' + self.lastName


class PersonParser(object):

	def __init__(self):
		p = parser.Parser('person.ebnf')
		p.setRuleTransform('person', Person)
		p.setRuleTransform('name', Name)
		p.setRuleTransform('firstName', collapse)
		p.setRuleTransform('lastName', collapse)
		p.setRuleTransform('age', lambda x: x[0])
		p.setRuleTransform('positiveInt', lambda x: int(collapse(x)))
		p.setRuleTransform('dob', lambda x: x[0])
		p.setRuleTransform('date', collapse)
		self.personParser = p

	def getPerson(self, personFile):
		return self.personParser.parseFile(personFile, True)


