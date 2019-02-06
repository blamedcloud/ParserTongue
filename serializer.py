#!/usr/bin/env python3
#serializer.py


class Serializer(object):

	def __init__(self, valid, args, err):
		self.valid = valid
		self.args = args
		self.err = err

	def __bool__(self):
		return self.valid

	def getArgs(self):
		return self.args

	def getError(self):
		return self.err

	def transform(self, f):
		if self:
			return Serializer(self.valid, self.args, self.err)
		else:
			raise ValueError("Can't transform failed parse!")
