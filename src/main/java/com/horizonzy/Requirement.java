package com.horizonzy;

import java.util.List;
import java.util.Map;

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

    public boolean matches(Map<String, String> labels) {
        switch (operator) {
            case Operator.In:
            case Operator.Equals:
            case Operator.DoubleEquals:
                String val = labels.get(key);
                if (val == null) {
                    return false;
                }
                return hasValue(val);
            case Operator.NotIn:
            case Operator.NotEquals:
                String val2 = labels.get(key);
                if (val2 == null) {
                    return true;
                }
                return !hasValue(val2);
            case Operator.Exists:
                return labels.containsKey(key);
            case Operator.DoesNotExist:
                return !labels.containsKey(key);
            case Operator.GreaterThan:
            case Operator.LessThan:
                String val3 = labels.get(key);
                if (val3 == null) {
                    return false;
                }
                Integer lsValue = null;
                try {
                    lsValue = Integer.parseInt(val3);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            String.format("ParseInt failed for value %s in label %s", val3, labels),
                            e);
                }
                if (strValues.size() != 1) {
                    throw new IllegalArgumentException(String.format(
                            "Invalid values count %d of requirement %s, for 'Gt', 'Lt' operators, exactly one value is required",
                            strValues.size(), this));
                }
                Integer rValue = null;
                for (String strValue : strValues) {
                    try {
                        rValue = Integer.parseInt(strValue);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(String.format(
                                "ParseInt failed for value %s in requirement %s, for 'Gt', 'Lt' operators, the value must be an integer",
                                strValue, this));
                    }
                }
                return (Operator.GreaterThan.equals(operator) && lsValue > rValue) || (
                        Operator.LessThan.equals(operator) && lsValue < rValue);
            default:
                return false;
        }
    }

    public boolean hasValue(String value) {
        return strValues.contains(value);
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

    @Override
    public String toString() {
        return "Requirement{" +
                "key='" + key + '\'' +
                ", operator='" + operator + '\'' +
                ", strValues=" + strValues +
                '}';
    }
}
