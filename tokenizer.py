

class Tokenizer(object):

	def __init__(self, inFILE):
		self.tokens = []
		self.index = 0
		self._createTokens()

	def _createTokens(self):
		pass

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



