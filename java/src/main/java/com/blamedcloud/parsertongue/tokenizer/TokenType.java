package com.blamedcloud.parsertongue.tokenizer;

import java.util.regex.Pattern;

public interface TokenType {

    public String getName();

    public String getExpression();

    public Pattern getPattern();

    public boolean isTypeOf(String raw);

    public boolean isIgnored();

    public boolean isSameAs(TokenType other);

}
