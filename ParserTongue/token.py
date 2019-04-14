#!/usr/bin/env python3
#token.py
from errors import *
import re

class Token(object):

	def __init__(self, string, tokenType, fullText = None):
		self.value = string
		self.tokenType = tokenType
		if fullText is not None:
			self._fullText = fullText
		else:
			self._fullText = self.value
		if not self.tokenType.isTypeOf(self._fullText):
			raise TokenInstantiationTypeError("Token '" + self._fullText + "' is not of type '" + self.tokenType.getName() + "'")

	def getType(self):
		return self.tokenType

	def getValue(self):
		return self.value

	def __str__(self):
		return '(' + str(self.tokenType) + ", '"+ self.value + "')"

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
		return Token(self.value, self.tokenType, self._fullText)


class TokenType(object):

	def __init__(self, typeName, typePattern, ignore = False):
		self.name = typeName
		self.pattern = typePattern
		try:
			self._re = re.compile(self.pattern)
		except:
			self._re = re.compile(re.escape(self.pattern))
		self.ignore = ignore

	def getName(self):
		return self.name

	def isTypeOf(self, raw):
		return self._re.fullmatch(raw) != None

	def isIgnored(self):
		return self.ignore

	def getRE(self):
		return self._re

	def __eq__(self, other):
		result = True
		if isinstance(other, TokenType):
			if self.name != other.name:
				result = False
#			if self.pattern != other.pattern:
#				result = False
		else:
			if self.name != str(other):
				result = False
		return result

	def __str__(self):
		return "(" + str(self.name) + ", " + str(self.pattern) + ")"

	def __repr__(self):
		return str(self)


