#!/usr/bin/env python3
#parser.py
import os
from errors import *
from grammar import Grammar
from tokenizer import Tokenizer, getTTLForAlphabet
from dependency import manageDependencies, grammarFileToName

class Parser(object):

    def __init__(self, grammarFile, startIdentifier = None, dependentGrammarFiles = None):
        self.alphabet = None
        self.ttl = None
        self.grammar = Grammar(grammarFile, startSym = startIdentifier, deferLinkage = True)
        if self.grammar.hasDependencies():
            manageDependencies(self.grammar, grammarFileToName(grammarFile), dependentGrammarFiles)
        else:
            self.grammar.setExternalRuleDicts({})
            self.grammar.linkRules()
        if not self.grammar.hasLinked():
            raise GrammarLinkError("Grammar was never linked. Aborting.")
        self.setGrammarAlphabet()

    def setTTL(self, ttl):
        self.ttl = ttl

    def setAlphabet(self, alphabet):
        self.alphabet = alphabet
        self.ttl = getTTLForAlphabet(self.alphabet)

    def setGrammarAlphabet(self):
        self.alphabet = self.grammar.getAlphabet()
        alphabetTTL = getTTLForAlphabet(self.alphabet)
        if self.grammar.hasRegExTTs():
            self.ttl = self.grammar.getRegExTTs()
            self.ttl.extendWith(alphabetTTL)
        else:
            self.ttl = alphabetTTL

    def setRuleTransform(self, ruleName, f):
        self.grammar.setRuleTransformer(ruleName, f)

    def getGrammar(self):
        return self.grammar

    def checkFile(self, objFile, ignoreWS = False, debug = False):
        return bool(self.parseFile(objFile, ignoreWS, debug))

    def parseFile(self, objFile, ignoreWS = False, debug = False):
        raw = ''
        with open(objFile) as FILE:
            for line in FILE:
                raw += line
        return self.parseRaw(raw, ignoreWS, debug)

    def checkRaw(self, raw, ignoreWS = False, debug = False):
        return bool(self.parseRaw(raw, ignoreWS, debug))

    def parseRaw(self, raw, ignoreWS = False, debug = False):
        tokens = Tokenizer(self.ttl, ignoreWS)
        tokens.tokenize(raw)
        obj = self.grammar.tryMatch(tokens, debug)
        if obj:
            return obj.getArgs()
        else:
            return obj.getError()


