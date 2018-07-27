
class Tokenizer(object):

	def __init__(self, inFILE):
		self.tokens = []
		self.index = 0
		try:
			self._createTokens(inFILE)
		except TokenizerQuotingError as err:
			print(err.message)
			raise err

	def _createTokens(self, FILE):
		text = ''
		for line in FILE:
			text += line
		
		# first tokenize qouted blocks individually.
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
						self.tokens.append(Token(subBlock, "'"))
		
	def _handleQuoting(self, rawText):
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
						blocks.append(Token(thisBlock, char))
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

	def nextToken(self):
		self.index += 1
		if self.index >= len(self.tokens):
			self.index = len(self.tokens)-1
			return False
		return True

	def previousToken(self):
		self.index -= 1
		if self.index < 0:
			self.index = 0
			return False
		return True

	def currentToken(self):
		return self.tokens[self.index]

	def __str__(self):
		return str(self.tokens)


class Token(object):

	def __init__(self, string, quoteChar = None):
		self.string = string
		self._quoteChar = quoteChar

	def __str__(self):
		return self.string

	def __repr__(self):
		if self._quoteChar == None:
			return str(self)
		elif self._quoteChar == '"':
			return '"' + str(self) + '"'
		else:
			return "'" + str(self) + "'"


class TokenizerQuotingError(Exception):

	def __init__(self, message):
		self.message = message
