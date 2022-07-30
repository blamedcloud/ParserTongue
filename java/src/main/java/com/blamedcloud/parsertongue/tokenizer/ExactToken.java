package com.blamedcloud.parsertongue.tokenizer;

import java.util.regex.Pattern;

public class ExactToken implements TokenType {

    private final String typeName;
    private final String typeExpression;
    private final boolean ignored;

    private final Pattern typePattern;

    public ExactToken(String name, String expression) {
        typeName = name;
        typeExpression = expression;
        ignored = false;

        typePattern = Pattern.compile(typeExpression, Pattern.LITERAL);
    }

    public ExactToken(String name, String expression, boolean ignore) {
        typeName = name;
        typeExpression = expression;
        ignored = ignore;

        typePattern = Pattern.compile(typeExpression, Pattern.LITERAL);
    }

    @Override
    public String getName() {
        return typeName;
    }

    @Override
    public String getExpression() {
        return typeExpression;
    }

    @Override
    public Pattern getPattern() {
        return typePattern;
    }

    @Override
    public boolean isTypeOf(String raw) {
        return typeExpression.equals(raw);
    }

    @Override
    public boolean isIgnored() {
        return ignored;
    }

    @Override
    public boolean isSameAs(TokenType other) {
        if (other instanceof ExactToken) {
            ExactToken exact = (ExactToken) other;
            return (typeName.equals(exact.typeName) && typeExpression.equals(exact.typeExpression));
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "(" + typeName + ", " + typeExpression + ")";
    }

}
