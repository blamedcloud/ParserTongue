#!/usr/bin/env python3
#grammar.py
from errors import *
from tokentype import Token, TokenType
from tokenizer import Tokenizer, getTTLForAlphabet, TokenizerTypeList
from smallestStrings import smallestStrGen
from rule import Rule
from serializer import Serializer

class Grammar(object):

    def __init__(self, grammarFile, startSym = None, lastStart = False, quiet = True, deferLinkage = False):
        self.quiet = quiet
        self.rules = None
        self.ruleDict = None
        self.start = None
        self.externalDependencies = []
        self._externalRuleDicts = {}
        self._deferLinkage = deferLinkage
        self._linkageDone = False
        self._additionalTokenTypes = TokenizerTypeList()

        full_text = ""
        with open(grammarFile) as FILE:
            for line in FILE:
                full_text += line
        fullTokenizer = Tokenizer()
        fullTokenizer.tokenize(full_text)
        splitToken = Token(';', fullTokenizer.getTTL()['End'])
        if not fullTokenizer.getLastToken()    == splitToken:
            raise GrammarParsingError("Found Text after last rule! Did you forget ';'?")
        ruleTokenizers = fullTokenizer.splitTokensOn(splitToken)

        try:
            self.rules = []
            for i, tokens in enumerate(ruleTokenizers):
                if not self.quiet:
                    print("PARSING RULE:",i)
                nextRule = Rule(tokens)
                if nextRule.hasDependency():
                    dependency = nextRule.getDependency()
                    if dependency not in self.externalDependencies:
                        self.externalDependencies.append(dependency)
                if nextRule.isRegExRule():
                    self._additionalTokenTypes.addTokenType(nextRule.getRegExTT())
                self.rules.append(nextRule)
        except GrammarError as err:
            print("Exception thrown while parsing rule",i)
            print(err.message)
            raise err
        except Exception as err:
            print("Unknown Exception thrown while parsing rule",i)
            raise err

        self.ruleDict = {r.lhs().getValue() : r for r in self.rules}
        if not self._deferLinkage:
            self.linkRules()

        if startSym == None:
            index = 0
            # set last rule as start instead of first
            if lastStart:
                index = -1
            self.start = self.rules[index].lhs().getValue()
        else:
            self.setStart(startSym)

    def setExternalRuleDicts(self, externalRuleDicts):
        self._externalRuleDicts = externalRuleDicts

    def getRegExTTs(self):
        return self._additionalTokenTypes

    def hasRegExTTs(self):
        return (len(self._additionalTokenTypes) > 0)

    def hasLinked(self):
        return self._linkageDone

    def getExternalRuleDicts(self):
        return self._externalRuleDicts

    def getDependencies(self):
        return self.externalDependencies

    def hasDependencies(self):
        return len(self.externalDependencies) > 0

    def linkRules(self):
        if not self._linkageDone:
            for rule in self.rules:
                rule.createLinkage(self.ruleDict, self._externalRuleDicts)
            self._linkageDone = True

    def getRuleList(self):
        return self.rules

    def getRuleDict(self):
        return self.ruleDict

    def setRuleTransformer(self, ruleName, f):
        if ruleName in self.ruleDict:
            self.ruleDict[ruleName].setTransformer(f)
        else:
            raise KeyError("No Rule with name '" + str(ruleName) + "' exists!")

    def setStart(self, startSymbol):
        if startSymbol in self.ruleDict:
            self.start = startSymbol
        else:
            raise GrammarParsingError("Cannot add start symbol: '" + str(startSymbol) + "', because it does not exist!")

    def isInLanguage(self, tokens, debug = False):
        return bool(self.tryMatch(tokens,debug))

    def tryMatch(self, tokens, debug = False):
        if not self._linkageDone:
            raise GrammarLinkError("Cannot Match without Linking. Aborting.")
        value = None
        for value in self.ruleDict[self.start].expectMatch(tokens, 0, debug):
            if value:
                if len(tokens) == 0:
                    if debug:
                        print("tryMatch-valueA:",str(value))
                    return value
                elif tokens.isExhausted():
                    if debug:
                        print("tryMatch-valueB:",str(value))
                    return value
        if tokens.isExhausted() or len(tokens) == 0:
            if debug:
                print("tryMatch-valueC:",str(value))
            return value
        return Serializer(False, None, "Tokens not Exhausted")

    # NOTE: this is the naive method that tries each possible string in A* (kleene star)
    # until it finds the next string in the language and yields it
    # as such, it is pretty inefficient.
    def getValidStringGen(self, maxIter = None, ignoreWS = True, debug = False):
        if not self._linkageDone:
            raise GrammarLinkError("Cannot Walk without Linking. Aborting.")
        def validStrGen(maxIter, ignoreWS, debug):
            (isInfinite, treeSize) = self.ruleDict[self.start].walk()
            iters = 0
            alphabet = self.getAlphabet()
            if '' not in alphabet:
                alphabet.append('')
            tokenizer = Tokenizer(getTTLForAlphabet(alphabet), ignoreWS)
            tokenizer.tokenize('')
            if self.isInLanguage(tokenizer, debug):
                yield ''
            iters += 1
            alphabet.remove('')
            if maxIter is None and not isInfinite:
                # if this is a finite language, we
                # use an upperbound of |A|^num(terminals)
                # where |A| is the size of the alphabet
                # and num(terminals) is (kind of) the number
                # of terminals in the grammar file.
                # This should be an upper bound, but a seriously
                # bad one in most cases.
                maxIter = (len(alphabet)**treeSize) + 1
            finished = False
            if (maxIter is not None) and iters >= maxIter:
                finished = True
            testStringGen = smallestStrGen(alphabet, True)()
            while not finished:
                testStr = next(testStringGen)
                tokenizer.tokenize(testStr)
                if self.isInLanguage(tokenizer, debug):
                    yield testStr
                iters += 1
                if (maxIter is not None) and iters >= maxIter:
                    finished = True
        return validStrGen(maxIter, ignoreWS, debug)

    def getAlphabet(self, includeExternal = True):
        if not self._linkageDone and includeExternal:
            raise GrammarLinkError("Cannot get Alphabet without Linking. Aborting.")
        alphabet = []
        for rule in self.rules:
            terminals = rule.getTerminals()
            for t in terminals:
                if t not in alphabet:
                    alphabet.append(t)
        if includeExternal:
            for _ , ruleDict in self._externalRuleDicts.items():
                for _ , rule in ruleDict.items():
                    terminals = rule.getTerminals()
                    for t in terminals:
                        if t not in alphabet:
                            alphabet.append(t)
        return sorted(alphabet, key=lambda x: len(x), reverse=True)

    def classifyFirstNStrings(self, number, ignoreWS = True, debug = False):
        if not self._linkageDone:
            raise GrammarLinkError("Cannot Classify without Linking. Aborting.")
        alphabet = self.getAlphabet()
        if '' not in alphabet:
            alphabet.append('')
        tokenizer = Tokenizer(getTTLForAlphabet(alphabet), ignoreWS)
        classification = {}
        tokenizer.tokenize('')
        classification[''] = self.isInLanguage(tokenizer, debug)
        alphabet.remove('')
        number -= 1
        stringGen = smallestStrGen(alphabet, True)()
        while number > 0:
            s = next(stringGen)
            tokenizer.tokenize(s)
            classification[s] = self.isInLanguage(tokenizer, debug)
            number -= 1
        return classification


