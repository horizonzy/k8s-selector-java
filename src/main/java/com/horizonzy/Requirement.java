package com.horizonzy;

import java.util.List;

public class Requirement {

    private String key;

    private String operator;

    private List<String> strValues;

    public Requirement() {
    }

    public Requirement(String key, String operator, List<String> strValues) {
        this.key = key;
        this.operator = operator;
        this.strValues = strValues;
    }

    public String getKey() {
        return key;
    }

    public String getOperator() {
        return operator;
    }

    public List<String> getStrValues() {
        return strValues;
    }

    public static Requirement newRequirement(String key, String operator, List<String> vals) {
        Selector.validateLabelKey(key);
        switch (operator) {
            case Operator.In:
            case Operator.NotIn:
                if (vals.size() == 0) {
                    throw new IllegalArgumentException(
                            "for 'in', 'notin' operators, values set can't be empty");
                }
                break;
            case Operator.Equals:
            case Operator.DoubleEquals:
            case Operator.NotEquals:
                if (vals.size() != 1) {
                    throw new IllegalArgumentException(
                            "exact-match compatibility requires one single value");
                }
                break;
            case Operator.Exists:
            case Operator.DoesNotExist:
                if (vals.size() != 0) {
                    throw new IllegalArgumentException(
                            "values set must be empty for exists and does not exist");
                }
                break;
            case Operator.GreaterThan:
            case Operator.LessThan:
                if (vals.size() != 1) {
                    throw new IllegalArgumentException(
                            "for 'Gt', 'Lt' operators, exactly one value is required");
                }
                for (String val : vals) {
                    try {
                        Integer.parseInt(val);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                                "for 'Gt', 'Lt' operators, the value must be an integer");
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("not supported current operator:" + operator);
        }
        for (String val : vals) {
            Selector.validateLabelValue(key, val);
        }
        return new Requirement(key, operator, vals);
    }
}
