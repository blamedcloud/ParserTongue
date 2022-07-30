package com.blamedcloud.parsertongue.grammar.result;

public class DoubleParseResult extends NumericParseResult {

    private final double value;

    public DoubleParseResult(double d) {
        value = d;
    }

    public DoubleParseResult(String s) {
        value = Double.valueOf(s);
    }

    public DoubleParseResult(StringParseResult s) {
        value = Double.valueOf(s.getValue());
    }

    @Override
    public NumericType getType() {
        return NumericType.REAL;
    }

    @Override
    public int getInt() {
        throw new RuntimeException("Can't get int from double");
    }

    @Override
    public int getNumerator() {
        throw new RuntimeException("Can't get numerator from double");
    }

    @Override
    public int getDenominator() {
        throw new RuntimeException("Can't get denominator from double");
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    @Override
    public ParseResult copy() {
        return new DoubleParseResult(value);
    }

}
