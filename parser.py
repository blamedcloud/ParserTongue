#!/usr/bin/env python3
#parser.py
from grammar import Grammar

class Parser(object):

    def __init__(self, grammarFile, startIdentifier):
        self.grammar = Grammar(grammarFile, startSym = startIdentifier)


    def parse(self, rawFile):
        tokens = None
        with open(rawFile, 'r') as FILE:
            tokens = Tokenizer(FILE)



