package com.blamedcloud.parsertongue.tokenizer;

import java.util.regex.Pattern;

public class RegexToken implements TokenType {

    private final String typeName;
    private final String typeExpression;
    private final boolean ignored;

    private final Pattern typePattern;

    public RegexToken(String name, String expression) {
        typeName = name;
        typeExpression = expression;
        ignored = false;

        typePattern = Pattern.compile(typeExpression);
    }

    public RegexToken(String name, String expression, boolean ignore) {
        typeName = name;
        typeExpression = expression;
        ignored = ignore;

        typePattern = Pattern.compile(typeExpression);
    }

    @Override
    public String getName() {
        return typeName;
    }

    @Override
    public boolean isTypeOf(String raw) {
        return typePattern.matcher(raw).matches();
    }

    @Override
    public boolean isIgnored() {
        return ignored;
    }

    @Override
    public Pattern getPattern() {
        return typePattern;
    }

    @Override
    public String getExpression() {
        return typeExpression;
    }

    // technically two different patterns could yield the same regular language
    // but we choose not to care about that case.
    @Override
    public boolean isSameAs(TokenType other) {
        if (other instanceof RegexToken) {
            RegexToken regex = (RegexToken) other;
            return (typeName.equals(regex.typeName) && typeExpression.equals(regex.typeExpression));
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "(" + typeName + ", " + typeExpression + ")";
    }
}
