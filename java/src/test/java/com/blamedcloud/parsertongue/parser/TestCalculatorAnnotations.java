package com.blamedcloud.parsertongue.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.blamedcloud.parsertongue.grammar.annotations.AnnotationManager;
import com.blamedcloud.parsertongue.grammar.result.IntParseResult;
import com.blamedcloud.parsertongue.grammar.result.ListParseResult;
import com.blamedcloud.parsertongue.grammar.result.NumericParseResult;
import com.blamedcloud.parsertongue.grammar.result.ParseResult;
import com.blamedcloud.parsertongue.grammar.result.ParseResultException;
import com.blamedcloud.parsertongue.grammar.result.ParseResultTransformer;
import com.blamedcloud.parsertongue.grammar.result.RationalParseResult;

public class TestCalculatorAnnotations {

    @Test
    public void testInts() {
        Parser calculator = getCalculator();

        ParseResultTransformer result = calculator.parseString("105");
        checkValue(result, 105);

        result = calculator.parseString("-561");
        checkValue(result, -561);

        result = calculator.parseString("0");
        checkValue(result, 0);
    }

    @Test
    public void testAdd() {
        Parser calculator = getCalculator();

        ParseResultTransformer result = calculator.parseString("1 + 3");
        checkValue(result, 4);

        result = calculator.parseString("6 + 0");
        checkValue(result, 6);

        result = calculator.parseString("-5 + 12");
        checkValue(result, 7);

        result = calculator.parseString("-5 + -4");
        checkValue(result, -9);
    }

    @Test
    public void testMinus() {
        Parser calculator = getCalculator();

        ParseResultTransformer result = calculator.parseString("1 - 3");
        checkValue(result, -2);

        result = calculator.parseString("-2 - 9");
        checkValue(result, -11);

        result = calculator.parseString("5 - -3");
        checkValue(result, 8);
    }

    @Test
    public void testMult() {
        Parser calculator = getCalculator();

        ParseResultTransformer result = calculator.parseString("1 * 3");
        checkValue(result, 3);

        result = calculator.parseString("-2 * 4");
        checkValue(result, -8);

        result = calculator.parseString("-3*-5");
        checkValue(result, 15);
    }

    @Test
    public void testPower() {
        Parser calculator = getCalculator();

        ParseResultTransformer result = calculator.parseString("2 ^ 3");
        checkValue(result, 8);

        result = calculator.parseString("-2 ^ 2");
        checkValue(result, 4);

        result = calculator.parseString("4 ^ 1");
        checkValue(result, 4);
    }

    @Test
    public void complexTests() {
        Parser calculator = getCalculator();

        ParseResultTransformer result = calculator.parseString("1 + 2 * 3 + 4");
        checkValue(result, 11);

        result = calculator.parseString("2 + 3 - 6 + 9");
        checkValue(result, 8);

        result = calculator.parseString("8 + 3 - 2 - 7");
        checkValue(result, 2);

        result = calculator.parseString("8 + 3/2 - 5");
        checkValue(result, 9, 2);

        result = calculator.parseString("(8 + 3)/2 - 5*3/2*6/9+1");
        checkValue(result, 3, 2);

        result = calculator.parseString("2*1+2^3-5/2");
        checkValue(result, 15, 2);

        result = calculator.parseString("2^3^2");
        checkValue(result, 512);

        result = calculator.parseString("12/3/2");
        checkValue(result, 2, 1);
    }

    private void checkValue(ParseResultTransformer result, int expected) {
        assertTrue(result.isValid());
        assertTrue(result.getResult() instanceof IntParseResult);
        IntParseResult parseResult = (IntParseResult) result.getResult();
        assertEquals("Wrong integer", expected, parseResult.getInt());
    }

    private void checkValue(ParseResultTransformer result, int expectedNumerator, int expectedDenominator) {
        assertTrue(result.isValid());
        assertTrue(result.getResult() instanceof RationalParseResult);
        RationalParseResult parseResult = (RationalParseResult) result.getResult();
        assertEquals("Wrong numerator", expectedNumerator, parseResult.getNumerator());
        assertEquals("Wrong denominator", expectedDenominator, parseResult.getDenominator());
    }

    private Parser getCalculator() {
        File grammarFile = new File("src/test/resources/calculatorAnnotations.ebnf");

        AnnotationManager annotationManager = AnnotationManager.getDefaultManager();

        Parser parser = Parser.newBuilder(grammarFile)
                              .setIgnoreWhiteSpaceDefault(true)
                              .setAnnotationManager(annotationManager)
                              .build();

        parser.setRuleTransform("pow_expr", TestCalculatorAnnotations::powExpr);
        parser.setRuleTransform("expr", TestCalculatorAnnotations::evalExpr);

        parser.setRuleTransform("term", ListParseResult::flattenList);
        parser.composeRuleTransform("term", TestCalculatorAnnotations::evalTerm);

        return parser;
    }

    private static ParseResult evalExpr(ParseResult expr) throws ParseResultException {
        ListParseResult exprList = ListParseResult.flattenList(expr);
        List<ParseResult> terms = exprList.getValue();
        NumericParseResult value = (NumericParseResult)terms.get(0);
        for (int i = 1; i < terms.size(); i += 2) {
            String op = terms.get(i).toString();
            NumericParseResult rValue = (NumericParseResult)terms.get(i+1);
            if (op.equals("+")) {
                value = value.add(rValue);
            } else if (op.equals("-")) {
                value = value.subtract(rValue);
            } else {
                throw new ParseResultException("Unknown operator in expr parse");
            }
        }
        return value;
    }

    private static ParseResult evalTerm(ParseResult term) throws ParseResultException {
        ListParseResult termList = (ListParseResult) term;
        List<ParseResult> terms = termList.getValue();
        NumericParseResult value = (NumericParseResult)terms.get(0);
        for (int i = 1; i < terms.size(); i += 2) {
            String op = terms.get(i).toString();
            NumericParseResult rValue = (NumericParseResult)terms.get(i+1);
            if (op.equals("*")) {
                value = value.multiply(rValue);
            } else if (op.equals("/")) {
                value = value.divide(rValue);
            } else {
                throw new ParseResultException("Unknown operator in term parse");
            }
        }
        return value;
    }

    private static ParseResult powExpr(ParseResult expr) throws ParseResultException {
        ListParseResult listExpr = (ListParseResult) expr;
        NumericParseResult left = (NumericParseResult) listExpr.getValue().get(0);
        NumericParseResult right = (NumericParseResult) listExpr.getValue().get(2);
        return left.power(right);
    }
}
