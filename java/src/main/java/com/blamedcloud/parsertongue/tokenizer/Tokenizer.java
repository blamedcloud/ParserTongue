package com.blamedcloud.parsertongue.tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {

    private List<Token> tokens;
    private int index;
    private boolean exhausted;
    private TokenizerTypeList tokenizerTypeList;
    private boolean ignoreWhiteSpace;
    private final Pattern whiteSpacePattern = Pattern.compile("\\s+");

    public Tokenizer() {
        setDefaults();
        tokenizerTypeList = TokenizerTypeList.defaultGrammarTTL();
        ignoreWhiteSpace = true;
    }

    public Tokenizer(TokenizerTypeList ttl) {
        setDefaults();
        tokenizerTypeList = ttl;
        ignoreWhiteSpace = true;
    }

    public Tokenizer(boolean ignoreWS) {
        setDefaults();
        tokenizerTypeList = TokenizerTypeList.defaultGrammarTTL();
        ignoreWhiteSpace = ignoreWS;
    }

    public Tokenizer(TokenizerTypeList ttl, boolean ignoreWS) {
        setDefaults();
        tokenizerTypeList = ttl;
        ignoreWhiteSpace = ignoreWS;
    }

    private void setDefaults() {
        tokens = new ArrayList<>();
        index = 0;
        exhausted = false;
    }

    private void resetFromTokenList(List<Token> tokenList) {
        setDefaults();
        tokens = tokenList;
    }

    public int size() {
        return tokens.size();
    }

    public boolean nextToken() {
        if (index + 1 == size()) {
            exhausted = true;
            return false;
        } else {
            index++;
            return true;
        }
    }

    // should this reset exhausted?
    public boolean previousToken() {
        if (index == 0) {
            return false;
        } else {
            index--;
            return true;
        }
    }

    public boolean isExhausted() {
        return exhausted;
    }

    private TokenType determineTokenType(String value) {
        for (TokenType tt : tokenizerTypeList) {
            if (tt.isTypeOf(value)) {
                return tt;
            }
        }
        throw new RuntimeException("Token '" + value + "' does not match any known token types!");
    }

    private Token getEmptyToken() {
        TokenType emptyType = determineTokenType("");
        return new Token("", emptyType);
    }

    public Token currentToken() {
        if (size() == 0) {
            return getEmptyToken();
        } else {
            return tokens.get(index);
        }
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int newIndex) {
        setIndex(newIndex, false);
    }

    public void setIndex(int newIndex, boolean wasExhausted) {
        if ((size() == 0) && (newIndex == 0)) {
            exhausted = wasExhausted;
        } else if ((newIndex >= 0) && (newIndex < size())) {
            index = newIndex;
            exhausted = wasExhausted;
        } else {
            throw new RuntimeException("index out of bounds");
        }
    }

    public List<Tokenizer> splitTokensOn(Token splitToken) {
        List<Tokenizer> tokenizers = new ArrayList<>();
        List<Token> newTokens = new ArrayList<>();

        for (Token token : tokens) {
            if (splitToken.isSameAs(token)) {
                if (newTokens.size() > 0) {
                    Tokenizer newTokenizer = new Tokenizer(tokenizerTypeList, ignoreWhiteSpace);
                    newTokenizer.resetFromTokenList(newTokens);
                    tokenizers.add(newTokenizer);
                    newTokens = new ArrayList<>();
                }
            } else {
                newTokens.add(token.copy());
            }
        }
        // handle left over tokens
        if (newTokens.size() > 0) {
            Tokenizer newTokenizer = new Tokenizer(tokenizerTypeList, ignoreWhiteSpace);
            newTokenizer.resetFromTokenList(newTokens);
            tokenizers.add(newTokenizer);
        }

        return tokenizers;
    }

    public Token getLastToken() {
        if (size() == 0) {
            return getEmptyToken();
        } else {
            return tokens.get(tokens.size() - 1);
        }
    }

    public TokenizerTypeList getTTL() {
        return tokenizerTypeList;
    }

    public void setTTL(TokenizerTypeList ttl) {
        tokenizerTypeList = ttl;
    }

    public void setIgnoreWhiteSpace(boolean ignoreWS) {
        ignoreWhiteSpace = ignoreWS;
    }

    public void tokenize(String rawText) {
        setDefaults();

        while (rawText.length() > 0) {
            boolean hasMatch = false;

            if (ignoreWhiteSpace) {
                Matcher whiteSpaceMatcher = whiteSpacePattern.matcher(rawText);
                if (whiteSpaceMatcher.lookingAt()) {
                    hasMatch = true;
                    rawText = rawText.substring(whiteSpaceMatcher.end());
                }
            }

            if (!hasMatch) {
                for (TokenType tt : tokenizerTypeList) {
                    Pattern ttPattern = tt.getPattern();
                    Matcher ttMatcher = ttPattern.matcher(rawText);
                    if (ttMatcher.lookingAt()) {
                        if (ttMatcher.group().length() > 0) {
                            hasMatch = true;
                            if (!tt.isIgnored()) {
                                // the python code does something different here, but I think this is fine ...
                                tokens.add(new Token(ttMatcher.group(), tt));
                            }
                            rawText = rawText.substring(ttMatcher.end());
                            break;
                        }
                    }
                }
                if (!hasMatch) {
                    throw new RuntimeException("Beginning of text doesn't match any known TokenTypes: '" + rawText + "'");
                }
            }
        }
    }

}
