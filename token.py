#!/usr/bin/env python3
#token.py
from errors import *
import re

class Token(object):

	def __init__(self, string, tokenType, quoteChar = None):
		self.value = string
		self._quoteChar = quoteChar
		self.tokenType = tokenType
		if not self.tokenType.isTypeOf(self.value):
			raise TokenInstantiationTypeError("Token '" + self.value + "' is not of type '" + self.tokenType.getName() + "'")

	def getType(self):
		return self.tokenType

	def getValue(self):
		return self.value

	def __str__(self):
		if self._quoteChar == None:
			quote = "'"
		else:
			quote = self._quoteChar
		return '(' + str(self.tokenType) + ", "+ quote + self.value + quote + ")"

	def __repr__(self):
		return str(self)

	def __eq__(self, other):
		result = True
		if isinstance(other, Token):
			if self.value != other.value:
				result = False
			if self.tokenType != other.tokenType:
				result = False
		else:
			if self.value != str(other):
				result = False
		return result

	def copy(self):
		return Token(self.value, self.tokenType, self._quoteChar)


class TokenType(object):
	
	def __init__(self, typeName, typePattern):
		self.name = typeName
		self.pattern = typePattern
		self._re = re.compile(self.pattern)

	def getName(self):
		return self.name

	def isTypeOf(self, raw):
		return self._re.fullmatch(raw) != None

	def __eq__(self, other):
		result = True
		if isinstance(other, TokenType):
			if self.name != other.name:
				result = False
			if self.pattern != other.pattern:
				result = False
		else:
			if self.name != str(other):
				result = False
		return result

	def __str__(self):
		return self.name

	def __repr__(self):
		return str(self)


