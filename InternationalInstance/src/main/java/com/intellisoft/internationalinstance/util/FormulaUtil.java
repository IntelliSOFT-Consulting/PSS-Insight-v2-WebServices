package com.intellisoft.internationalinstance.util;

import com.intellisoft.internationalinstance.DbFormula;

import java.util.List;

public class FormulaUtil {
    public static double computeResult(DbFormula formula, List<Double> values) {
        String numeratorExpression = formula.getNumerator() != null ? formula.getNumerator() : "";
        String denominatorExpression = formula.getDenominator() != null ? formula.getDenominator() : "";
        String format = formula.getFormat() != null ? formula.getFormat() : "";

        double numeratorValue = getValueFromExpression(numeratorExpression, values);
        double denominatorValue = getValueFromExpression(denominatorExpression, values);

        switch (format.toLowerCase()) {
            case "percentage":
                return (numeratorValue / denominatorValue) * 100;
            case "ratio":
                return numeratorValue / denominatorValue;
            default:
                throw new IllegalArgumentException("Invalid format: " + format);
        }
    }

    private static double getValueFromExpression(String expression, List<Double> values) {
        String[] placeholders = expression.split("[{}]");
        StringBuilder evaluatedExpression = new StringBuilder(expression);

        for (int i = 0; i < placeholders.length; i++) {
            if (i % 2 != 0) {
                int valueIndex = Integer.parseInt(placeholders[i]);
                if (valueIndex < values.size()) {
                    double value = values.get(valueIndex);
                    evaluatedExpression = new StringBuilder(evaluatedExpression.toString()
                            .replace("{" + placeholders[i] + "}", Double.toString(value)));
                } else {
                    throw new IllegalArgumentException("Invalid value index: " + valueIndex);
                }
            }
        }

        try {
            return Double.parseDouble(evaluatedExpression.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid expression: " + expression);
        }
    }
}
