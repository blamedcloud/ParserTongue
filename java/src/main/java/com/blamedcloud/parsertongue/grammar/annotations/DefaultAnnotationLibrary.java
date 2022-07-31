package com.blamedcloud.parsertongue.grammar.annotations;

import java.util.function.Function;

import com.blamedcloud.parsertongue.grammar.result.IntParseResult;
import com.blamedcloud.parsertongue.grammar.result.ListParseResult;
import com.blamedcloud.parsertongue.grammar.result.ParseResult;
import com.blamedcloud.parsertongue.grammar.result.ParseResultFunction;
import com.blamedcloud.parsertongue.grammar.result.StringParseResult;
import com.blamedcloud.parsertongue.tokenizer.ExactToken;
import com.blamedcloud.parsertongue.tokenizer.Token;
import com.blamedcloud.parsertongue.tokenizer.TokenType;

public class DefaultAnnotationLibrary extends MapAnnotationLibrary {

    public DefaultAnnotationLibrary() {
        super();
        initialize();
    }

    private void initialize() {
        addNoArg(TO_INT_NAME, TO_INT_FUNCTION);
        addNoArg(FLATTEN_NAME, FLATTEN_FUNCTION);
        addNoArg(CONCAT_NAME, CONCAT_FUNCTION);
        addUnary(INDEX_NAME, INDEX_FUNCTION);
    }

    public void addNoArg(String name, ParseResultFunction f) {
        addAnnotation(name, new NoArgAnnotation(getToken(name), f));
    }

    public void addUnary(String name, Function<Token, ParseResultFunction> f) {
        addAnnotation(name, new UnaryAnnotation(getToken(name), f));
    }

    private static Token getToken(String value) {
        TokenType tt = new ExactToken(value, value);
        return new Token(value, tt);
    }

    public static final String TO_INT_NAME = "toInt";
    public static final ParseResultFunction TO_INT_FUNCTION = (pr -> new IntParseResult(pr.toString()));

    public static final String FLATTEN_NAME = "flatten";
    public static final ParseResultFunction FLATTEN_FUNCTION = ListParseResult::flattenList;

    public static final String CONCAT_NAME = "concat";
    public static final ParseResultFunction CONCAT_FUNCTION = pr -> {
        StringBuilder sb = new StringBuilder();
        ListParseResult lpr = (ListParseResult) pr;
        for (ParseResult result : lpr.getValue()) {
            sb.append(result.toString());
        }
        return new StringParseResult(sb.toString());
    };

    public static final String INDEX_NAME = "index";
    public static final Function<Token, ParseResultFunction> INDEX_FUNCTION = t -> {
        int index = Integer.parseInt(t.getValue());
        return (pr -> {
            ListParseResult lpr = (ListParseResult) pr;
            return lpr.getValue().get(index);
        });
    };

}
