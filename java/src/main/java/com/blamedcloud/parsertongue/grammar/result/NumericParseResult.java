package com.blamedcloud.parsertongue.grammar.result;

public abstract class NumericParseResult implements ParseResult {

    public enum NumericType {
        INTEGER,
        RATIONAL,
        REAL;
    }

    private static IntParseResult NEGATIVE_ONE = new IntParseResult(-1);

    public abstract NumericType getType();

    // which of these is implemented depends on which NumericType this is.
    public abstract int getInt();
    public abstract int getNumerator();
    public abstract int getDenominator();
    public abstract double getValue();

    public NumericParseResult add(NumericParseResult other) {
        if (this.getType() == NumericType.REAL || other.getType() == NumericType.REAL) {
            return new DoubleParseResult(this.getValue() + other.getValue());
        } else if (this.getType() == NumericType.RATIONAL || other.getType() == NumericType.RATIONAL) {
            int newNum = this.getNumerator() * other.getDenominator() + other.getNumerator() * this.getDenominator();
            int newDem = this.getDenominator() * other.getDenominator();
            try {
                return new RationalParseResult(newNum, newDem);
            } catch (ParseResultException e) {
                // there shouldn't be a divide by 0 error here
                throw new RuntimeException("Divide by 0 error in add");
            }
        } else { // both ints
            return new IntParseResult(this.getInt() + other.getInt());
        }
    }

    public NumericParseResult subtract(NumericParseResult other) {
        NumericParseResult oppOther = other.opposite();
        return this.add(oppOther);
    }

    public NumericParseResult multiply(NumericParseResult other) {
        if (this.getType() == NumericType.REAL || other.getType() == NumericType.REAL) {
            return new DoubleParseResult(this.getValue() * other.getValue());
        } else if (this.getType() == NumericType.RATIONAL || other.getType() == NumericType.RATIONAL) {
            int newNum = this.getNumerator() *  other.getNumerator();
            int newDem = this.getDenominator() * other.getDenominator();
            try {
                return new RationalParseResult(newNum, newDem);
            } catch (ParseResultException e) {
                // there shouldn't be a divide by 0 error here
                throw new RuntimeException("Divide by 0 error in add");
            }
        } else { // both ints
            return new IntParseResult(this.getInt() * other.getInt());
        }
    }

    public NumericParseResult opposite() {
        return this.multiply(NEGATIVE_ONE);
    }

    public NumericParseResult divide(NumericParseResult other) throws ParseResultException { // divide by zero
        if (this.getType() == NumericType.REAL || other.getType() == NumericType.REAL) {
            if (Double.compare(other.getValue(), 0) == 0) {
                throw new ParseResultException("Divide by zero");
            } else {
                return new DoubleParseResult(this.getValue() / other.getValue());
            }
        } else if (this.getType() == NumericType.RATIONAL || other.getType() == NumericType.RATIONAL) {
            int newNum = this.getNumerator() *  other.getDenominator();
            int newDem = this.getDenominator() * other.getNumerator();
            return new RationalParseResult(newNum, newDem);
        } else { // both ints
            return new RationalParseResult(this.getInt(), other.getInt());
        }
    }

    public NumericParseResult power(NumericParseResult other) throws ParseResultException { // imaginary numbers
        if (this.getType() != NumericType.REAL && (other.getType() == NumericType.INTEGER || (other.getType() == NumericType.RATIONAL && other.getDenominator() == 1))) {
            int power = other.getNumerator();

            double newNum, newDen;
            if (power < 0) {
                newNum = Math.pow(this.getDenominator(), -1*power);
                newDen = Math.pow(this.getNumerator(), -1*power);
            } else {
                newNum = Math.pow(this.getNumerator(), power);
                newDen = Math.pow(this.getDenominator(), power);
            }
            sanityCheck(newNum);
            sanityCheck(newDen);

            if (((int)newDen) == 1) {
                return new IntParseResult((int)newNum);
            } else {
                return new RationalParseResult((int)newNum, (int)newDen);
            }

        } else {
            double result = Math.pow(this.getValue(), other.getValue());
            sanityCheck(result);
            return new DoubleParseResult(result);
        }
    }

    private static void sanityCheck(double result) throws ParseResultException {
        if (Double.isInfinite(result)) {
            throw new ParseResultException("Infinity in power operation");
        } else if (Double.isNaN(result)) {
            throw new ParseResultException("NaN in power operation");
        }
    }

}
