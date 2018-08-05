#!/usr/bin/env python3
#tokenizer.py
from errors import *
from token import Token, TokenType
import re

class TokenizerTypeList(object):

	def __init__(self):
		self._typeList = []
		self._indexLookup = {}

	def __len__(self):
		return len(self._typeList)

	def addTokenType(self, tt):
		if not isinstance(tt, TokenType):
			raise TypeError
		if tt.getName() not in self._indexLookup:
			self._indexLookup[tt.getName()] = len(self)
			self._typeList.append(tt)

	def __getitem__(self, item):
		if isinstance(item, int):
			return self._typeList[item % len(self)]
		else:
			return self._typeList[self.indexOf(item)]

	def indexOf(self, item):
		if isinstance(item, TokenType):
			return self._indexLookup[item.getName()]
		else:
			return self._indexLookup[item]

	def __iter__(self):
		return iter(self._typeList)

	def __contains__(self, tt):
		return (tt.getName() in self._indexLookup)

def defaultGrammarTTL():
	gTTL = TokenizerTypeList()
	gTTL.addTokenType(TokenType("End", r';'))
	gTTL.addTokenType(TokenType("Define", r'='))
	gTTL.addTokenType(TokenType("Control", r'[()[\]{}|,]'))
	gTTL.addTokenType(TokenType("Identifier", r'[a-zA-Z][a-zA-Z0-9_]*'))
	gTTL.addTokenType(TokenType("Terminal", r'\'[^\']*\'|"[^"]*"'))
	return gTTL


class Tokenizer(object):

#	identifierType = TokenType("Identifier", r'[a-zA-Z][a-zA-Z0-9_]*')
#	controlType = TokenType("Control", r'[()[\]{}|,]')
#	defineType = TokenType("Define", r'=')
#	endType = TokenType("End", r';')
#	terminalType = TokenType("Terminal", r'[^\']*|[^"]*')

	def __init__(self, ttl = defaultGrammarTTL(), ignoreWhiteSpace = True):
		self.tokens = []
		self.index = 0
		self._exhausted = False
		self.ttl = ttl
		self._ignoreWS = ignoreWhiteSpace
		self._wsRE = re.compile('\s+')
#		try:
#			self._createTokens(inData)
#		except TokenizerError as err:
#			print(err.message)
#			raise err

	def nextToken(self):
		self.index += 1
		if self.index >= len(self.tokens):
			self._exhausted = True
			self.index = len(self.tokens)-1
			return False
		return True

	def previousToken(self):
		self.index -= 1
		if self.index < 0:
			self.index = 0
			return False
		return True

	def isExhausted(self):
		return self._exhausted

	def currentToken(self):
		return self.tokens[self.index]

	def getIndex(self):
		return self.index

	def __len__(self):
		return len(self.tokens)

	def __str__(self):
		return str(self.tokens)

	def splitTokensOn(self, splitToken):
		tokenizers = []
		newTokens = []
		for t in self.tokens:
			if t == splitToken:
				if len(newTokens) > 0:
					newTokenizer = Tokenizer(self.ttl, self._ignoreWS)
					newTokenizer.resetFromTokenList(newTokens)
					tokenizers.append(newTokenizer)
					newTokens = []
			else:
				newTokens.append(t)
		if len(newTokens) > 0:
			newTokenizer = Tokenizer(self.ttl, self._ignoreWS)
			tokenizers.append(newTokenizer.resetFromTokenList(newTokens))
		return tokenizers

	def resetFromTokenList(self, tokenList):
		self.tokens = []
		for t in tokenList:
			if not isinstance(t, Token):
				raise TokenizerCreationError("in Tokenizer.resetFromTokenList : given list contains non-token!")
			self.tokens.append(t)

	# used by Grammar to check the last token is ';' before splitting.
	def getLastToken(self):
		return self.tokens[-1]

	# Not super recomended, but provided anyway
	def getTokenList(self):
		return self.tokens

	def setTTL(self, ttl):
		self.ttl = ttl

	def getTTL(self):
		return self.ttl

	def setIgnoreWhiteSpace(self, value):
		self._ignoreWS = bool(value)

	def determineTokenType(self, tokenStr):
		for tokenType in self.ttl:
			if tokenType.isTypeOf(tokenStr):
				return tokenType
		raise UnknownTokenTypeError("Token '" + tokenStr + "' does not match any known tokenTypes!")

	def tokenize(self, rawText):
		self.tokens = []
		text = rawText

		### do something about empty string...

		while len(text) > 0:
			matchObj = None
			if self._ignoreWS:
				matchObj = self._wsRE.match(text)
			if matchObj is None:
				for tt in self.ttl:
					ttRE = tt.getRE()
					matchObj = ttRE.match(text)
					if matchObj is not None:
						if matchObj.end() > 0:
							break
				if matchObj is not None:
					self.tokens.append(Token(text[:matchObj.end()], tt))
					text = text[matchObj.end():]
				else:
					raise TokenizerNoMatchError("Beginning of text doesn't match any known TokenTypes:",text)
			else:
				text = text[matchObj.end():]


