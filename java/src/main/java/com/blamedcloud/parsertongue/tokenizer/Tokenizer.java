package com.blamedcloud.parsertongue.tokenizer;

import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.EMPTY_NAME;

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

    public boolean hasSameTokens(Tokenizer other) {
        if (tokens.size() != other.tokens.size()) {
            return false;
        }
        for (int i = 0; i < tokens.size(); i++) {
            if (!tokens.get(i).isSameAs(other.tokens.get(i))) {
                return false;
            }
        }
        return true;
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

    private TokenType determineTokenType(String value) throws TokenizerException {
        for (TokenType tt : tokenizerTypeList) {
            if (tt.isTypeOf(value)) {
                return tt;
            }
        }
        if (value.length() == 0) {
            // if the empty string doesn't have a token type, make one up:
            TokenType emptyType = new ExactToken(EMPTY_NAME, "");
            return emptyType;
        }
        throw new TokenizerException("Token '" + value + "' does not match any known token types!");
    }

    private Token getEmptyToken() {
        TokenType emptyType;
        try {
            emptyType = determineTokenType("");
        } catch (TokenizerException e) {
            // The empty string should always have a token type
            // So this should never get thrown
            throw new RuntimeException(e);
        }
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

    public void tokenize(String rawText) throws TokenizerException {
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
                                if (ttMatcher.groupCount() > 0) {
                                    // concatenate all the "real" groups.
                                    // group 0 is the entire match, and is not counted
                                    // towards groupCount().
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = 1; i < ttMatcher.groupCount() + 1; i++) {
                                        String groupValue = ttMatcher.group(i);
                                        if (groupValue != null && groupValue.length() > 0) {
                                            sb.append(groupValue);
                                        }
                                    }
                                    tokens.add(new Token(sb.toString(), tt, ttMatcher.group(0)));
                                } else {
                                    // no groups to worry about, token value is entire match
                                    tokens.add(new Token(ttMatcher.group(), tt));
                                }
                            }
                            rawText = rawText.substring(ttMatcher.end());
                            break;
                        }
                    }
                }
                if (!hasMatch) {
                    throw new TokenizerException("Beginning of text doesn't match any known TokenTypes: '" + rawText + "'");
                }
            }
        }
    }

}
