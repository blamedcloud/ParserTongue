#!/usr/bin/env python3
#person.py


class Person:

	def __init__(self, args):
		self.name = args[0]
		self.age = args[1]
		self.dob = args[2]

	def __str__(self):
		output = '{\n'
		output += '\tname: ' + str(self.name) + ';\n'
		output += '\tage:  ' + str(self.age) + ';\n'
		output += '\tdob:  ' + str(self.dob) + ';\n'
		output += '}\n'
		return output


class Name:

	def __init__(self, args):
		self.firstName = args[0]
		self.lastName = args[2]
		self.fullName = args[0] + ' ' + args[2]

	def __str__(self):
		return self.fullName
