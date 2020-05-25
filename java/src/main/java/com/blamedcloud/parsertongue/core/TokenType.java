package com.blamedcloud.parsertongue.core;

import java.util.regex.Pattern;

public class TokenType {

    private final String typeName;
    private final String typeExpression;
    private final boolean ignored;

    private final Pattern typePattern;

    public TokenType(String name, String expression) {
        typeName = name;
        typeExpression = expression;
        ignored = false;

        typePattern = Pattern.compile(typeExpression);
    }

    public TokenType(String name, String expression, boolean ignore) {
        typeName = name;
        typeExpression = expression;
        ignored = ignore;

        typePattern = Pattern.compile(typeExpression);
    }

    public String getName() {
        return typeName;
    }

    public boolean isTypeOf(String raw) {
        return typePattern.matcher(raw).matches();
    }

    public boolean isIgnored() {
        return ignored;
    }

    public Pattern getPattern() {
        return typePattern;
    }

    // technically two different patterns could yield the same regular language
    // but we choose not to care about that case.
    public boolean isSameAs(TokenType other) {
        return (typeName.equals(other.typeName) && typeExpression.equals(other.typeExpression));
    }

    @Override
    public String toString() {
        return "(" + typeName + ", " + typeExpression + ")";
    }

}
