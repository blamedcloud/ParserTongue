package com.blamedcloud.parsertongue.grammar.result;

public class IntParseResult extends NumericParseResult {

    private final int value;

    public IntParseResult(int i) {
        value = i;
    }

    public IntParseResult(String s) {
        value = Integer.valueOf(s);
    }

    public IntParseResult(StringParseResult s) {
        value = Integer.valueOf(s.getValue());
    }

    @Override
    public NumericType getType() {
        return NumericType.INTEGER;
    }

    @Override
    public int getInt() {
        return value;
    }
    @Override
    public int getNumerator() {
        return value;
    }

    @Override
    public int getDenominator() {
        return 1;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public IntParseResult copy() {
        return new IntParseResult(value);
    }

}
