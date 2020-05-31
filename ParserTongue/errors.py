#!/usr/bin/env python3
#errors.py

class ParserTongueError(Exception):
    pass


########################
### Tokenizer Errors ###
########################

class TokenizerError(ParserTongueError):
    pass


class TokenInstantiationTypeError(TokenizerError):

    def __init__(self, message):
        self.message = message


class UnknownTokenTypeError(TokenizerError):

    def __init__(self, message):
        self.message = message


class TokenizerNoMatchError(TokenizerError):

    def __init__(self, message):
        self.message = message


class TokenizerCreationError(TokenizerError):

    def __init__(self, message):
        self.message = message


######################
### Grammar Errors ###
######################

class GrammarError(ParserTongueError):
    pass


class GrammarParsingError(GrammarError):

    def __init__(self, message):
        self.message = message


class RuleParsingError(GrammarError):

    def __init__(self, message):
        self.message = message


class RuleLinkageError(GrammarError):

    def __init__(self, message):
        self.message = message


class RuleTreeError(GrammarError):

    def __init__(self, message):
        self.message = message


class GrammarLinkError(GrammarError):

    def __init__(self, message):
        self.message = message


class GrammarDependencyError(GrammarError):

    def __init__(self, message):
        self.message = message


