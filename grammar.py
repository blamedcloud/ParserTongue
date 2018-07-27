

class Grammar(object):
	
	def __init__(self, grammarFile):
		full_text = ""
		with open(grammarFile) as FILE:
			for line in FILE:
				full_text += line
		full_text = full_text.replace("\n"," ")
		rawRules = full_text.split(';')
		ruleObjs = [Rule(r) for r in rawRules]
		
		self.ruleDict = {r.lhs() : r for r in ruleObjs}
		
		



class Rule(object):
	
	def __init__(self, rawRule):
		self.terminals = set()
		self.nonTerminals = {}
		self.comments = []


	def _parseOutComments(self):
		pass

	def lhs(self):
		pass

	
