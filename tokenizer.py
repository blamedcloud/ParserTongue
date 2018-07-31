#!/usr/bin/env python3
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


class Tokenizer(object):

	identifierType = TokenType("Identifier", r'[a-zA-Z][a-zA-Z0-9_]*')
	controlType = TokenType("Control", r'[()[\]{}|,]')
	defineType = TokenType("Define", r'=')
	endType = TokenType("End", r';')
	terminalType = TokenType("Terminal", r'[^\']*|[^"]*')

	def __init__(self, inData, allowEscapes = False):
		self.tokens = []
		self.index = 0
		self.escapes = allowEscapes
		self._exhausted = False
		try:
			self._createTokens(inData)
		except TokenizerError as err:
			print(err.message)
			raise err
	
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
					newTokenizer = Tokenizer('')
					newTokenizer.resetFromTokenList(newTokens)
					tokenizers.append(newTokenizer)
					newTokens = []
			else:
				newTokens.append(t)
		if len(newTokens) > 0:
			newTokenizer = Tokenizer('')
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

	def _determineTokenType(self, tokenStr):
		# terminalType is handled elsewhere
		typeList = [Tokenizer.endType, Tokenizer.defineType, Tokenizer.controlType, Tokenizer.identifierType]
		for tokenType in typeList:
			if tokenType.isTypeOf(tokenStr):
				return tokenType
		raise UnknownTokenTypeError("Token '" + tokenStr + "' does not match any known tokenTypes!") 

	def _createTokens(self, rawTokens):
		text = ''
		if isinstance(rawTokens, str):
			text = rawTokens
		else: # a file or a list primarily
			for line in rawTokens:
				text += line
		
		# first tokenize qouted blocks individually.
		if self.escapes:
			blocks = self._handleQuotingWithEscapes(text)
		else:
			blocks = self._handleQuoting(text)
	
		for block in blocks:
			if isinstance(block, Token):
				self.tokens.append(block)
			else:	
				# homogenize white-space
				block = block.replace('\n',' ').replace('\t',' ')
				# flatten white space
				block = ' '.join(block.split(' '))				
				# split on white space
				subBlocks = block.split(' ')
				for subBlock in subBlocks:
					if len(subBlock) > 0:
						self.tokens.append(Token(subBlock, self._determineTokenType(subBlock)))

	def _handleQuoting(self, rawText):
		blocks = []

		thisBlock = ''
		quoteMatch = None

		for char in rawText:
			if char == '"' or char == "'":
				if quoteMatch == None:
					blocks.append(thisBlock)
					thisBlock = ''
					quoteMatch = char
				elif char == quoteMatch:
					blocks.append(Token(thisBlock, Tokenizer.terminalType, char))
					thisBlock = ''
					quoteMatch = None
				else:
					thisBlock += char
			else:
				thisBlock += char
		if quoteMatch != None:
			raise TokenizerQuotingError("Encountered Unmatched quote of type: " + quoteMatch)

		if len(thisBlock) > 0:
			blocks.append(thisBlock)

		return blocks

	def _handleQuotingWithEscapes(self, rawText):
		blocks = []
		
		escaping = False
		thisBlock = ''

		quoteMatch = None

		for char in rawText:
		
			if char == '\\':
				if escaping:
					thisBlock += '\\'
					escaping = False
				else:
					escaping = True
			elif char == 't':
				if escaping:
					thisBlock += '\t'
					escaping = False
				else:
					thisBlock += 't'
			elif char == 'n':
				if escaping:
					thisBlock += '\n'
					escaping = False
				else:
					thisBlock += 'n'
			elif char == '"' or char == "'":
				if escaping:
					if quoteMatch == None:
						raise TokenizerQuotingError("Escaped quotes only allowed within quoted section!")
					else:
						thisBlock += char
						escaping = False
				else:
					if quoteMatch == None:
						blocks.append(thisBlock)
						thisBlock = ''
						quoteMatch = char
					elif char == quoteMatch:
						blocks.append(Token(thisBlock, terminalType, char))
						thisBlock = ''
						quoteMatch = None
					else:
						thisBlock += char
			else:
				thisBlock += char
			
		if quoteMatch != None:
			raise TokenizerQuotingError("Encountered Unmatched quote of type: " + quoteMatch)

		if len(thisBlock) > 0:
			blocks.append(thisBlock)

		return blocks


class TokenizerError(Exception):
	pass


class UnknownTokenTypeError(TokenizerError):

	def __init__(self, message):
		self.message = message


class TokenizerCreationError(TokenizerError):
	
	def __init__(self, message):
		self.message = message


class TokenInstantiationTypeError(TokenizerError):

	def __init__(self, message):
		self.message = message


class TokenizerQuotingError(TokenizerError):

	def __init__(self, message):
		self.message = message


