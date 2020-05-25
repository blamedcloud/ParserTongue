package com.blamedcloud.parsertongue.tokenizer;

public class Token {

    private final String value;
    private final TokenType tokenType;
    private final String fullText;

    public Token(String raw, TokenType tokenType) {
        this.value = raw;
        this.tokenType = tokenType;
        this.fullText = raw;

        if (!this.tokenType.isTypeOf(fullText)) {
            throw new RuntimeException("Token '" + this.fullText + "' is not of type '" + this.tokenType.getName() + "'!");
        }
    }

    public Token(String raw, TokenType tokenType, String fullText) {
        this.value = raw;
        this.tokenType = tokenType;
        this.fullText = fullText;

        if (!this.tokenType.isTypeOf(fullText)) {
            throw new RuntimeException("Token '" + this.fullText + "' is not of type '" + this.tokenType.getName() + "'!");
        }
    }

    public TokenType getType() {
        return tokenType;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return '(' + tokenType.toString() + ", '" + value + "')";
    }

    public boolean isSameAs(Token other) {
        return (value.equals(other.value) && tokenType.isSameAs(other.tokenType));
    }

    public Token copy() {
        return new Token(value, tokenType, fullText);
    }
}
