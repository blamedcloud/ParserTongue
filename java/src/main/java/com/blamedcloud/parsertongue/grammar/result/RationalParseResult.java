package com.blamedcloud.parsertongue.grammar.result;

public class RationalParseResult extends NumericParseResult {

    private final int numerator;
    private final int denominator;

    private final double value;

    public RationalParseResult(IntParseResult n, IntParseResult d) throws ParseResultException {
        this(n.getInt(), d.getInt());
    }

    public RationalParseResult(int n, int d) throws ParseResultException {
        if (d == 0) {
            throw new ParseResultException("Divide by 0 error");
        }
        int g = RationalParseResult.gcd(n, d);
        numerator = n / g;
        denominator = d / g;

        value = ((double)numerator)/((double)denominator);
    }

    @Override
    public NumericType getType() {
        return NumericType.RATIONAL;
    }

    @Override
    public int getInt() {
        if (denominator == 1) {
            return numerator;
        } else {
            throw new RuntimeException("Can't get int from rational");
        }
    }

    @Override
    public int getNumerator() {
        return numerator;
    }

    @Override
    public int getDenominator() {
        return denominator;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(numerator) + '/' + Integer.toString(denominator);
    }

    @Override
    public ParseResult copy() {
        try {
            return new RationalParseResult(numerator, denominator);
        } catch (ParseResultException e) {
            // there shouldn't be a divide by 0 error since this object exists
            throw new RuntimeException("Divide by 0 error in copy");
        }
    }

    public static int gcd(int a, int b) {
        if (b == 0) return a;
        return gcd(b, a % b);
    }


}
