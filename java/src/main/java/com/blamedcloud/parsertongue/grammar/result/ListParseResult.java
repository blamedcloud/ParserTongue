package com.blamedcloud.parsertongue.grammar.result;

import java.util.ArrayList;
import java.util.List;

public class ListParseResult implements ParseResult {

    List<ParseResult> results;

    public ListParseResult() {
        results = new ArrayList<>();
    }

    public ListParseResult(ParseResult first) {
        results = new ArrayList<>();
        add(first);
    }

    public void add(ParseResult result) {
        results.add(result);
    }

    public List<ParseResult> getValue() {
        return results;
    }

    public void extend(ListParseResult other) {
        List<ParseResult> otherResults = other.getValue();
        for (ParseResult otherResult : otherResults) {
            add(otherResult.copy());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < results.size() - 1; i++) {
            sb.append(results.get(i).toString()).append(", ");
        }
        if (results.size() >= 1) {
            sb.append(results.get(results.size()-1));
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public ListParseResult copy() {
        ListParseResult copyResult = new ListParseResult();
        for (ParseResult childResult : results) {
            copyResult.add(childResult.copy());
        }
        return copyResult;
    }

    public static ListParseResult wrapInList(ParseResult input) {
        return new ListParseResult(input);
    }

}
