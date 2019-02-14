#!/usr/bin/env python3
#main.py
import person
from .. import parser

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

def main():
	p = parser.Parser('person.ebnf')
	p.setRuleTransform('person', person.Person)
	p.setRuleTransform('name', person.Name)
	p.setRuleTransform('firstName', collapse)
	p.setRuleTransform('lastName', collapse)
	p.setRuleTransform('age', lambda x: x[0])
	p.setRuleTransform('positiveInt', lambda x: int(collapse(x)))
	p.setRuleTransform('dob', lambda x: x[0])
	p.setRuleTransform('date', collapse)
	return p.parseFile('testPerson.txt')


if __name__ == "__main__":
	obj = main()
	print(obj)

